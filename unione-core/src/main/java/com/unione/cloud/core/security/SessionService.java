package com.unione.cloud.core.security;

import java.util.List;
import java.util.Map;

/**
 * @描述 <p>用户会话接口
 * <p>1.获得用户ID				String getUserId()
 * <p>2.获得用户机构ID				String getOrgId()
 * <p>3.获得用户账号				String getUsername()
 * <p>4.获得用户真实姓名			String getRealname()
 * <p>5.获得用户昵称				String getAliasname()
 * <p>6.获得用户类型				String getUsertype();
 * <p>7.获得用户状态				String getUserstatus()
 * <p>8.获得用户角色列表			List<String> getUserRoles()
 * <p>9.获得用户信息对象			UserPrincipal getUserPrincipal()
 * <p>10.判断用户是否超级管理员		boolean isAdministrator()
 * 
 * @author Jeking Yang
 * @since 1.0.0
 */
public interface SessionService{
	
	public Long getUserId();

	public Long getTenantId();

	public Long getOrgId();
	
	default public String getOrgLvsn() {return null;};
	
	default public String getAreaName() {return null;};
	
	default public String getAreaSn() {return null;};
	
	public String getUsername();

	public String getRealname();
	
	public String getAliasname();
	
	public String getUsertype();
	
	public String getUserstatus();
	
	public List<Long> getUserRoles();
	
	public UserPrincipal getUserPrincipal();
	
	public String getToken();
	
	public <A> A getAttr(String name);
	
	public Map<String, Object> getVars();
	
	public <V> V getVar(String name);
	
	public <V> void setVar(String name,V value);
	
	public boolean isAdmin();

}
