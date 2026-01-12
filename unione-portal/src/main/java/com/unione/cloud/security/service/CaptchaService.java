package com.unione.cloud.security.service;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.core.exception.AssertUtil;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	private HttpServletResponse response;
	
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * 验证码：宽度
	 */
	@Value("${security.captcha.width:150}")
	private int WIDTH;
	
	/**
	 * 验证码：高度
	 */
	@Value("${security.captcha.height:50}")
	private int HEIGHT;
	
	/**
	 * 验证码：字符数量
	 */
	@Value("${security.captcha.codecount:6}")
	private int CODECOUNT;
	
	/**
	 * 验证码使用状态，默认开启
	 */
	@Value("${security.captcha.enable:true}")
	private boolean ENABLE;
	
	
	/**
	 * 缓存时间：单位秒，默认300秒
	 */
	@Value("${security.captcha.cache.time:300}")
	private int    CACHE_TIME;
	
	
	
	
	/**
	 * 	获得缓存对象 
	 * @return
	 */
	private Cache<String,String> getCache() {
		QuickConfig config = QuickConfig.newBuilder("SYS:SECURITY:CAPTCHA:")
		    .expire(Duration.ofSeconds(CACHE_TIME))
		    .cacheType(CacheType.REMOTE)
		    .build();
		return cacheManager.getOrCreateCache(config);
	}
	
	
	/**
	 * 创建验证码
	 * @param scene
	 * @return
	 */
	public AbstractCaptcha create() {
		log.debug("进入->创建验证码");
		AbstractCaptcha captcha=CaptchaUtil.createGifCaptcha(WIDTH, HEIGHT,CODECOUNT);
		
		String captchaid=UUID.randomUUID().toString();
		// 设置cookie
		Cookie ck=new Cookie("CAPTCHAID",captchaid);
		ck.setPath("/");
		ck.setHttpOnly(true);
		response.addCookie(ck);
		
		Cache<String,String> cache=this.getCache();
		cache.put(captchaid, captcha.getCode());
		
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
			log.warn("验证码状态未开启，如需开启请修改配置,security.captcha.enable=true");
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
				if(ck!=null && "CAPTCHAID".equalsIgnoreCase(ck.getName())){
					captchaid=ck.getValue();
					break;
				}
			}
		}
		if(ObjectUtil.isEmpty(captchaid)){
			captchaid=request.getHeader("CAPTCHAID");
		}
		AssertUtil.service().notNull(captchaid, "请重新获取验证码");
		
		Cache<String,String> cache=this.getCache();
		String code=cache.get(captchaid);
		cache.remove(captchaid);
		
		// 删除cookie中
		Cookie ck=new Cookie("CAPTCHAID","");
		ck.setPath("/");
		ck.setHttpOnly(true);
		ck.setMaxAge(0);
		response.addCookie(ck);
		
		return captcha.equalsIgnoreCase(code);
	}
	
	

}
