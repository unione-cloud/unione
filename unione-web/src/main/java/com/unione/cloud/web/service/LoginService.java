package com.unione.cloud.web.service;

import java.time.Duration;
import java.util.Date;

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
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.web.model.dto.LoginParam;
import com.unione.cloud.web.model.dto.LoginResult;
import com.unione.cloud.web.model.dto.LoginUser;
import com.unione.cloud.web.util.LogsUtil;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
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
	@Value("${security.login.singleTip:账号[{username}]已在其他设备登录}")
	private String LOGIN_SINGLETIP;
	
	/**
	 * 	登录失败提示信息
	 */
	@Value("${security.login.failur.tip:账号或密码不正确}")
	private String LOGIN_FAILURE_TIP;
	
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
	
//	/**
//	 * 判断用户帐号是否已被锁
//	 * @param username
//	 * @return
//	 */
//	public boolean isLocked(String username) {
//		log.info("判断用户帐号是否已被锁,username:{}",username);
//		if(!LOGIN_SINGLELIMIT) {
//			log.warn("判断用户帐号是否已被锁,目前未开启，如需开启，请修改配置，ecurity.login.failLimit=true");
//			return false;
//		}
//		Integer count=this.getFailure(username);
//		if(count!=null && count>=LOGIN_FAILCOUNT) {
//			return true;
//		}
//		return false;
//	}
	
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
			count=redisService.getObj(String.format("%s-%s", CACHE_NAME,username));
		}
		return count;
	}
	
	/**
	 * 	清空登录错误次数
	 * @param username
	 */
	public void cleanFailure(String username) {
		
		
		
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
		LoginUser user=new LoginUser();
		user.setUsername(StringUtils.trim(param.getUsername()));
		SqlBuilder<LoginUser> builder=SqlBuilder.build(user).where("username=? OR tel=?");
		user=dataBaseDao.findUnique(builder);
		AssertUtil.service()
			.notNull(user, LOGIN_FAILURE_TIP)
			.notNull(user,new String[] {"pwdText","pwdSalt"}, LOGIN_FAILURE_TIP);
		
		LogsUtil.add("使用用户密码盐对输入密码进行加密并判断和密码是否一致");
		String pwd = SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(param.getPassword());
		if(ObjectUtil.notEqual(pwd, user.getPwdText())) {
			LogsUtil.add("帐号或密码不正确，累计该帐号错误次数");
			failureCount=this.incFailure(param.getUsername());
			LogsUtil.add("该帐号错误次数为：%s,限制次数为：%s，限制开关：%s",failureCount,LOGIN_FAILCOUNT,LOGIN_FAILLIMITE);
			
			// 输出错误提示
			if(LOGIN_FAILLIMITE) {
				if(failureCount>=LOGIN_FAILCOUNT) {
					return LoginResult.fail(LOGIN_FAILURE_LLIMITEED
							.replace("{failureCount}", failureCount+"")
							.replace("{totalCount}", LOGIN_FAILCOUNT+""));
				}
				return LoginResult.fail(LOGIN_FAILURE_LLIMITETIP
						.replace("{failureCount}", failureCount+"")
						.replace("{totalCount}", LOGIN_FAILCOUNT+""));
			}else {
				return LoginResult.fail(LOGIN_FAILURE_TIP);
			}
		}else {
			// 密码正确
			
		}
		
		
		log.info("退出：用户登录方法,username:{},captcha:{}",param.getUsername(),param.getCaptcha());
		return result;
	}
	
	

}
