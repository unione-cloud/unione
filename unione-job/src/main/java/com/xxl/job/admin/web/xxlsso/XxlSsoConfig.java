package com.xxl.job.admin.web.xxlsso;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.xxl.job.admin.constant.Consts;
import com.xxl.sso.core.bootstrap.XxlSsoBootstrap;
import com.xxl.sso.core.constant.Const;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.id.UUIDTool;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author xuxueli 2018-11-15
 */
@RefreshScope
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class XxlSsoConfig implements HandlerInterceptor,WebMvcConfigurer {

    @Value("${xxl-sso.token.key}")
    private String tokenKey;

    @Value("${xxl-sso.token.timeout}")
    private long tokenTimeout;

    @Value("${security.filter.login:/portal/login}")
    private String loginPath;

    @Value("${security.filter.forbid:/portal/forbid}")
    private String forbidPath;

     @Value("${xxl.footer.hide:false}")
    private boolean hideFooter;


    @Resource
    private SimpleLoginStore loginStore;

    @Autowired
    private SessionService sessionService;


    /**
     * 1、配置 XxlSsoBootstrap
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public XxlSsoBootstrap xxlSsoBootstrap() {

        XxlSsoBootstrap bootstrap = new XxlSsoBootstrap();
        bootstrap.setLoginStore(loginStore);
        bootstrap.setTokenKey(tokenKey);
        bootstrap.setTokenTimeout(tokenTimeout);
        return bootstrap;
    }

    @Override
    @SuppressWarnings("null")
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
        request.setAttribute("isIframe", ObjectUtil.equal(request.getParameter("isIframe"), "true") ||
            ObjectUtil.equal(request.getParameter("isIframe"), "1"));
        request.setAttribute("hideFooter", hideFooter);
        if(sessionService.getPrincipal()!=null){
            LoginInfo loginInfo = new LoginInfo(String.valueOf(sessionService.getUserId()), UUIDTool.getSimpleUUID());
            loginInfo.setUserName(sessionService.getUsername());
            loginInfo.setRealName(sessionService.getRealname());
            if(sessionService.isAdmin()||sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)
                ||sessionService.getUserRoles().contains(UserRoles.SYSOPSUSER) || sessionService.getUserRoles().contains(UserRoles.FORMDEV)){
                loginInfo.setRoleList(Arrays.asList(Consts.ADMIN_ROLE));
            }else{
                response.sendRedirect(forbidPath);
                return false;
            }
		    XxlSsoHelper.loginWithCookie(loginInfo, response, false);
            request.setAttribute(Const.XXL_SSO_USER, loginInfo);
            return true;
        }
        response.sendRedirect(loginPath);
        return false;
    }

    /**
     * 2、配置 XxlSso 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 2.1、build xxl-sso interceptor
        // XxlSsoWebInterceptor webInterceptor = new XxlSsoWebInterceptor(excludedPaths, loginPath);

        // 2.2、add interceptor
        registry.addInterceptor(this).addPathPatterns("/**");
    }

}
