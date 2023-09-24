package com.unione.cloud.web.service;

import java.time.Duration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 验证码服务
 * 
 * @作者	Jeking Yang
 * @日期	2023年9月24日 下午9:29:27
 * @版本	1.0.0
 **/
@Slf4j
@Service
@RefreshScope
public class CaptchaService {
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private HttpServletRequest request;
	
	
	@Value("${captcha.width:150}")
	private int width;
	
	@Value("${captcha.height:50}")
	private int height;
	
	/**
	 * 验证码使用状态，默认开启
	 */
	@Value("${captcha.enable:true}")
	private boolean ENABLE;
	
	/**
	 * 	缓存类型： ehcache/redis
	 */
	@Value("${captcha.cache.type:ehcache}")
	private String CACHE_TYPE;
	
	/**
	 * 缓存名称
	 */
	@Value("${captcha.cache.name:captcha-cache}")
	private String CACHE_NAME;
	
	/**
	 * 缓存内存大小：单位M，默认10M
	 */
	@Value("${captcha.cache.memory:10}")
	private int    CACHE_MEMORY;
	
	/**
	 * 缓存时间：单位秒，默认300秒
	 */
	@Value("${captcha.cache.time:300}")
	private int    CACHE_TIME;
	
	
	
	
	/**
	 * 	获得缓存对象 ehcache
	 * @return
	 */
	private Cache<String,String> getCache() {
		Cache<String,String> cache=cacheManager.getCache(CACHE_NAME,String.class,String.class);
		if(cache==null) {
			CacheConfiguration<String,String> config=CacheConfigurationBuilder.newCacheConfigurationBuilder(
					 String.class,
					 String.class,
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
	 * 创建验证码
	 * @param scene
	 * @return
	 */
	public AbstractCaptcha create() {
		log.debug("进入->创建验证码");
		AbstractCaptcha captcha=CaptchaUtil.createGifCaptcha(width, height);
		
		String captchaid=UUID.randomUUID().toString();
		// 设置cookie
		Cookie ck=new Cookie("CAPTCHAID",captchaid);
		ck.setPath("/");
		ck.setHttpOnly(true);
		response.addCookie(ck);
		
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,String> cache=this.getCache();
			cache.put(captchaid, captcha.getCode());
		}else {
			redisService.put(String.format("%s-%s", CACHE_NAME,captchaid), captcha.getCode(), Duration.ofSeconds(CACHE_TIME));
		}
		
		return captcha;
	}
	
	
	/**
	 * 匹配验证码
	 * @param captcha
	 * @return
	 */
	public boolean validate(String captcha) {
		log.debug("进入->匹配验证码,code:{}",captcha);
		if(!ENABLE) {
			log.warn("验证码状态未开启，如需开启请修改配置,captcha.enable=true");
			return true;
		}
		
		if(StringUtils.isEmpty(captcha)) {
			return false;
		}
		
		// 从cookie中获取
		String captchaid=null;
		if(request.getCookies()!=null) {
			for(int i=0;i<request.getCookies().length;i++) {
				Cookie ck=request.getCookies()[i];
				if(ck!=null && "captchaId".equals(ck.getName())){
					captchaid=ck.getValue();
					break;
				}
			}
		}
		AssertUtil.service().notNull(captchaid, "请求参数不完整，请重新获取验证码");
		
		String code=null;
		if("ehcache".equalsIgnoreCase(CACHE_TYPE)) {
			Cache<String,String> cache=this.getCache();
			code=cache.get(captchaid);
			cache.remove(captchaid);
		}else {
			code=redisService.getObj(String.format("%s-%s", CACHE_NAME,captchaid));
			if(!StringUtils.isEmpty(code)) {
				redisService.delete(String.format("%s-%s", CACHE_NAME,captchaid));
			}
		}
		
		// 删除cookie中
		Cookie ck=new Cookie("CAPTCHAID","");
		ck.setPath("/");
		ck.setHttpOnly(true);
		ck.setMaxAge(0);
		response.addCookie(ck);
		
		return ObjectUtil.equal(code, captcha);
	}
	
	

}
