package com.unione.cloud.core.token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RefreshScope
@Configuration
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = false)
public class TokenFilter implements HandlerInterceptor,WebMvcConfigurer {
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private ObjectMapper mapper;
	
	/**
	 * 	请求信息token名称
	 */
	@Value("${security.jwt.token:token}")
	private String REQUEST_TOKEN;
	
	/**
	 * 	静态资源url
	 */
	@Value("${security.filter.resource:/*.html,/**/*.js,/**/*.css,/**/*.png,/**/*.jpg,/img/**,/images/**,/assets/**,/plugins/**}")
	private List<String> RESOURCE_URL=new ArrayList<>();

	/**
	 * 	url拦截名单，默认/**
	 */
	@Value("${security.filter.includes:/**}")
	private List<String> INCLUDES_URL=new ArrayList<>();
	
	/**
	 * 	url白名单
	 */
	@Value("${security.filter.executions:}")
	private List<String> EXECUTIONS_URL=new ArrayList<>();
	
	private PathMatcher matcher = new AntPathMatcher();


	
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("=================================");
		log.info("设置拦截资源路径,size:{}",INCLUDES_URL.size());
		log.info("设置静态资源路径,size:{}",RESOURCE_URL.size());
		log.info("设置排除资源路径,size:{}",EXECUTIONS_URL.size());
		
		InterceptorRegistration registration=registry.addInterceptor(this);
		INCLUDES_URL.stream().forEach(item->registration.addPathPatterns(item));
//		EXECUTIONS_URL.stream().forEach(item->registration.excludePathPatterns(item));
		RESOURCE_URL.stream().forEach(item->registration.excludePathPatterns(item));
		
	}
	
	

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
				// 请求信息
		String uri=request.getRequestURI();
		SessionHolder.build().setVar("_unione_pre_requestid", request.getHeader("_unione_requestid"));
		String _unione_actionid = request.getHeader("_unione_actionid");
		if(StringUtils.isEmpty(_unione_actionid)) {
			_unione_actionid=IdGenHolder.generate()+"";
			request.setAttribute("_unione_actionid", _unione_actionid);
			request.setAttribute("_unione_requestid", _unione_actionid);
			SessionHolder.build().setVar("_unione_requestid", _unione_actionid);
		}else{
			request.setAttribute("_unione_requestid", IdGenHolder.generate()+"");
			SessionHolder.build().setVar("_unione_requestid", request.getHeader("_unione_requestid"));
		}
		SessionHolder.build().setVar("_unione_actionid", _unione_actionid);
		log.debug("request uri:{},url:{},action id:{},pre request id,request id:{}",
			uri,request.getRequestURL(),_unione_actionid,request.getHeader("_unione_pre_requestid"),request.getHeader("_unione_requestid"));

			
		// 首先清空当前线程中token信息
		SessionHolder.setToken(null);
		SessionHolder.setUserPrincipal(null);
		
		// 从header中获取
		String token=request.getHeader(REQUEST_TOKEN);
		// 从cookie中获取
		if(StringUtils.isEmpty(token) && request.getCookies()!=null) {
			for(int i=0;i<request.getCookies().length;i++) {
				Cookie ck=request.getCookies()[i];
				if(ck!=null && REQUEST_TOKEN.equals(ck.getName())){
					token=ck.getValue();
					break;
				}
			}
		}
		// 从params中获取
		if(StringUtils.isEmpty(token)) {
			token=request.getParameter(REQUEST_TOKEN);
		}
		
		if(StringUtils.isEmpty(token)) {
			// 如果token为空则验证当前请求是否在白名单内
			if (EXECUTIONS_URL.stream().anyMatch(x -> matcher.match(x, uri))) {
				log.debug("url白名单,直接放行,uri:{}",uri);
				return true;
			}
			
			log.error("request uri:{},token 获取失败",request.getRequestURI());
			this.writeResult(response);
			return false;
		}
		
		// 验证token
		UserPrincipal principal=tokenService.toPrincipal(token);
		
		log.debug("成功获取并验证，token:{},principal:{}",token,principal);
		if(principal==null) {
			// 如果当前请求是否在白名单内
			if (EXECUTIONS_URL.stream().anyMatch(x -> matcher.match(x, uri))) {
				log.debug("url白名单,直接放行,uri:{}",uri);
				return true;
			}
			this.writeResult(response);
			return false;
		}
		
		SessionHolder.setToken(token);
		SessionHolder.setUserPrincipal(principal);
		
		// 如果是/isAuthed请求，则返回用户信息
		if("/isAuthed".equals(request.getRequestURI())) {
			this.writePrincipal(response, principal, token);
			return false;
		}
		
		return true;
	}
	


	private void writeResult(ServletResponse servletResponse){
        if(servletResponse instanceof HttpServletResponse){
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HashMap<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("code", "401");
            result.put("message","未登录");
            try {
            	response.setStatus(401);
                response.setHeader("Content-Type", "application/json;charset=UTF-8");
                response.getWriter().write(mapper.writeValueAsString(result));
                response.getWriter().flush();
            } catch (IOException e) {
               log.error("writeResult失败",e);
            }
        }
    }
	
	private void writePrincipal(ServletResponse servletResponse,UserPrincipal principle,String token){
        if(servletResponse instanceof HttpServletResponse){
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HashMap<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("principal", principle);
			result.put("token", token);
			result.put("message", "用户已认证");
			
			if(principle!=null) {
				// 设置token到cookie中
				Cookie ck=new Cookie(REQUEST_TOKEN, token);
				ck.setPath("/");
				response.addCookie(ck);
			}
            try {
            	response.setStatus(200);
                response.setHeader("Content-Type", "application/json;charset=UTF-8");
                response.getWriter().write(mapper.writeValueAsString(result));
                response.getWriter().flush();
            } catch (IOException e) {
            	log.error("writePrincipal失败",e);
            }
        }
    }

}
