package com.unione.cloud.portal.system.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.ValueSupplier;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.portal.system.dto.LoginParam;
import com.unione.cloud.portal.system.dto.LoginResult;
import com.unione.cloud.portal.system.model.SysRole;
import com.unione.cloud.portal.system.model.SysUser;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 	登录服务
 * @作者	Jeking Yang
 * @日期	2023年9月23日 下午11:29:27
 * @版本	1.0.0
 */
@Slf4j
@Service
@RefreshScope
public class LoginService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private CaptchaService captchaService;	
	
	
	/**
	 * 	缓存类型： ehcache/redis
	 */
	@Value("${security.cache.type:ehcache}")
	private String CACHE_TYPE;
	
	/**
	 * 缓存名称
	 */
	@Value("${security.login.cache.name:security-login-cache}")
	private String CACHE_NAME;
	
	/**
	 * 缓存内存大小：单位M，默认20M
	 */
	@Value("${security.login.cache.memory:20}")
	private int    CACHE_MEMORY;
	
	/**
	 * 缓存时间：单位秒，默认7200秒
	 */
	@Value("${security.login.failLimitTime:7200}")
	private int    CACHE_TIME;
	
	/**
	 * 用户登录失败限制开关，默认true
	 */
	@Value("${security.login.failLimit:true}")
	private boolean LOGIN_FAILLIMITE;

	/**
	 * 用户登录失败次数，默认5次
	 */
	@Value("${security.login.failCount:5}")
	private int LOGIN_FAILCOUNT;
	
	/**
	 * 启用单端登录验证，默认不开启
	 */
	@Value("${security.login.singleLimit:false}")
	private boolean LOGIN_SINGLELIMIT;
	
	/**
	 * 	单端登录验证提示信息
	 */
	@Value("${security.login.singleTip:账号[{username}]已在其他设备登录}")
	private String LOGIN_SINGLETIP;
	
	/**
	 * 	登录失败提示信息
	 */
	@Value("${security.login.failur.tip:账号或密码不正确}")
	private String LOGIN_FAILURE_TIP;
	
	/**
	 * 	帐号禁止提示信息
	 */
	@Value("${security.login.failur.forbid:当前帐号已被{forbid}}")
	private String LOGIN_FAILURE_FORBID;
	
	/**
	 * 	登录失败：提示信息
	 */
	@Value("${security.login.failur.limittip:帐号或密码错误{failureCount}次，连续{totalCount}次错误帐号将被锁}")
	private String LOGIN_FAILURE_LLIMITETIP;
	
	/**
	 * 	登录失败：帐号被锁提示信息
	 */
	@Value("${security.login.failur.limited:帐号或密码错误{failureCount}次，请在{LimitTime}后再试}")
	private String LOGIN_FAILURE_LLIMITEED;
	
    /**
     * 	Token Center Manage 令牌中心化管理，key前缀
     */
    @Value("${security.tcm.key:TOKEN}")
    private String tcmKey;
    
    /**
     * 	Token Center Manage 令牌中心化管理，redis 数据库，默认：10
     */
    @Value("${security.tcm.db:10}")
    private int     tcmDb;
	
	/**
	 *  	登录允许状态：用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
	 */
	@Value("${security.login.status.allow:1}")
	private List<Integer> LOGIN_STATUS_ALLOW=new ArrayList<>();
	
	private Map<Integer, String> LOGIN_STATUS_MAP=new HashMap<>();
	/**
	 * 	用户登录状态定义：用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
	 */
	@SuppressWarnings("unchecked")
	@Value("${security.login.status.maps:{1:\"正常\",2:\"禁用\",3:\"注销\",4:\"锁定\"}}")
	public void setLoingStatusMap(String maps) {
		LOGIN_STATUS_MAP=new HashMap<>();
		Map<String,String> map=JSONUtil.toBean(maps, Map.class);
		map.entrySet().stream().forEach(entry->{
			LOGIN_STATUS_MAP.put(Integer.parseInt(entry.getKey()),entry.getValue());
		});
	}
	
	
	
	/**
	 * 	获得缓存对象 ehcache
	 * @return
	 */
	private Cache<String,Integer> getCache() {
		Cache<String,Integer> cache=cacheManager.getCache(CACHE_NAME,String.class,Integer.class);
		if(cache==null) {
			CacheConfiguration<String,Integer> config=CacheConfigurationBuilder.newCacheConfigurationBuilder(
					 String.class,
					 Integer.class,
					 ResourcePoolsBuilder.newResourcePoolsBuilder()
                     // 磁盘存储,记得添加true，才能正常的持久化，并且序列化以及反序列化
                     .disk(CACHE_MEMORY,MemoryUnit.MB, true)
                     .build())
					 .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(CACHE_TIME)))
					 .build();
			cache=cacheManager.createCache(CACHE_NAME,config);
		}
		return cache;
	}
	
	
	@SuppressWarnings("deprecation")
	public Date getLimitTime(String username) {
		Long limit=null;
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,Integer> cache=this.getCache();
			Integer count=cache.get(username);
			org.ehcache.expiry.Duration duration = cache.getRuntimeConfiguration().getExpiry().getExpiryForAccess(username, new ValueSupplier<Integer>() {
				@Override
				public Integer value() {
					return count;
				}
			});
			if(duration!=null) {
				limit=duration.getLength();
			}
		}else {
			limit=redisService.getExpire(String.format("%s-%s", CACHE_NAME,username));
		}
		if(limit!=null) {
			return DateUtil.date().offset(DateField.SECOND, limit.intValue());
		}
		return null;
	}
	
	/**
	 * 	获得帐号登录失败次数
	 * @param username
	 * @return
	 */
	public int getFailure(String username) {
		Integer count=null;
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,Integer> cache=this.getCache();
			count=cache.get(username);
		}else {
			count=redisService.getObj(String.format("%s-%s", CACHE_NAME,username));
		}
		return count!=null?count:0;
	}
	
	
	
	/**
	 * 	登录错误累计次数，超出限制后
	 * @param username
	 * @return
	 */
	private int incFailure(String username) {
		LogsUtil.add("认证失败，累计该帐号失败次数,username:%s",username);
		
		Integer count=null;
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,Integer> cache=this.getCache();
			count=cache.get(username);
			if(count==null) {
				count=0;
			}
			count=count+1;
			cache.put(username, count);
		}else {
			Long value=redisService.getObjOps().increment(String.format("%s-%s", CACHE_NAME,username));
			count=value.intValue();
		}
		// 如果已经超过阀值，则更新用户锁定时间
		if(count>=LOGIN_FAILCOUNT && LOGIN_FAILLIMITE) {
			Long lockTime=DateUtil.date().offset(DateField.SECOND, CACHE_TIME).getTime();
			SysUser user=SysUser.builder().username(username).lockTime(lockTime).build();
			SqlBuilder<SysUser> builder=SqlBuilder
					.build(user).field("lockTime").where("username=?");
			int len = dataBaseDao.update(builder);
			LogsUtil.add("更新用户锁定时间，username:%s,len:%s",username,len);
		}
		return count;
	}
	
	/**
	 * 	清空登录错误次数
	 * @param username
	 */
	public void cleanFailure(String username) {
		LogsUtil.add("清空登录错误次数,username:%s",username);
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,Integer> cache=this.getCache();
			cache.remove(username);
		}else {
			redisService.delete(String.format("%s-%s", CACHE_NAME,username));
		}
	}
	
	/**
	 * 执行用户登录方
	 * @param param
	 * @return
	 */
	public LoginResult doLogin(LoginParam param) {
		log.info("进入：用户登录方法,username:{},captcha:{}",param.getUsername(),param.getCaptcha());
		LogsUtil.add("用户请求登录，username:%s,captcha:%s",param.getUsername(),param.getCaptcha());
		AssertUtil.service()
			.notNull(param, new String[] {"username","password"},"请求参数%s不能为空")
			.isTrue(captchaService.validate(param.getCaptcha()), "验证码不正确");
				
		LogsUtil.add("验证帐号是否已被锁,username:%s",param.getUsername());
		int failureCount=this.getFailure(param.getUsername());
		if(failureCount>=LOGIN_FAILCOUNT && LOGIN_FAILLIMITE) {
			LogsUtil.add("帐号已被锁定，拒绝本次登录请求");
			Date expire=this.getLimitTime(param.getUsername());
			if(expire!=null) {
				return LoginResult.fail(LOGIN_FAILURE_LLIMITEED
						.replace("{failureCount}", failureCount+"")
						.replace("{LimitTime}", DateUtil.format(expire, "yyyy-MM-dd HH:mm")));
			}
		}
		
		LogsUtil.add("加载帐号信息,username:%s",param.getUsername());
		SysUser user=new SysUser();
		user.setUsername(StringUtils.trim(param.getUsername()));
		SqlBuilder<SysUser> builder=SqlBuilder
				.build(user)
				.query("SELECT u.*,org.ID as orgId,org.NAME as orgName,org.AREA_CODE as areaCode FROM SYS_USER u LEFT JOIN SYS_ORGAN org on u.ORG_ID=org.ID WHERE u.USERNAME=#{params.username}");
		user=dataBaseDao.findOne(builder);
		if(user==null) {
			LogsUtil.add("认证失败：帐号不正确");
			return this.loginFailure(param.getUsername());
		}
		
		if(!LOGIN_STATUS_ALLOW.contains(user.getStatus())) {
			String text=LOGIN_STATUS_MAP.get(user.getStatus());
			if(StringUtils.isEmpty(text)) {
				text="禁用";
			}
			LogsUtil.add("认证失败：帐号已%s",text);
			return LoginResult.fail(LOGIN_FAILURE_FORBID.replace("{forbid}", text));
		}
		
		if(StringUtils.isEmpty(user.getPwdText())) {
			LogsUtil.add("认证失败：帐号密码为空");
			return this.loginFailure(param.getUsername());
		}
		
		if(StringUtils.isEmpty(user.getPwdSalt())) {
			LogsUtil.add("认证失败：密码盐为空");
			return this.loginFailure(param.getUsername());
		}
		
		LogsUtil.add("使用用户密码盐对输入密码进行加密并判断和密码是否一致");
		String pwd = SmUtil.sm4(Base64.decode(user.getPwdSalt())).encryptHex(param.getPassword());
		if(ObjectUtil.notEqual(pwd, user.getPwdText())) {
			LogsUtil.add("认证失败：密码不正确");
			return this.loginFailure(param.getUsername());
		}
		
		// 密码正确
		this.cleanFailure(param.getUsername());	//清空失败信息
		
		// 单设备登录
		if(LOGIN_SINGLELIMIT) {
			// 如果开启了单设备登录
			boolean flag = redisService.execute(tcmDb,(RedisCallback<Boolean>) action->{
				ScanOptions options = ScanOptions.scanOptions().match(String.format("%s:%s:*",tcmKey,param.getUsername())).build();
				Cursor<byte[]> cursor = action.scan(options);
				return cursor.hasNext();
			});
			AssertUtil.service().isTrue(!flag, LOGIN_SINGLETIP.replace("{username}", param.getUsername()));
		}
			
		log.info("退出：用户登录方法,username:{},captcha:{}",param.getUsername(),param.getCaptcha());
		// 构建认证信息
		return this.loginSuccess(user);
	}
	
	
	/**
	 * 	登录成功
	 * @param user
	 * @return
	 */
	private LoginResult loginSuccess(SysUser user) {
		LogsUtil.add("用户登录成功，username:%s",user.getUsername());
		
		// 实例化用户认证对象
		UserPrincipal principal=new UserPrincipal();
		BeanUtil.copyProperties(user, principal);
		principal.setAvatar(user.getPortrait());
		
		// 加载用户角色
		SysRole role=new SysRole();
		role.setUserId(user.getId());
		SqlBuilder<SysRole> loadUserRole=SqlBuilder
				.build(role).query("SELECT r.CODES FROM SYS_USER_ROLE ur LEFT JOIN SYS_ROLE r ON ur.ROLE_ID=r.ID WHERE r.STATUS=1 AND ur.USER_ID=#{params.userId}");
		List<SysRole> roles = dataBaseDao.findList(loadUserRole);
		Set<String> roleCodes = roles.stream().map(row->row.getCodes()).filter(row->row!=null).collect(Collectors.toSet());
		principal.setUserRoles(new ArrayList<>(roleCodes));
		LogsUtil.add("加载用户角色,role list:%s",roleCodes);
		
		// 更新用户登录信息
		user.setLastLoginIp(LogsUtil.getClientIp());
		user.setLastLoginTime(DateUtil.date().getTime());
		SqlBuilder<SysUser> updater=SqlBuilder
				.build(user).field("lastLoginTime,lastLoginIp").where("id=?");
		int len = dataBaseDao.update(updater);
		LogsUtil.add("更新用户登录信息,user id:%s,len:%s",user.getId(),len);
		
		LogsUtil.add("生成认证令牌并返回结果");
		String token=tokenService.build4auth(principal);
		return LoginResult.success(token, principal);
	}
	
	/**
	 * 	登录失败
	 * @param username
	 * @return
	 */
	private LoginResult loginFailure(String username) {
		
		int failureCount=this.incFailure(username);
		LogsUtil.add("该帐号错误次数为：%s,限制次数为：%s，限制开关：%s",failureCount,LOGIN_FAILCOUNT,LOGIN_FAILLIMITE);
		
		// 输出错误提示
		if(LOGIN_FAILLIMITE) {
			if(failureCount>=LOGIN_FAILCOUNT) {
				Date expire=this.getLimitTime(username);
				if(expire!=null) {
					return LoginResult.fail(LOGIN_FAILURE_LLIMITEED
							.replace("{failureCount}", failureCount+"")
							.replace("{LimitTime}", DateUtil.format(expire, "yyyy-MM-dd HH:mm")));
				}
			}
			return LoginResult.fail(LOGIN_FAILURE_LLIMITETIP
					.replace("{failureCount}", failureCount+"")
					.replace("{totalCount}", LOGIN_FAILCOUNT+""));
		}else {
			return LoginResult.fail(LOGIN_FAILURE_TIP);
		}
	}
	
}
