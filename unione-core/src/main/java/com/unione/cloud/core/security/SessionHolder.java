package com.unione.cloud.core.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RefreshScope
public class SessionHolder implements SessionService {
	
	private static SessionHolder holder;
	private static ThreadLocal<UserPrincipal> session=new NamedInheritableThreadLocal<>("Session context");
	private static ThreadLocal<String> token=new NamedInheritableThreadLocal<>("Token context");
	private static ThreadLocal<Map<String,Object>> var=new NamedInheritableThreadLocal<>("Var context");
	
	@Value("${security.administrator:administrator}")
	private String administrator;
	
	
	public SessionHolder() {
		holder=this;
	}
	public static SessionHolder build() {
		return holder;
	}
	
	
	
	/**
	 * 	设置session用户
	 * @param userPrincipal
	 */
	public static void setUserPrincipal(UserPrincipal userPrincipal) {
		SessionHolder.session.set(userPrincipal);
	}
	
	/**
	 * 	设置用户token
	 * @param token
	 */
	public static void setToken(String token) {
		SessionHolder.token.set(token);
	}
	
	@Override
	public Long getUserId() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getSid():null;
	}

	@Override
	public Long getTenantId() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getTenantId():null;
	}

	@Override
	public Long getOrgId() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getOrgId():null;
	}

	@Override
	public String getOrgLvsn() {
		return this.getAttr("orgLvsn");
	}
	
	@Override
	public String getAreaName() {
		return this.getAttr("areaName");
	}
	
	@Override
	public String getAreaSn() {
		return this.getAttr("areaCode");
	}
	
	@Override
	public String getUsername() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getUsername():null;
	}

	@Override
	public String getRealname() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getRealName():null;
	}

	@Override
	public String getAliasname() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getAliasName():null;
	}

	@Override
	public String getUsertype() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getType():null;
	}

	@Override
	public String getUserstatus() {
		UserPrincipal principal=this.getUserPrincipal();
		return principal!=null?principal.getStatus():null;
	}

	@Override
	public List<Long> getUserRoles() {
		UserPrincipal principal=this.getUserPrincipal();
		log.debug("获取当前用户信息,principal:{}",principal);
		return principal!=null?principal.getUserRoles():new ArrayList<Long>();
	}

	@Override
	public UserPrincipal getUserPrincipal() {
		return session.get();
	}

	@Override
	public String getToken() {
		return token.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> A getAttr(String name) {
		UserPrincipal principal=this.getUserPrincipal();
		if(principal!=null) {
			Map<String , Object> map=principal.getAttr();
			if(map!=null) {
				return (A)map.get(name);
			}
		}
		return null;
	}
	
	@Override
	public Map<String, Object> getVars(){
		Map<String, Object> map=var.get();
		if(map==null) {
			map=new HashMap<String, Object>();
			var.set(map);
		}
		return map; 
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getVar(String name) {
		return (V)getVars().get(name);
	}
	@Override
	public <V> void setVar(String name, V value) {
		getVars().put(name, value);
	}

	@Override
	public boolean isAdmin() {
		return administrator!=null && administrator.equals(this.getUsername());
	}
	

}
