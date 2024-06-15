package com.unione.cloud.core.security;

import java.util.List;
import java.util.Map;

import com.unione.cloud.core.security.UserRoles.Roles;

/**
 * @描述 <p>用户会话接口
 * <p>1.获得用户ID				String getUserId()
 * <p>2.获得用户机构ID			String getOrgId()
 * <p>3.获得用户账号			String getUsername()
 * <p>4.获得用户真实姓名			String getRealname()
 * <p>5.获得用户昵称			String getAliasname()
 * <p>6.获得用户类型			String getUsertype();
 * <p>7.获得用户状态			String getUserstatus()
 * <p>8.获得用户角色列表			List<String> getUserRoles()
 * <p>9.获得用户信息对象			UserPrincipal getUserPrincipal()
 * <p>10.判断用户是否超级管理员		boolean isAdmin()
 * 
 * @author Jeking Yang
 * @since 1.0.0
 */
public interface SessionService{
	
	/**
	 * 获取用户id
	 * @return
	 */
	public Long getUserId();

	/**
	 * 获取租户id
	 * @return
	 */
	public Long getTenantId();

	/**
	 * 获取用户机构ID
	 * @return
	 */
	public Long getOrgId();
	
	/**
	 * 获取用户机构层级编码
	 * @return
	 */
	public String getOrgLvsn();
	
	/**
	 * 获取用户所在区域编码
	 * @return
	 */
	public String getAreaCode();
	
	/**
	 * 获取用户账号
	 * @return
	 */
	public String getUsername();
	
	/**
	 * 获取用户手机号
	 * @return
	 */
	public String getUserTel();

	/**
	 * 获取用户真实姓名
	 * @return
	 */
	public String getRealname();
	
	/**
	 * 获取用户昵称
	 * @return
	 */
	public String getAliasname();
	
	/**
	 * 获取用户类型
	 * @return
	 */
	public Integer getUsertype();
	
	
	/**
	 * 获取用户角色
	 * @return
	 */
	public List<String> getUserRoles();
	
	/**
	 * 获取用户认证对象
	 * @return
	 */
	public UserPrincipal getPrincipal();
	
	/**
	 * 获取会话token
	 * @return
	 */
	public String getToken();
	
	/**
	 * 获得用户扩展属性
	 * @param <A>
	 * @param name
	 * @return
	 */
	public <A> A getAttr(String name);
	
	/**
	 * 获得会话变量集合（不可修改）
	 * @return
	 */
	public Map<String, Object> getVars();
	
	/**
	 * 获得会话变量
	 * @param <V>
	 * @param name
	 * @return
	 */
	public <V> V getVar(String name);
	
	/**
	 * 设置回话变量（默认微服务不共享，需要开启：security.session.var:true）
	 * @param <V>
	 * @param name
	 * @param value
	 */
	public <V> void setVar(String name,V value);
	
	/**
	 * 验证当前账号是否为超管
	 * @return
	 */
	public boolean isAdmin();
	
	/**
	 * 验证当前帐号是否有指定的角色
	 * @param role
	 * @return 如果拥有其中一个角色，则返回true
	 */
	public boolean hasRole(Roles ...role);
	

}
