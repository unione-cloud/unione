package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.system.dto.TenantInfoDto;
import com.unione.cloud.system.dto.UserRoleDto;
import com.unione.cloud.system.model.SysRole;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.model.SysUserRole;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TenantService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
	private CacheManager cacheManager;

	@Autowired
	private RedisService redisService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SecretService secretService;



	@Value("${unione.cache.tenant.expire:72000}")
	private long CACHE_TIME;

	private Cache<Long, SysTenant> getCache(){
		return cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:TENANT:ID:")
			.cacheType(CacheType.BOTH)
			.cacheNullValue(true)
			.expire(Duration.ofSeconds(CACHE_TIME))
			.localExpire(Duration.ofSeconds(30))
			.build());
	}
	
	private Cache<String, Long> getCache2(){
		return cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:TENANT:NAME:")
			.cacheType(CacheType.BOTH)
			.cacheNullValue(true)
			.expire(Duration.ofSeconds(CACHE_TIME))
			.localExpire(Duration.ofSeconds(30))
			.build());
	} 
	
	/**
	 * 	加载租户信息
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:TENANT",key = "#id",expire = 600,cacheType = CacheType.LOCAL,cacheNullValue = true)
	public SysTenant loadTenant(Long id) {
		log.debug("进入：加载租户信息方法，缓存未命中，从db中加载,id:{}",id);
		Cache<Long, SysTenant> cache = getCache();
		SysTenant target = cache.get(id);
		if(target == null) {
			target = redisService.doHpdl(new HpdlProcess<SysTenant>(String.format("hpdl:tenant:%s",id)) {
				@Override
				public SysTenant process() {
					SysTenant tenant = cache.get(id);
					if(tenant == null) {
						tenant = dataBaseDao.findById(SqlBuilder.build(SysTenant.class).id(id));
						if(tenant==null){
							tenant = new SysTenant();
						}else{
							getCache2().put(tenant.getName(), id);
						}
						cache.put(id, tenant);
					}
					return tenant;
				}
			}, 500, 3);
		}
		AssertUtil.service().notNull(target, "租户信息不存在").notNull(target.getId(), "租户id不存在");
		log.debug("退出：加载租户信息方法，缓存未命中，从db中加载,id:{},result:{}",id,target);
		return target;
	}
	
	/**
	 * 	加载租户信息
	 * @param name
	 * @return
	 */
	public SysTenant loadTenant(String name){
		AssertUtil.service().notNull(name, "租户名称不能为空");
		Cache<String, Long> cache = getCache2();
		Long id=cache.get(name);
		if(id==null){
			SysTenant tenant=redisService.doHpdl(new HpdlProcess<SysTenant>(String.format("hpdl:tenant:%s",name)) {
				@Override
				public SysTenant process() {
					Long tmp=cache.get(name);
					if(tmp!=null){
						return loadTenant(tmp);
					}
					SysTenant tenant=dataBaseDao.findOne(SqlBuilder.build(SysTenant.class)
						.where("name=?")
						.where("name", name.trim()));
					if(tenant!=null){
						cache.put(tenant.getName(), tenant.getId());
						getCache().put(tenant.getId(), tenant);
					}else{
						cache.put(name, -1L);
					}
					return tenant;
				}
			}, 500, 3);
			return tenant;
		}
		if(id==-1L){
			return null;
		}
		return loadTenant(id);
	}
	
	/**
	 * 	加载租户信息
	 * @param ids
	 * @return
	 */
	public Map<Long, SysTenant> loadTenant(Set<Long> ids) {
		log.debug("进入：加载租户信息方法,ids:{}",ids);
		if(ObjectUtil.isEmpty(ids)) {
			return MapUtil.empty();
		}
		
		Cache<Long, SysTenant> cache = getCache();
		Map<Long, SysTenant> map = cache.getAll(ids);
		
		List<Long> idList = ids.stream().filter(id->!map.keySet().contains(id)).collect(Collectors.toList());
		if(!idList.isEmpty()) {
			redisService.doHpdl(new HpdlProcess<Void>(String.format("hpdl:tenant:%s", sessionService.getUserId())) {
				@Override
				public Void process() {
					List<Long> uids=new ArrayList<>();
					idList.forEach(id->{
						SysTenant tenant=cache.get(id);
						if(tenant!=null){
							if(tenant.getId()!=null){
								map.put(id, tenant);
							}
						}else{
							uids.add(id);
						}
					});
					if(!uids.isEmpty()){
						dataBaseDao.findByIds(SqlBuilder.build(SysTenant.class).ids(uids))
						.stream().forEach(row->{
							map.put(row.getId(), row);
							cache.put(row.getId(), row);
							getCache2().put(row.getName(), row.getId());
						});
					}
					return null;
				}
			}, 500, 3);
			dataBaseDao.findByIds(SqlBuilder.build(SysTenant.class).ids(idList))
			.stream().forEach(row->{
				map.put(row.getId(), row);
				cache.put(row.getId(), row);
			});
		}
		log.debug("退出：加载租户信息方法,ids:{},result len:{}",ids,map.size());
		return map;
	}

	/**
	 * 	清除租户信息缓存
	 * @param id
	 */
	public void clear(long id){
		Cache<Long, SysTenant> cache = getCache();
		SysTenant tenant=cache.get(id);
		if(tenant!=null){
			getCache2().remove(tenant.getName());
		}
		cache.remove(id);
	}


	@Transactional(rollbackFor = Exception.class)
	public Results<Long> save(TenantInfoDto entity){
		AssertUtil.service().notNull(entity, new String[]{"sn","name","linkMan","linkTel","status"},"租户%s不能为空");

		// 验证租户编码、租户名称是否已存在
		SysTenant tenant=dataBaseDao.findOne(SqlBuilder.build(SysTenant.class).where("sn=? or name=?")
			.where("sn",entity.getSn())
			.where("name",entity.getName()));
		if(tenant!=null){
			if(entity.getId()==null || !ObjectUtil.equal(entity.getId(),tenant.getId())){
				StringBuffer buf=new StringBuffer();
				if(entity.getSn().equals(tenant.getSn())){
					buf.append(String.format("编码%s,", entity.getSn()));
				}
				if(entity.getName().equals(tenant.getName())){
					buf.append(String.format("名称%s,", entity.getName()));
				}
				return Results.failure(String.format("租户%s已存在", buf.subSequence(0, buf.length()-1)));
			}
		}

		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			if(ObjectUtil.isEmpty(entity.getPassword())){
				return Results.failure("登录密码不能为空");
			}
			entity.setId(IdGenHolder.generate());

			// 验证租户管理员帐号是否已存在
			SysUser admin=dataBaseDao.findOne(SqlBuilder.build(SysUser.class).where("username=? or tel=?")
				.where("username",entity.getSn())
				.where("tel",entity.getLinkTel())
				.dataPermis(PermisRule.ALL));
			if(admin!=null){
				StringBuffer buf=new StringBuffer();
				if(entity.getSn().equals(admin.getUsername())){
					buf.append(String.format("帐号%s,", entity.getSn()));
				}
				if(entity.getLinkTel().equals(admin.getTel())){
					buf.append(String.format("手机号%s,", entity.getLinkTel()));
				}
				return Results.failure(String.format("管理员%s已存在", buf.subSequence(0, buf.length()-1)));
			}

			// 初始化管理员帐号
			admin=new SysUser();
			admin.setUserType(1);
			admin.setUsername(entity.getSn());
			admin.setRealName(entity.getLinkMan());
			admin.setOrgId(-1L);
			admin.setTenantId(entity.getId());
			admin.setPwdSalt(RandomUtil.randomString(16));
			admin.setDelFlag(0);
			admin.setStatus(1);	//用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定	
			admin.setTel(entity.getLinkTel());
			String password=secretService.decrypt(entity.getPassword());
			password = SmUtil.sm4(admin.getPwdSalt().getBytes()).encryptHex(password);
			admin.setPwdText(password);
			dataBaseDao.insert(admin);

			// 保存角色
			if(!ObjectUtil.isEmpty(entity.getRoleList())){
				List<String> roleList=Stream.of(entity.getRoleList().split(",")).collect(Collectors.toList());
				List<SysRole> roles = dataBaseDao.findList(SqlBuilder.build(SysRole.class).where("sn in [sns]").where("sns",roleList));
				for(SysRole role:roles){
					SysUserRole userRole=new SysUserRole();
					userRole.setTenantId(entity.getId());
					userRole.setUserId(admin.getId());
					userRole.setRoleId(role.getId());
					userRole.setEnDilivery(0);
					dataBaseDao.insert(userRole);
				}
			}

			// 保存租户
			entity.setAdminId(admin.getId());
			entity.setRegisteWay(2);
			len = dataBaseDao.insertWithId(entity);
		}else {
			String[] fields = {"domain","logo","loginAd","linkMan","linkAdd","linkTel","locationCity","locationProvince","openTime","maxUserCount","maxUserOnline","maxOrganCount","maxOrganUserCouint","timeLimitStart","timeLimitEnd","status","descs"};
			SqlBuilder<TenantInfoDto> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
			this.clear(entity.getId());

			if(tenant!=null && tenant.getAdminId()!=null){
				// 加载管理员账户
				SysUser admin=dataBaseDao.findById(SqlBuilder.build(SysUser.class,tenant.getAdminId()).dataPermis(PermisRule.ALL));
				if(admin==null){
					return Results.failure(String.format("租户管理员帐号%s不存在", entity.getSn()));
				}

				List<String> fieldList=new ArrayList<>();
				// 更新管理员账户密码
				if(!ObjectUtil.isEmpty(entity.getPassword())){
					String password=secretService.decrypt(entity.getPassword());
					password = SmUtil.sm4(admin.getPwdSalt().getBytes()).encryptHex(password);
					admin.setPwdText(password);
					fieldList.add("pwdText");
				}
				if(!ObjectUtil.equal(admin.getStatus(), entity.getStatus())){
					fieldList.add("status");
					if(ObjectUtil.equal(entity.getStatus(), 3)){
						// 关闭租户，禁用管理员帐号
						admin.setStatus(2);
					}else{
						// 开启租户，启用管理员帐号
						admin.setStatus(1);
					}
				}
				if(!fieldList.isEmpty()){
					dataBaseDao.updateById(SqlBuilder.build(admin).field(fieldList.toArray(new String[0])));
				}

				// 管理员角色处理
				if(ObjectUtil.isEmpty(entity.getRoleList())){
					// 清空管理员角色
					SysUserRole ur=new SysUserRole();
					ur.setUserId(admin.getId());
					int len2 = dataBaseDao.delete(SqlBuilder.build(ur));
					LogsUtil.add(String.format("清空租户管理员角色，len:%s",len2));
				}else{
					// 修改管理员角色列表
					List<UserRoleDto> list=dataBaseDao.findList("loadUserRoleList",SqlBuilder.build(UserRoleDto.class,List.of(admin.getId())));
					Map<String,UserRoleDto> roleMap=list.stream().collect(Collectors.toMap(UserRoleDto::getRoleSn, Function.identity()));
					Set<String> roleSet=Stream.of(entity.getRoleList().split(",")).collect(Collectors.toSet());
					Set<String> addRoles=new HashSet<>();
					for(String roleSn:roleSet){
						if(!roleMap.containsKey(roleSn)){
							addRoles.add(roleSn);
							roleMap.remove(roleSn);
						}
					}

					// 删除管理员角色
					if(!roleMap.isEmpty()){
						int len2 = dataBaseDao.deleteById(SqlBuilder.build(SysUserRole.class,roleMap.values().stream().map(r->r.getId()).collect(Collectors.toList())));
						LogsUtil.add(String.format("删除租户管理员角色，roles:%s,len:%s",roleMap.keySet(),len2));
					}
					// 新增管理员角色
					if(!addRoles.isEmpty()){
						List<SysRole> roles = dataBaseDao.findList(SqlBuilder.build(SysRole.class).where("sn in [sns]").where("sns",addRoles));
						for(SysRole role:roles){
							SysUserRole userRole=new SysUserRole();
							userRole.setTenantId(entity.getId());
							userRole.setUserId(admin.getId());
							userRole.setRoleId(role.getId());
							userRole.setEnDilivery(0);
							dataBaseDao.insert(userRole);
						}
						LogsUtil.add(String.format("新增租户管理员角色，roles:%s",addRoles));
					}
				}

			}

		}
		
		return Results.build(len>0, entity.getId());
	}


}
