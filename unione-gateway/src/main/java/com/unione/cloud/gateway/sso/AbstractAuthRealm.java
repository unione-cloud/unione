package com.unione.cloud.gateway.sso;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.core.util.RequestUtils;
import com.unione.cloud.gateway.feign.UniOneSSoClient;
import com.unione.cloud.gateway.feign.UniOneSSoClient.UniOneSSoRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthRealm {
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private RedisService redisService;
	
	@Lazy
	@Autowired
	private UniOneSSoClient uniOneSSoClient;
	
	/**
	 * 	授权缓存类型：redis
	 */
	@Value("${security.auth.cache.type:}")
	private String AUTH_CACHE_TYPE;
	
	/**
	 * 	授权缓存时间：redis缓存时有效，默认60*30秒
	 */
	@Value("${security.auth.cache.time:1800}")
	private int    AUTH_CACHE_TIME;
	
	
	private final String AUTH_CACHE_PREFIX="AUTH_CACHE_";
	
	
	/**
	 * 	获取收起认证realm名称
	 * @return
	 */
	public abstract String getRealmName();
	
	/**
	 * 	获得当前请求用户授权code,唯一标识当前登录用户
	 * @param request
	 * @param response
	 * @return
	 */
	public abstract String getAuthCode(ServerHttpRequest request, ServerHttpResponse response);
	
	
	/**
	 * 	获取用户认证信息
	 * 	对于某些特殊情况，可以通过请求新获得完整的凭据时，可以通过重现该接口，如此不需要额外调用sso服务
	 * @param request
	 * @param response
	 * @return
	 */
	public UserPrincipal getAuthPrincipal(ServerHttpRequest request, ServerHttpResponse response) {return null;};
	
	/**
	 * 	执行授权认证
	 * @param authCode
	 * @param request
	 * @param response
	 * @return
	 */
	public UserPrincipal doGetAuthentication(String authCode,ServerHttpRequest request, ServerHttpResponse response) {
		// 获得当前请求用户授权code
		String realmName = this.getRealmName();
		log.debug("进入SSO授权认证方法，realm name:{},auth code:{}",realmName,authCode);
		AssertUtil.service().notNull(authCode, "用户授权code不能为空");
		
		UserPrincipal principal=null;
		// 从缓存中验证当前用户授权code是否已认证
		if("redis".equals(AUTH_CACHE_TYPE)) {
			principal=redisService.getObj(AUTH_CACHE_PREFIX+authCode);
		}
		
		if(principal==null) {
			principal=this.getAuthPrincipal(request,response);
			if(principal!=null) {
				// 如果通过getAuthPrincipal接口成功获取到凭据对象，则不需要调用login服务进行授权认证
				return principal;
			}
		}
		
		if(principal==null) {
			principal = new UserPrincipal();
			String ip = RequestUtils.getClientIp(request);
			principal.getAttr().put("AUTH_REALM_NAME", realmName);
			principal.getAttr().put("AUTH_CODE", authCode);
			principal.getAttr().put("AUTH_IP", ip);
			try {
				Results<UserPrincipal> result = this.doThirdLogin(tokenService.build(principal), realmName, authCode);
				if(result !=null && result.isSuccess()) {
					principal = result.getBody();
					principal.getAttr().put("AUTH_REALM_NAME", realmName);
					principal.getAttr().put("AUTH_CODE", authCode);
					// 设置缓存
					if("redis".equals(AUTH_CACHE_TYPE)) {
						redisService.put(AUTH_CACHE_PREFIX+authCode,principal,Duration.ofSeconds((long)(AUTH_CACHE_TIME*(Math.random()+1))));
					}
					log.info("SSO授权认证成功,realm name:{},auth code:{},user id:{}",realmName,authCode,principal.getId());
				}else {
					log.error("执行SSO授权认证失败，error:{}",result.getMessage());
					// 清空缓存
					if("redis".equals(AUTH_CACHE_TYPE)) {
						redisService.delete(AUTH_CACHE_PREFIX+authCode);
					}
					SessionHolder.setToken(null);
					SessionHolder.setUserPrincipal(null);
					return null;
				}
			}catch(Exception e) {
				// 清空缓存
				if("redis".equals(AUTH_CACHE_TYPE)) {
					redisService.delete(AUTH_CACHE_PREFIX+authCode);
				}
				log.error("调用SSO服务认证出错:{}",e);
				return null;
			}
		}else {
			return principal;
		}
		
		return principal;
	}
	
	
	/**
	 * 	调用SSO服务授权接入方法 【feign】
	 * @param authToken
	 * @param realmName
	 * @param authCode
	 * @return
	 */
	protected Results<UserPrincipal> doThirdLogin(String authToken,String realmName,String authCode){
		log.info("进入:调用SSO服务授权接入方法,authToken:{},realmName:{},authCode:{}",authToken,realmName,authCode);

		UniOneSSoRequest request=new UniOneSSoRequest();
		request.setAuthCode(authCode);
		request.setAuthToken(authToken);
		request.setRealmName(realmName);
		Results<UserPrincipal> result=new Results<>();
		
		try {
			CompletableFuture<Results<UserPrincipal>> future = CompletableFuture.supplyAsync(new Supplier<Results<UserPrincipal>>() {
				@Override
				public Results<UserPrincipal> get() {
					log.info("调用SSO服务授权接入方法,realmName:{},authCode:{}",realmName,authCode);
					return uniOneSSoClient.sso(request);
				}
			});
			result = future.get();
		} catch (Exception e) {
			log.error("调用SSO服务授权接入方法失败,realmName:{},authCode:{}",realmName,authCode,e);
		}
		
		log.info("退出:调用SSO服务授权接入方法方法,authToken:{},realmName:{},authCode:{},success:{}",authToken,realmName,authCode,result.isSuccess());
		return result;
	}
	
	
}
