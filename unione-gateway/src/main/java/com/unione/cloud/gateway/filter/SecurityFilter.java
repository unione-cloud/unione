package com.unione.cloud.gateway.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.core.util.ApiPermisUtil;
import com.unione.cloud.core.util.RequestUtils;
import com.unione.cloud.core.util.SpringCtxUtil;
import com.unione.cloud.gateway.sso.AbstractAuthRealm;
import com.unione.cloud.gateway.util.LogsUtil;
import com.unione.cloud.gateway.util.LogsUtil.LogType;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RefreshScope
public class SecurityFilter extends AbstractGatewayFilterFactory<SecurityFilter.Config> {

	@Autowired
	private TokenService tokenService;
	
	
	private PathMatcher matcher = new AntPathMatcher();
	
	/**
	 * 	第三方授权认证realm集合
	 */
	private Map<String, AbstractAuthRealm> thirdAuthRealms=null;
	
	private Map<Integer, String> ERROR_HANDLES=new HashMap<>();
	/**
	 * 	设置gateway异常处理，json对象，{statu->url}
	 * @param handleConfig
	 */
	@SuppressWarnings("unchecked")
	@Value("${spring.cloud.gateway.error.handler:}")
	public void setErrorHandle(String handleConfig) {
		log.info("进入：设置gateway异常处理方法,config:{}",handleConfig);
		ERROR_HANDLES.clear();
		if(!StringUtils.isEmpty(handleConfig)) {
			Map<String, Object> map = JSONUtil.toBean(handleConfig, Map.class);
			map.entrySet().forEach(entry->{
				ERROR_HANDLES.put(Integer.parseInt(entry.getKey()), (String)entry.getValue());
			});
		}
	}
	
	@Data
	public static class Config {
		/**
		 * 	拦截器名称
		 */
		private String name="tokenFilter";
		/**
		 * 	登录url
		 */
		private String loginUrl="/login";
		/**
		 * 	token存储名称
		 */
		private String tokenName="token";
		/**
		 * 	白名单
		 */
		private List<String> exclusion=new ArrayList<>();
		/**
		 * 	第三方认证realm集合
		 */
		private List<String> thirdRealms=new ArrayList<>();
		/**
		 * 	第三方授权认证失败跳转页面，为空则不跳转
		 */
		private String thirdAuthFailureUrl="/login#/unbind";
		/**
		 * 	第三方授权认证失败错误提示信息
		 */
		private String thirdAuthFailureMsg="未绑定本系统帐号，请联系管理员";
		/**
		 * 	未认证提示信息
		 */
		private String noLoginTip="当前离线，请先登录";
		/**
		 * 	无操作权限提示信息
		 */
		private String noPermisTip="无操作权限";
		/**
		 * 	用户注销成功提示信息
		 */
		private String logoutTip="注销成功";
		/**
		 * 	token 鉴权验证开关，默认关闭
		 */
		private boolean tokenSignatureEnable;
		/**
		 *  token 鉴权强制验证开关，默认关闭
		 */
		private boolean tokenSignatureForce;
		/**
		 * 	token 鉴权验证提示信息
		 */
		private String tokenSignatrueTips="您正在进行非法操作，平台已记录非法行为，请立刻停止改操作";
		/**
		 * 	token 鉴权验证页面跳转url（非ajax请求优先使用该配置，如果为空，则给他出文本提示）
		 */
		private String tokenSignatrueFailTo;
		/**
		 * 	可信IP地址集合
		 */
		private List<String> trustIpAddrs=new ArrayList<>();
		
	}

