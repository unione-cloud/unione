package com.unione.cloud.web.service;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.web.model.dto.LoginParam;
import com.unione.cloud.web.model.dto.LoginResult;
import com.unione.cloud.web.model.dto.LoginUser;
import com.unione.cloud.web.util.LogsUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 	登录服务
 * @作者	Jeking Yang
 * @日期	2023年9月23日 下午11:29:27
 * @版本	1.0.0
 */
@Slf4j
@Service
public class LoginService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private RedisService redisService;
	
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
	@Value("${security.login.singleTip:账号[%s]已在其他设备登录}")
	private String LOGIN_SINGLETIP;
	
	/**
	 * 	登录失败提示信息
	 */
	@Value("${security.login.failur.tip:账号或密码不正确}")
	private String LOGIN_FAILURE_TIP;
	
	
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
	
	/**
	 * 判断用户帐号是否已被锁
	 * @param username
	 * @return
	 */
	public boolean isLocked(String username) {
		log.info("判断用户帐号是否已被锁,username:{}",username);
		if(!LOGIN_SINGLELIMIT) {
			log.warn("判断用户帐号是否已被锁,目前未开启，如需开启，请修改配置，ecurity.login.failLimit=true");
			return false;
		}
		Integer count=null;
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,Integer> cache=this.getCache();
			count=cache.get(username);
		}else {
			count=redisService.getObj(String.format("%s-%s", CACHE_NAME,username));
		}
		if(count!=null && count>=LOGIN_FAILCOUNT) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * 执行用户登录方
	 * @param param
	 * @return
	 */
	public LoginResult doLogin(LoginParam param) {
		log.info("进入：用户登录方法,username:{},captcha:{}",param.getUsername(),param.getCaptcha());
		LogsUtil.add("用户请求登录，username:%s,captcha:%s",param.getUsername(),param.getCaptcha());
		AssertUtil.service().notNull(param, new String[] {"username","password"},"请求参数%s不能为空");
		
		LoginResult result=null;
		
		LogsUtil.add("验证帐号是否已被锁,username:%s",param.getUsername());
		if(this.isLocked(param.getUsername())) {
			LogsUtil.add("帐号已被锁定，拒绝本次登录请求");
			return LoginResult.fail(LOGIN_FAILURE_TIP);
		}
		
		LogsUtil.add("加载帐号信息,username:%s",param.getUsername());
		LoginUser user=new LoginUser();
		user.setUsername(StringUtils.trim(param.getUsername()));
		SqlBuilder<LoginUser> builder=SqlBuilder.build(user).where("username=? OR tel=?");
		user=dataBaseDao.findUnique(builder);
		AssertUtil.service()
			.notNull(user, LOGIN_FAILURE_TIP)
			.notNull(user,new String[] {"pwdText","pwdSalt"}, LOGIN_FAILURE_TIP);
		
		
		
		
		
		
		log.info("退出：用户登录方法,username:{},captcha:{}",param.getUsername(),param.getCaptcha());
		return result;
	}
	
	

}
