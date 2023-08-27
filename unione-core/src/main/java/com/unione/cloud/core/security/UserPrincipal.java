package com.unione.cloud.core.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @描述 <p>安全认证模型类
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
public class UserPrincipal implements Serializable {
	
	private static final long serialVersionUID = -8604458874915692692L;

	/**
	 * 用户主键
	 */
	private Long sid;
	
	private Long orgId;

	/**
	 * 租户编码
	 */
	private Long tenantId;
	
	private String orgName;
	
	private String username;
	
	@JsonIgnore
	private String password;
	
	private String realName;
	
	private String aliasName;
	
	private String photo;
	
	private String type;
	
	private String status;
	
	private String lastLoginIp;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;
	
	private long totalLoginCount;
	
	private Integer totalLoginFailure;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

	private List<Long> userRoles=new ArrayList<Long>();

	private Map<String, Object> attr=new HashMap<String, Object>();
	
	// web 回话token（jwt+md5签名串）
	private String webToken;
	// app 回话token（jwt+md5签名串）
	private String appToken;
	
}
