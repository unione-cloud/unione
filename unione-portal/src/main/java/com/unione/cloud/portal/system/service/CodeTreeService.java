package com.unione.cloud.portal.system.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.nacos.shaded.com.google.common.base.Objects;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.portal.system.model.SysCodeLvsn;
import com.unione.cloud.portal.system.model.SysCodeTree;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodeTreeService {


    @Autowired
    private RedisService redisService;

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionService;


    private static final String CODE_LVSN_CACHEKEY="SYSCODE:LVSNINFO";
	
	private static final String CODE_LVSN_VALUE_CACHEKEY="SYSCODE:LVSNVALUE:%s";

    private static final String CODE_TREE_CACHEKEY="SYSCODE:TREE";

	private static final String CODE_ERROR_CACHEKEY="SYSCODE:ERROR:%s";

	private static final String CODE_TREE_LOCK_CACHEKEY="SYSCODE:TREELOCK:%s";

	private static final String CODE_LVSN_LOCK_CACHEKEY="SYSCODE:LVSNLOCK:%s";

	/**
	 * 	code tree 错误缓存时间，单位分钟，默认5分钟
	 */
	@Value("${codetree.error.timelief:5}")
	private int CODE_ERROR_TIMELIEF;


    private static final char chs[] = new char[36];
	private static final Map<String, Integer> cmap= new HashMap<>();
	static {
		for(int i = 0; i < 10 ; i++) {
			chs[i] = (char)('0' + i);
			cmap.put(chs[i]+"", i);
		}
		for(int i = 0; i < 26; i++) {
			chs[10 + i] = (char)('A' + i);
			cmap.put(chs[10 + i]+"", 10+i);
		}
		for(int i = 0; i < 26 && (36+i)<chs.length; i++) {
			chs[36+i] = (char)('a' + i);
			cmap.put(chs[36+i]+"", 36+i);
		}
	}

    
	/**
	 * 	同步保存缓存信息到db
	 */
	public void save2db() {
		log.info("进入:同步保存code lvsn缓存信息到db方法");
		Set<String> keys = redisService.getMapOps().keys(CODE_LVSN_CACHEKEY);
		log.info("code lvsn redis缓存数量,count:{}",keys.size());
		
		String[] fields = {"lvLen","currentMaxLv","currentLvsn"};
		log.info("同步保存到db：开始");
		keys.stream().forEach(key->{
			SysCodeLvsn lvsn=redisService.getMap(CODE_LVSN_CACHEKEY, key);
			if(lvsn!=null) {
				log.info("保存code lvsn信息到db，sid:{},sn:{},key:{}",lvsn.getId(),lvsn.getTreeSn(),key);
				lvsn.setLastUpdated(DateUtil.date());
				
				String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,key);
				String format="%0"+lvsn.getLvLen()+"d";
				List<Long> list = redisService.getList(lvsnValueKey);
				lvsn.setCurrentMaxLv(list.size());
				StringBuffer buf=new StringBuffer();
				for(Long v:list) {
					if(v==null) {
						log.warn("缓存数据异常，请及时处理,code lvsn:{},redis key:{}",lvsn.getTreeSn(),lvsnValueKey);
						continue;
					}
					String str=toStr(v);
					if(str.length()<lvsn.getLvLen()) {
						str = String.format(format, 0).substring(0,lvsn.getLvLen()-str.length())+str;
					}
					buf.append(str);
				}
				lvsn.setCurrentLvsn(buf.toString());
				int len = dataBaseDao.updateById(SqlBuilder.build(lvsn).field(fields));
				log.info("保存code lvsn信息到db，id:{},sn:{},key:{},result:{}",lvsn.getId(),lvsn.getTreeSn(),key,len);
				if(len>0) {
					// 回写到redis
					redisService.putMap(CODE_LVSN_CACHEKEY, key, lvsn);
				}
			}else {
				log.warn("从redis中获取code lvsn缓存key:{},数据为空,忽略",key);
			}
		});
		log.info("同步保存code lvsn到db：结束");
		
		log.info("退出:同步保存code lvsn缓存信息到db方法");
	}
	
	
	/**
	 * 	保存指定的层级编码信息
	 * @param tree sn
	 */
	public void save2db(String sn) {
		log.info("进入:保存指定的层级编码信息,tree sn:{}",sn);
		SysCodeTree tree = loadTree(sn);
		AssertUtil.service().notNull(tree, "code tree层级树定义信息未找到,sn:"+sn);
		
		//0全局，1租户级，2机构级 
		String lvsnInfoKey=this.buildLvsnRedistKey(tree);
		String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,lvsnInfoKey);
		SysCodeLvsn lvsn=redisService.getMap(CODE_LVSN_CACHEKEY, lvsnInfoKey);
		AssertUtil.service().notNull(lvsn, "code lvsn层级信息未找到");
		
		log.info("保存lvsn信息到db，id:{},sn:{},key:{}",lvsn.getId(),lvsn.getTreeSn(),lvsnInfoKey);
		lvsn.setLastUpdated(DateUtil.date());
		
		String format="%0"+lvsn.getLvLen()+"d";
		String[] fields = {"lvLen","currentMaxLv","currentLvsn"};
		List<Long> list = redisService.getList(lvsnValueKey);
		lvsn.setCurrentMaxLv(list.size());
		StringBuffer buf=new StringBuffer();
		for(Long v:list) {
			if(v==null) {
				log.error("缓存数据异常，请及时处理,lvsn:{},redis key:{}",lvsn.getTreeSn(),lvsnValueKey);
				return;
			}
			String str=toStr(v);
			if(str.length()<lvsn.getLvLen()) {
				str = String.format(format, 0).substring(0,lvsn.getLvLen()-str.length())+str;
			}
			buf.append(str);
		}
		lvsn.setCurrentLvsn(buf.toString());
		
		int len = dataBaseDao.updateById(SqlBuilder.build(lvsn).field(fields));
		log.info("保存lvsn信息到db，id:{},sn:{},key:{},result:{}",lvsn.getId(),lvsn.getTreeSn(),lvsnInfoKey,len);
		if(len>0) {
			// 回写到redis
			redisService.putMap(CODE_LVSN_CACHEKEY, lvsnInfoKey, lvsn);
		}
		
		log.info("退出:保存指定的层级编码信息,tree sn:{}",sn);
	}
	
	/**
	 * 	重置层级编码层级
	 * @param tree sn
	 * @param lv
	 */
	public void reset(String sn,int lv) {
		log.info("进入:重置层级编码层级方法,code tree sn:{},lv:{}",sn,lv);
		SysCodeTree tree = loadTree(sn);
		AssertUtil.service().notNull(tree, "code tree层级树定义信息未找到,sn:"+sn);
		
		//0全局，1租户级，2机构级 
		String lvsnInfoKey=this.buildLvsnRedistKey(tree);
		String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,lvsnInfoKey);
		SysCodeLvsn lvsn=redisService.getMap(CODE_LVSN_CACHEKEY, lvsnInfoKey);
		AssertUtil.service().notNull(lvsn, "code lvsn层级信息未找到");
		
		long size = redisService.getListOps().size(lvsnValueKey);
		AssertUtil.service().isTrue(lv>=0, "lv参数不能小于0")
			      .isTrue(lv<=size, "目前最大层级为:"+size);
		
		// 重置redis缓存
		redisService.putListValue(lvsnValueKey, lv, 0);
		
		this.save2db(sn);
		
		log.info("退出:重置层级编码层级方法,code tree sn:{},lv:{}",sn,lv);
	}
	

	private String buildLvsnRedistKey(SysCodeTree tree) {
		//0全局，1租户级，2机构级 
		String lvsnKey=tree.getSn();
		if(tree.getTypes()!=null) {
			if(tree.getTypes()==1) {
				return String.format("TENANT:%s:%s", sessionService.getTenantId(),lvsnKey);
			}else if(tree.getTypes()==2) {
				return String.format("ORGAN:%s:%s", sessionService.getOrgId(),lvsnKey);
			}
		}
		return lvsnKey;
	}

	private String buildLvsnRedistKey(SysCodeLvsn tree) {
		//0全局，1租户级，2机构级 
		String lvsnKey=tree.getTreeSn();
		if(tree.getLvType()!=null) {
			if(tree.getLvType()==1) {
				return String.format("TENANT:%s:%s", sessionService.getTenantId(),lvsnKey);
			}else if(tree.getLvType()==2) {
				return String.format("ORGAN:%s:%s", sessionService.getOrgId(),lvsnKey);
			}
		}
		return lvsnKey;
	}

	private String toStr(long number) {
		int radix=36;
		log.debug("进入:进制转换：10->toRadix方法，num:{},radix:{}",number,radix);
		if(number==0) {
			log.debug("退出:进制转换：10->toRadix方法，num:{},radix:{},str:{}",number,radix,0);
			return "0";
		}
		StringBuilder sb = new StringBuilder();
		while (number != 0) {
			sb.append(chs[(int)(number%radix)]);
			number = number / radix;
		}
		String str=sb.reverse().toString();
		log.debug("退出:进制转换：10->toRadix方法，num:{},radix:{},str:{}",number,radix,str);
		return str;
	}

	/**
	 * 	进制转换：36进制->10
	 * @param str
	 * @return
	 */
	private long toLong(String str) {
		int radix=36;
		log.debug("进入:进制转换：radix->10方法,str:{},radix:{}",str,radix);
		long num=0L;
		for(int i=str.length()-1;i>=0;i--) {
			char c=str.charAt(i);
			int n = cmap.get(c+"");
			num = num+n*(long)Math.pow(radix, (str.length()-i-1));
		}
		log.debug("退出:进制转换：radix->10方法,str:{},radix:{},num:{}",str,radix,num);
		return num;
	}
	
	/**
	 * 	初始化层级编码value
	 * @param tree
	 * @param lvsn
	 */
	private void initLvsnValue(SysCodeTree tree,SysCodeLvsn lvsn) {
		log.debug("进入:初始化层级编码value方法,tree sn:{}",tree.getSn());
		//0全局，1租户级，2机构级 
		String lvsnRedisKey=this.buildLvsnRedistKey(tree);
		
		// 层级信息处理
		String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,lvsnRedisKey);
		int lvlen = (tree.getLvLen()!=null?tree.getLvLen():3);
		String currentLvsn = StringUtils.trimToNull(lvsn.getCurrentLvsn());
		if(currentLvsn!=null) {
			int maxlv=currentLvsn.length()/lvlen;
			lvsn.setCurrentMaxLv(maxlv);
			List<Long> list=new ArrayList<>();
			Boolean hasLvsnRedisCache = true;
			if(!redisService.getListOps().getOperations().hasKey(lvsnValueKey)){
				hasLvsnRedisCache = false;
			}
			for(int i=0;i<maxlv;i++) {
				String num=currentLvsn.substring(lvlen*i, lvlen*(i+1));
				Long   value = toLong(num);
				list.add(value);
				if(hasLvsnRedisCache) {
					redisService.putListValue(lvsnValueKey, i, value);
				}else{
					redisService.putListValue(lvsnValueKey, value);
				}
			}
			log.info("成功将层级编码value初始化到redis,key:{},value:{}",lvsnValueKey,list);
		}else {
			redisService.putListValue(lvsnValueKey, 0L);
		}
		
		log.debug("退出:初始化层级编码value方法,tree sn:{}",tree.getSn());
	}


    /**
	 * 	加载code tree
	 * @param sn
	 * @return
	 */
	public SysCodeTree loadTree(String sn) {
		log.debug("进入:加载code tree方法,sn:{}",sn);
		SysCodeTree tree = this.loadTree(sn, 4);
		log.debug("退出:加载code tree方法,sn:{},tree:{}",sn,tree);
		return tree;
	}
	@SuppressWarnings("null")
	private SysCodeTree loadTree(String sn,int time) {
		log.debug("进入:加载code tree方法,sn:{},time:{}",sn,time);
		AssertUtil.service().isTrue(time>0, "系统繁忙,请稍后再试");
		
		SysCodeTree tree=redisService.getMap(CODE_TREE_CACHEKEY, sn);
		log.info("从redis中获取缓存,redis key:{},tree sn:{},tree:{}",CODE_TREE_CACHEKEY,sn,tree);
		
		// 如果map 缓存中为空，则从异常缓存中获取
		String errorKey=String.format(CODE_ERROR_CACHEKEY,sn);
		if(tree==null) {
			tree=redisService.getObj(errorKey);
			log.info("从redis中获取异常缓存对象,redis key:{},tree:{}",errorKey,tree);
		}
		
		// 如果tree依然为空，则从数据库中加载
		if(tree==null) {
			String lockKey=String.format(CODE_TREE_LOCK_CACHEKEY,sn);
			boolean lock=redisService.putIfAbsent(lockKey, true, Duration.ofSeconds(3));
			log.info("从数据库中加载uid tree,sn:{},key:{},lock:{}",sn,lockKey,lock);
			
			if(lock) {
				log.debug("成功获取到全局key，开始从数据库中加载uid tree,sn:{}",sn);
				SysCodeTree tmp=new SysCodeTree();
				tmp.setSn(sn);
				tmp.setStatus(1);
				tmp.setDelFlag(0);
				
				tmp=dataBaseDao.findOne(SqlBuilder.build(tmp).where("sn=?"));
				log.debug("从数据库中加载code tree,sn:{},tree:{}",sn,tmp);
				
				if(tmp!=null) {
					tree=tmp;
					if(Objects.equal(tree.getStatus(), 1) && Objects.equal(tree.getDelFlag(), 0)) {
						log.info("设置redis缓存,redis key:{},tree key:{}",CODE_TREE_CACHEKEY,sn);
						redisService.putMap(CODE_TREE_CACHEKEY, sn, tree);
					}else{
						log.warn("从数据库中加载code tree,sn:{}成功,tree状态异常,status:{},delflag:{},设置redis假缓存,redis key:{}",
							sn,tmp.getStatus(),tmp.getDelFlag(),errorKey);
						redisService.put(errorKey, tmp, Duration.ofMinutes(CODE_ERROR_TIMELIEF));
					}
				}else {
					log.warn("从数据库中加载code tree,sn:{}失败,设置redis假缓存,redis key:{}",sn,errorKey);
					tmp=new SysCodeTree();
					redisService.put(errorKey, tmp, Duration.ofMinutes(CODE_ERROR_TIMELIEF));
				}
				
				// 删除锁
				redisService.delete(lockKey);
			}else {
				// 休息300毫秒
				ThreadUtil.safeSleep(300);
				tree = loadTree(sn, time-1);
			}
		}
		
		// 验证缓存有效性（假缓存）
		AssertUtil.service()
			.notNull(tree, "code tree未找到")
			.isTrue(tree.getId()!=null, "系统繁忙,请稍后再试")
			.notEq(tree.getStatus(), 1, "code tree已停用")
			.notEq(tree.getDelFlag(), 0, "code tree已删除");
		log.debug("退出:加载code tree方法,sn:{},time:{},tree:{}",sn,time,tree);
		return tree;
	}
	
	
	/**
	 * 	设置code tree缓存
	 * @param tree
	 */
	public void putTree(SysCodeTree tree) {
		AssertUtil.service().notNull(tree, "code Tree对象不能为空")
			      .isTrue(!StringUtils.isEmpty(tree.getSn()), "code Tree对象编码不能为空");
		log.debug("进入:设置code Tree缓存方法,sid:{},redis key:{},sn:{}",tree.getId(),CODE_TREE_CACHEKEY,tree.getSn());
		redisService.putMap(CODE_TREE_CACHEKEY, tree.getSn(), tree);
		log.debug("退出:设置code Tree缓存方法,sid:{},redis key:{},sn:{}",tree.getId(),CODE_TREE_CACHEKEY,tree.getSn());
	}
	
	
	/**
	 * 	移除uid tree缓存
	 * @param sn
	 */
	public void remTree(String sn) {
		AssertUtil.service().isTrue(!StringUtils.isEmpty(sn), "code Tree编码不能为空");
		log.debug("进入:移除code Tree缓存方法,redis key:{},sn:{}",CODE_TREE_CACHEKEY,sn);
		redisService.deleteMapHash(CODE_TREE_CACHEKEY, sn);
		log.debug("退出:移除code Tree缓存方法,redis key:{},sn:{}",CODE_TREE_CACHEKEY,sn);
	}
	

	/**
	 * 	加载code lvsn
	 * @param sn
	 * @return
	 */
	public SysCodeLvsn loadLvsn(String sn) {
		log.debug("进入:加载code lvsn方法,sn:{}",sn);
		SysCodeLvsn lsvn = this.loadLvsn(sn, 4);
		log.debug("退出:加载code lvsn方法,sn:{},lsvn:{}",sn,lsvn);
		return lsvn;
	}
	private SysCodeLvsn loadLvsn(String sn,int time) {
		log.debug("进入:加载code lvsn方法,sn:{},time:{}",sn,time);
		SysCodeTree tree = loadTree(sn);
		AssertUtil.service().isTrue(time>0, "系统繁忙,请稍后再试").notNull(tree, "code tree层级树定义信息未找到,sn:"+sn);
		
		//0全局，1租户级，2机构级 
		String lvsnRedisKey=this.buildLvsnRedistKey(tree);
				
		SysCodeLvsn lvsn=redisService.getMap(CODE_LVSN_CACHEKEY, lvsnRedisKey);
		log.info("从redis中获取缓存,redis key:{},lvsn sn:{},lvsn:{}",CODE_LVSN_CACHEKEY,lvsnRedisKey,lvsn);
		
		// 如果lvsn依然为空，则从数据库中加载
		if(lvsn==null) {
			String lockKey=String.format(CODE_LVSN_LOCK_CACHEKEY, lvsnRedisKey);
			boolean lock=redisService.putIfAbsent(lockKey, true, Duration.ofSeconds(3));
			log.info("从数据库中加载code lvsn,sn:{},key:{},lock:{}",sn,lockKey,lock);
			
			if(lock) {
				log.debug("成功获取到全局key，开始从数据库中加载code lvsn,sn:{}",sn);
				SysCodeLvsn tmp=new SysCodeLvsn();
				tmp.setTreeSn(sn);
				tmp.setLvType(tree.getTypes());
				if(tree.getTypes()!=null && tree.getTypes()!=0) {
					tmp.setTenantId(sessionService.getTenantId());
					if(tree.getTypes()==2) {
						tmp.setOrgId(sessionService.getOrgId());
					}
				}
				
				tmp=dataBaseDao.findOne(SqlBuilder.build(tmp));
				log.debug("从数据库中加载code lvsn,sn:{},lvsn:{}",sn,tmp);
				
				if(tmp==null) {
					log.warn("从数据库中加载code lvsn,sn:{}失败,自动生成记录",sn);
					tmp=new SysCodeLvsn();
					tmp.setTreeSn(sn);
					tmp.setTreeId(tree.getId());
					tmp.setLvType(tree.getTypes());
					tmp.setLvLen(tree.getLvLen());
					tmp.setCurrentMaxLv(0);
					tmp.setCurrentLvsn("");
					
					int len = dataBaseDao.insert(tmp);
					AssertUtil.service().isTrue(len>0, "保存层级编码记录失败");
				}
				// 初始化层级编码value
				this.initLvsnValue(tree,tmp);
				
				log.info("设置redis缓存,redis key:{},code lvsn key:{}",CODE_LVSN_CACHEKEY,lvsnRedisKey);
				redisService.putMap(CODE_LVSN_CACHEKEY, lvsnRedisKey, tmp);
				
				// 删除锁
				redisService.delete(lockKey);
				
				lvsn=tmp;
			}else {
				// 休息300毫秒
				ThreadUtil.safeSleep(300);
				lvsn = loadLvsn(sn, time-1);
			}
		}
		
		// 验证缓存有效性（假缓存）
		AssertUtil.service()
			.notNull(lvsn, "价值code lvsn层级信息失败")
			.isTrue(tree.getId()!=null, "系统繁忙,请稍后再试");
		log.debug("退出:加载code lvsn方法,sn:{},time:{},lvsn:{}",sn,time,tree);
		return lvsn;
	}
	
	
	/**
	 * 	设置code lvsn缓存
	 * @param lvsn
	 */
	public void putLvsn(SysCodeLvsn lvsn) {
		AssertUtil.service()
			.notNull(lvsn, "code lvsn对象不能为空")
			.isTrue(!StringUtils.isEmpty(lvsn.getTreeSn()), "code lvsn Tree编码不能为空");
		SysCodeTree tree = loadTree(lvsn.getTreeSn());
		AssertUtil.service()
				  .notNull(tree, "层级树定义信息未找到,sn:"+lvsn.getTreeSn());
		
		//0全局，1租户级，2机构级 
		String lvsnRedisKey=this.buildLvsnRedistKey(tree);
		
		// 初始化层级编码value
		this.initLvsnValue(tree,lvsn);
		
		log.debug("进入:设置code lvsn缓存方法,id:{},redis key:{},sn:{}",lvsn.getId(),CODE_LVSN_CACHEKEY,lvsnRedisKey);
		redisService.putMap(CODE_LVSN_CACHEKEY, lvsnRedisKey, lvsn);
		
		log.debug("退出:设置code lvsn缓存方法,id:{},redis key:{},sn:{}",lvsn.getId(),CODE_LVSN_CACHEKEY,lvsnRedisKey);
	}
	
	
	
	/**
	 * 	移除code lvsn缓存
	 * @param sn
	 */
	public void remLvsn(String sn) {
		AssertUtil.service().isTrue(!StringUtils.isEmpty(sn), "code tree对象编码不能为空");
		SysCodeTree tree = loadTree(sn);
		//0全局，1租户级，2机构级 
		String lvsnInfoKey=this.buildLvsnRedistKey(tree);
		
		// 删除层级value信息
		String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,lvsnInfoKey);
		redisService.delete(lvsnValueKey);
		
		
		log.debug("进入:移除code lvsn缓存方法,redis key:{},sn:{}",CODE_LVSN_CACHEKEY,lvsnInfoKey);
		redisService.deleteMapHash(CODE_LVSN_CACHEKEY, lvsnInfoKey);
		log.debug("退出:移除code lvsn缓存方法,redis key:{},sn:{}",CODE_LVSN_CACHEKEY,lvsnInfoKey);
	}


	
	/**
	 * 生成一个层级编码：根节点
	 * @param tree sn
	 * @return
	 */
	public String generate(String tsn) {
		return generate(tsn,"",0);
	}

	/**
	 * 	生成一个层级编码
	 * @param tree sn	树编码
	 * @param parent	为空，则是根，顶层
	 * @param lv		层级，0开始，只能逐级生成
	 * @return
	 */
	public String generate(String tsn,String parent,int lv) {
		log.debug("进入:生成一个层级编码方法,tree sn:{},parent:{},lv:{}",tsn,parent,lv);
		SysCodeLvsn lvsnObj = loadLvsn(tsn);
		AssertUtil.service().notNull(lvsnObj, "code lvsn层级编码对象未找到,sn:"+tsn);
		
		String lvsnInfoKey=buildLvsnRedistKey(lvsnObj);
		String lvsnValueKey=String.format(CODE_LVSN_VALUE_CACHEKEY,lvsnInfoKey);

		String format="%0"+lvsnObj.getLvLen()+"d";
		long size = redisService.getListOps().size(lvsnValueKey);
		AssertUtil.service().isTrue(lv>=0, "lv参数不能小于0")
			      .isTrue(lv<=size, "目前最大层级为:"+size);
		
		// 生产层级编码
		String lvsn=redisService.doHpdl(new HpdlProcess<String>(lvsnValueKey) {
			@Override
			public String process() {
				log.info("生成层级编码:开始,tree sn:{},parent:{},lv:{}",tsn,parent,lv);
				String sn=null;
				Object ovalue = redisService.getListValue(lvsnValueKey, lv);
				Long lvalue=1L;
				if(ovalue==null) {
					sn=String.format(format, lvalue);
					redisService.putListValue(lvsnValueKey, lvalue);
				}else {
					lvalue = Long.valueOf(ovalue.toString());
					lvalue=lvalue+1;
					sn = toStr(lvalue);
					if(sn.length()<lvsnObj.getLvLen()) {
						sn = String.format(format, 0).substring(0,lvsnObj.getLvLen()-sn.length())+sn;
					}
					redisService.putListValue(lvsnValueKey, lv, lvalue);
				}
				log.info("生成层级编码:完成,tree sn:{},parent:{},lv:{},lvsn:{}",tsn,parent,lv,sn);
				return String.format("%s%s", parent,sn);
			}
		}, 200, 30, 5);
		AssertUtil.service().notNull(lvsn, "层级编码生成失败");
		
		log.debug("退出:生成一个层级编码方法,tree sn:{},parent:{},lv:{},lvsn:{}",tsn,parent,lv,lvsn);
		return lvsn;
	}



}