	public SecurityFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ($exchange, chain) -> {
			log.debug("进入:SecurityFilter拦截器");
			final ServerWebExchange exchange=$exchange;

			// 获得请求对象
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response= exchange.getResponse();
			
			// 获得当前请求uri
			String requestUri = request.getURI().getPath();
			log.debug("进入:SecurityFilter拦截器,请求url:{}",requestUri);
			
			// 获得请求ip信息
			String ip = RequestUtils.getClientIp(request);
			
			// 从header中获取
			String token = null;
			if (request.getHeaders() != null) {
				token = request.getHeaders().getFirst(config.getTokenName());
			}
			
			// 从cookie中获取
			if (StringUtils.isEmpty(token) && request.getCookies() != null) {
				HttpCookie ck = request.getCookies().getFirst(config.getTokenName());
				if (ck != null) {
					token = ck.getValue();
				}
			}
			
			// 从params中获取
			if (StringUtils.isEmpty(token) && request.getQueryParams() != null) {
				token = request.getQueryParams().getFirst(config.getTokenName());
			}
			
			// 公共的url,直接放行
			if (!matcher.match("/*/isAuthed", requestUri) && !matcher.match("/login/logout", requestUri) && 
					config.getExclusion().stream().anyMatch(x -> matcher.match(x, requestUri))) {
				log.debug("url白名单,直接放行,uri:{}",requestUri);
				ServerWebExchange ex = setRequestHeader(exchange,config, token, ip);
				return chain.filter(ex).then(Mono.fromRunnable(()->{
					after(exchange, config);
				}));
			}
			
			UserPrincipal principal = null;
			String realmName = null;		//认证realm名称，如果不为空则是第三方认证信息转换后的token
			if (token != null) {
				principal = tokenService.toPrincipal(token);
				if(principal!=null) {
					realmName = (String)principal.getAttr().get("AUTH_REALM_NAME");
					
					// token鉴权防止越权逻辑：开始  
					if(config.tokenSignatureEnable && StringUtils.isNotEmpty((String)principal.getAttr().get("signature")) || config.tokenSignatureForce) {
						// token 签名验证 signature = base64(md5(username+ip))
						String signature=SmUtil.sm3().digestHex(principal.getUsername()+ip);
						log.debug("token签名验证,signature input:{},sign:{},client ip:{},call url:{},trust ips:{}",principal.getAttr().get("signature"),
								signature,ip,requestUri,config.trustIpAddrs);
						
						String isFeignReq=request.getHeaders().getFirst(DigestUtil.md5Hex(token));
						log.debug("验证当前请求是否为内部feign调用,token:{},isFeignReq:{}",token,isFeignReq);
						if(!"true".equalsIgnoreCase(isFeignReq) && ObjectUtils.notEqual(signature, principal.getAttr().get("signature")) && 
								!config.trustIpAddrs.stream().anyMatch(i->{
							if(ip.equals(i)) {
								return true;
							}
							if(i.endsWith(".*") && i.subSequence(0, i.lastIndexOf(".")).equals(ip.substring(0, ip.lastIndexOf(".")))) {
								return true;
							}
							return false;
						})) {
							log.error("token签名验证失败,signature input:{},sign:{}",principal.getAttr().get("signature"),signature);
							log.error("token info username:{},ip:{}",principal.getUsername(),ip);
							
							SessionHolder.setToken(tokenService.getAuthToken(token));
							LogsUtil.set(LogType.Token, "token签名验证失败");
							LogsUtil.setTarget(principal.getId());
							LogsUtil.setIp(ip);
							LogsUtil.setPrincipal(principal);
							LogsUtil.add("token签名验证失败，用户username:%s,的token被非法获取并使用,非法使用ip:%s",principal.getUsername(),ip);
							LogsUtil.add("当前请求url：%s",requestUri);
							LogsUtil.add("当前请求token：%s",token);
							LogsUtil.add("当前请求reale token：%s",tokenService.getAuthToken(token));
							LogsUtil.add("当前请求signature：%s",signature);
							LogsUtil.add("当前请求isFeignReq：%s",isFeignReq);
							LogsUtil.failure();
							
							// 判断是否为ajax请求
							if (isAjaxRequest(exchange.getRequest()) || StringUtils.isEmpty(config.getTokenSignatrueFailTo())) {
								// 给出非法操作提示并记录日志
								Results<?> result=new Results<>();
								result.setMessage(config.getTokenSignatrueTips());
								result.setCode(HttpStatus.FORBIDDEN.value());
								return writeResult(response,result);
							}
							
							// 重定向到帐号未绑定提示页面
							response.setStatusCode(HttpStatus.SEE_OTHER);
							response.getHeaders().set("Location", config.getTokenSignatrueFailTo());
							return response.setComplete();
						}
					}
					// token鉴权防止越权逻辑：结束
				}
			}
			
			
			// 监测第三方授权认证
			if(principal == null && config.thirdRealms.size()>0) {
				
				// 首次进行认证
				for(String rlName:config.thirdRealms) {
					// 根据 rlName 获得 realm 实例
					AbstractAuthRealm realm=SpringCtxUtil.getBean(rlName);
					String authCode = realm.getAuthCode(request,response);
					log.debug("第三方认证接入，首次进行认证，realm name:{},auth code:{}",rlName,authCode);
					if(!StringUtils.isEmpty(authCode)) {
						// 成功获取到authCode，则必定成功获取principal对象，否则第三方认证对接失败
						principal=realm.doGetAuthentication(authCode,request,response);
						if(principal!=null) {
							token = tokenService.build4auth(principal);
							log.debug("第三方认证接入，首次进行认证，realm name:{},auth code:{},认证成功,token:{}",rlName,authCode,token);
							
							// 添加token到header
							ServerWebExchange ex = setRequestHeader(exchange,config, token, ip);
							String _tk=token;
							return chain.filter(ex).then(Mono.fromRunnable(()->{
								afterThirdAuth(ex, config,_tk);
							}));
						}else {
							log.error("第三方认证接入，首次进行认证，realm name:{},auth code:{},认证失败,重定向到错误页面",rlName,authCode);
							// 判断是否为ajax请求
							if (isAjaxRequest(exchange.getRequest())) {
								// 给出帐号未绑定错误提示
								Results<?> result=new Results<>();
								result.setMessage(config.getThirdAuthFailureMsg());
								result.setCode(HttpStatus.UNAUTHORIZED.value());
								return writeResult(response,result);
							}
							
							// 重定向到帐号未绑定提示页面
							response.setStatusCode(HttpStatus.SEE_OTHER);
							response.getHeaders().set("Location", config.getThirdAuthFailureUrl());
							return response.setComplete();
						}
					}
				}
			}else if(!StringUtils.isEmpty(realmName) && config.thirdRealms.size()>0){
				// 根据 realmName 获得 realm 实例
				AbstractAuthRealm realm=SpringCtxUtil.getBean(realmName);
				String authCode = realm.getAuthCode(request,response);
				if(!StringUtils.isEmpty(authCode) && !authCode.equals( (String)principal.getAttr().get("AUTH_CODE"))) {
					// 如果当前用户的第三方授权code和当前请求的授权code不一致，则使用当前授权code重现认证
					log.warn("当前用户的第三方授权code和当前请求的授权code不一致，则使用当前授权code重现认证,realm name:{},current auth code:{},new auth code:{}");
					principal=realm.doGetAuthentication(authCode,request,response);
					if(principal!=null) {
						token = tokenService.build4auth(principal);
						log.warn("使用当前授权code重现认证成功,token:{}",token);
						
						// 添加token到header
						ServerWebExchange ex = setRequestHeader(exchange,config, token, ip);
						String _tk=token;
						return chain.filter(ex).then(Mono.fromRunnable(()->{
							afterThirdAuth(ex, config,_tk);
						}));
					}else {
						log.warn("使用当前授权code重现认证失败");
						// 判断是否为ajax请求
						if (isAjaxRequest(exchange.getRequest())) {
							// 给出帐号未绑定错误提示
							Results<?> result=new Results<>();
							result.setMessage(config.getThirdAuthFailureMsg());
							result.setCode(HttpStatus.UNAUTHORIZED.value());
							return writeResult(response,result);
						}
						
						// 重定向到帐号未绑定提示页面
						response.setStatusCode(HttpStatus.SEE_OTHER);
						response.getHeaders().set("Location", config.getThirdAuthFailureUrl());
						return response.setComplete();
					}
				}
			}
			// 监测第三方授权认证  END
			
			// 判断是否为 login 页面请求,login页面直接放行
			if(matcher.match(config.getLoginUrl(), requestUri)) {
				return chain.filter(exchange).then(Mono.fromRunnable(()->{
					after(exchange, config);
				}));
			}			
			
			// 判断是否为 /login/logout请求
			if(matcher.match("/login/logout", requestUri)) {
				String method=request.getMethodValue();
				if(!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
					Results<?> result=new Results<>();
					result.setMessage("资源未找到");
					result.setCode(HttpStatus.NOT_FOUND.value());
					return writeResult(response,result);
				}
				return chain.filter(exchange).then(Mono.fromRunnable(()->{
					after(exchange, config);
				}));
			}
			
			// 判断是否为 /*/isAuthed请求
			if(matcher.match("/*/isAuthed", requestUri)) {
				String method=request.getMethodValue();
				if(!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
					Results<?> result=new Results<>();
					result.setMessage("资源未找到");
					result.setCode(HttpStatus.NOT_FOUND.value());
					return writeResult(response,result);
				}
				return writePrincipal(config,response,principal,token);
			}
			

			// 已登录用户
			if (principal != null) {
				SessionHolder.setToken(token);
				SessionHolder.setUserPrincipal(principal);
				
				boolean hadPermis=ApiPermisUtil.validPsermis(principal, requestUri);
				if(!hadPermis) {
					//当前用户无权限访问该url，给出错误提示
					Results<?> result=new Results<>();
					result.setMessage(config.getNoPermisTip());
					result.setCode(HttpStatus.FORBIDDEN.value());
					return writeResult(response,result);
				}
					
				// 添加token到header
				ServerWebExchange ex = setRequestHeader(exchange,config, token, ip);
				return chain.filter(ex).then(Mono.fromRunnable(()->{
					after(ex, config);
				}));
			}
			
			// 清空 token cookie
			ResponseCookie cookie = ResponseCookie.from(config.getTokenName(), null).path("/").maxAge(0).build();
			response.addCookie(cookie);
			
			// 未登录用户，判断是否为ajax请求
			if (isAjaxRequest(exchange.getRequest())) {
				// 用户未登录
				Results<?> result=new Results<>();
				result.setMessage(config.getNoLoginTip());
				result.setCode(HttpStatus.UNAUTHORIZED.value());
				return writeResult(response,result);
			}
			
			// 用户未登录，重定向到登录页面
			response.setStatusCode(HttpStatus.SEE_OTHER);
			response.getHeaders().set("Location", config.getLoginUrl());
			return response.setComplete();
		};
	} 
	
	
	/**
	 * 	拦截器after处理
	 * @param exchange
	 * @param config
	 */
	protected void after(ServerWebExchange exchange,Config config) {
		HttpStatus httpStatus=exchange.getResponse().getStatusCode();
		if (!exchange.getResponse().isCommitted()&& httpStatus.isError()) {
			 String uri=exchange.getRequest().getURI().toString();
			 int status = exchange.getResponse().getStatusCode().value();
			 log.error("Gateway Router Server Process Error,url:{},status:{}",uri,status);
			 if(ERROR_HANDLES.get(status)!=null) {
				 if (isAjaxRequest(exchange.getRequest())) {
					Results<?> result=new Results<>();
					result.setMessage("系统异常");
					result.setCode(status);
					this.writeResult(exchange.getResponse(),result);
				 }else {
					 ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatusHolder.parse("302"));
					 ServerHttpResponse response = exchange.getResponse();
					 response.getHeaders().set("Location", ERROR_HANDLES.get(status));
				 }
			 }else {
				 log.info("未设置异常处理，使用底层默认处理");
			 }
		}
	}
	
	/**
	 * 	第三方授权认证成功后处理
	 * @param exchange
	 * @param config
	 * @param token
	 */
	protected void afterThirdAuth(ServerWebExchange exchange,Config config,String token) {
		ServerHttpResponse response = exchange.getResponse();
		// 设置 token cookie
		ResponseCookie cookie = ResponseCookie.from(config.getTokenName(), token).path("/").build();
		response.addCookie(cookie);
	}

	
	protected Map<String, AbstractAuthRealm> getThirdAuthRealms(){
		if(thirdAuthRealms==null) {
			thirdAuthRealms=new HashMap<>();
			SpringCtxUtil.getBeans(AbstractAuthRealm.class).values().forEach(realm->{
				thirdAuthRealms.put(realm.getRealmName(), realm);
			});
		}
		return thirdAuthRealms;
	}
	

	/**
	 * 	输出结果
	 * @param response
	 * @param result
	 * @return
	 */
	protected Mono<Void> writeResult(ServerHttpResponse response, Results<?> result) {
		// 设置状态码
		response.setStatusCode(HttpStatus.valueOf(result.getCode()));
		
		// 设置headers
		HttpHeaders httpHeaders = response.getHeaders();
		httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

		// 设置body
		DataBuffer bodyDataBuffer = response.bufferFactory().wrap(JSONUtil.toJsonStr(result).getBytes());

		return response.writeWith(Mono.just(bodyDataBuffer));
	}
	

	/**
	 * 	输出当前用户信息
	 * @param config
	 * @param response
	 * @param principle
	 * @param token
	 * @return
	 */
	protected Mono<Void> writePrincipal(Config config,ServerHttpResponse response,UserPrincipal principle,String token){
      
            HashMap<String, Object> result = new HashMap<>();
            result.put("success", principle!=null);
            result.put("principal", principle);
			result.put("token", principle!=null?token:null);
			result.put("message",  principle!=null?"用户已认证":"当前未认证");

			// 设置headers
			HttpHeaders httpHeaders = response.getHeaders();
			httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
			httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
			
			// 添加 token cookie
			ResponseCookie cookie = ResponseCookie.from(config.getTokenName(), token).path("/").build();
			response.addCookie(cookie);
			
			// 设置body
			DataBuffer bodyDataBuffer = response.bufferFactory().wrap(JSONUtil.toJsonStr(result).getBytes());

			return response.writeWith(Mono.just(bodyDataBuffer));
    }

	/**
	 * 	设置请求头信息
	 * @param exchange
	 * @param principal
	 * @param remoteAddress
	 * @return
	 */
	protected ServerWebExchange setRequestHeader(ServerWebExchange exchange,Config config, String token,
			String remoteAddress) {
		// 添加当前用户信息到header
		Builder reqBuilder = exchange.getRequest().mutate()
				.header(config.getTokenName(), tokenService.getAuthToken(token))
				.header("RemoteAddress", remoteAddress);
		return exchange.mutate().request(reqBuilder.build()).build();
	}

	
	
	/**
	 * 	判断当前请求是否为ajax
	 * @param req
	 * @return
	 */
	protected Boolean isAjaxRequest(ServerHttpRequest req) {
		String XMLHttpRequest = req.getHeaders().getFirst("x-requested-with");
		if ("XMLHttpRequest".equalsIgnoreCase(XMLHttpRequest)) {
			return true;
		}
		String AxiosWith = req.getHeaders().getFirst("X-Axios-With");
		if ("true".equalsIgnoreCase(AxiosWith)) {
			return true;
		}
		return false;
	}
	
	
	
	
}
