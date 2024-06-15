package com.unione.cloud.core.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.security.UserRoles.Roles;

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
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getId():null;
	}

	@Override
	public Long getTenantId() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getTenantId():null;
	}

	@Override
	public Long getOrgId() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getOrgId():null;
	}

	@Override
	public String getOrgLvsn() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getOrgLvsn():null;
	}
	
	@Override
	public String getAreaCode() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getAreaCode():null;
	}
	
	@Override
	public String getUsername() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getUsername():null;
	}
	
	@Override
	public String getUserTel() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getTel():null;
	}

	@Override
	public String getRealname() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getRealName():null;
	}

	@Override
	public String getAliasname() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getAliasName():null;
	}

	@Override
	public Integer getUsertype() {
		UserPrincipal principal=this.getPrincipal();
		return principal!=null?principal.getUserType():null;
	}

	@Override
	public List<String> getUserRoles() {
		UserPrincipal principal=this.getPrincipal();
		log.debug("获取当前用户信息,principal:{}",principal);
		return principal!=null?principal.getUserRoles():new ArrayList<String>();
	}

	@Override
	public UserPrincipal getPrincipal() {
		return session.get();
	}

	@Override
	public String getToken() {
		return token.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> A getAttr(String name) {
		UserPrincipal principal=this.getPrincipal();
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
	
	@Override
	public boolean hasRole(Roles... roles) {
		List<String> list=this.getUserRoles();
		for(Roles role:roles) {
			if(list.contains(role.code())) {
				return true;
			}
		}
		return false;
	}
	

}
