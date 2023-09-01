package com.unione.cloud.core.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @描述 用户认证凭证对象
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
public class UserPrincipal implements Serializable {
	
	private static final long serialVersionUID = -8604458874915692692L;

	/**
	 * 用户主键
	 */
	private Long id;
	/**
	 * 租户编码
	 */
	private Long tenantId;
	/**
	 * 机构ID
	 */
	private Long orgId;
	/**
	 * 机构名称
	 */
	private String orgName;
	/**
	 * 用户账号
	 */
	private String username;
	/**
	 * 账号密码
	 */
	@JsonIgnore
	private String pwdText;
	/**
	 * 密码盐
	 */
	@JsonIgnore
	private String pwdSalt;
	/**
	 * 真实姓名
	 */
	private String realName;
	/**
	 * 昵称
	 */
	private String aliasName;
	/**
	 * 头像
	 */
	private String avatar;
	/**
	 * 用户类型
	 */
	private Integer type;
	/**
	 * 用户状态
	 */
	private Integer status;
	/**
	 * 上次登录IP
	 */
	private String lastLoginIp;
	/**
	 * 上次登录时间：时间戳
	 */
	private Long lastLoginTime;
	/**
	 * 累计成功登陆次数
	 */
	private Long totalLoginSuccess;
	/**
	 * 累计失败登陆次数
	 */
	private Long totalLoginFailure;
	/**
	 * 锁定时间期限
	 */
	private Long lockTime;
	/**
	 * 用户角色列表(编码)
	 */
	private List<String> userRoles=new ArrayList<String>();
	/**
	 * 扩展属性
	 */
	private Map<String, Object> attr=new HashMap<String, Object>();
	
}
