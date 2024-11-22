package com.unione.cloud.core.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * 机构层级编码
	 */
	private String orgLvsn;
	/**
	 * 区域编码
	 */
	private String areaCode;
	/**
	 * 用户账号
	 */
	private String username;
	/**
	 * 用户手机号
	 */
	private String tel;
	/**
	 * 真实姓名
	 */
	private String realName;
	/**
	 * 昵称
	 */
	private String aliasName;
	/**
	 * 用户类型
	 */
	private Integer userType;
	 /**
	  * 头像
	  */
	private String avatar;
	/**
	 * 上次登录IP
	 */
	private String lastLoginIp;
	/**
	 * 上次登录时间：时间戳
	 */
	private Long lastLoginTime;

	/**
	 * 用户角色列表(编码)
	 */
	private List<String> userRoles=new ArrayList<String>();
	/**
	 * 扩展属性
	 */
	private Map<String, Object> attr=new HashMap<String, Object>();
	
}
