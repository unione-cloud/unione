package com.unione.cloud.core.security;

import lombok.ToString;

/**
 * 	用户角色 	，通用角色定义，项目中可以自行扩展
 * 	通用角色包括：
 * 	1、超级管理员：管理平台所有数据
 * 	2、
 * @author Jeking Yang
 * @since 1.0.0
 */
public interface UserRoles {
	
	/**
	 * 	超级管理员
	 */
	public static final Roles SUPPER_ADMIN=new Roles("SUPPER-ADMIN","超级管理员");
	public static final String SUPPERADMIN="SUPPER-ADMIN";
	
	/**
	 * 	系统运维人员
	 */
	public static final Roles SYSOPS_USER=new Roles("SYSOPS-USER","系统运维人员");
	public static final String SYSOPSUSER="SYSOPS-USER";
	
	/**
     *  系统三权管理
     */
	public static final Roles SYS3P_CONFIG=new Roles("THREE-POWERS-CONFIG","系统三权管理-配置");
	public static final Roles SYS3P_AUTH=new Roles("THREE-POWERS-AUTH","系统三权管理-授权");
	public static final Roles SYS3P_AUDIT=new Roles("THREE-POWERS-AUDIT","系统三权管理-审计");
	public static final String SYS3PCONFIG="THREE-POWERS-CONFIG";
	public static final String SYS3PAUTH="THREE-POWERS-AUTH";
	public static final String SYS3PAUDIT="THREE-POWERS-AUDIT";
	
	/**
	 * 	管理员角色（租户管理员）
	 */
	public static final Roles TENANT_ADMIN=new Roles("TENANT-ADMIN","租户管理员");
	public static final String TENANTADMIN="TENANT-ADMIN";
	
	/**
	 * 	管理员角色（机构管理员、二级管理员）
	 */
	public static final Roles ORGAN_ADMIN=new Roles("ORGAN-ADMIN","机构管理员");
	public static final String ORGANADMIN="ORGAN-ADMIN";
	
	/**
	 * 	普通用户（正常用户）
	 */
	public static final Roles NORMAL_USER=new Roles("NORMAL-USER","普通用户");
	public static final String NORMALUSER="NORMAL-USER";
	
	/**
	 * 	新注册用户（待审用户）
	 */
	public static final Roles NEW_USER=new Roles("NEWS-USER","新注册用户");
	public static final String NEWUSER="NEWS-USER";
	
	@ToString
	public static class Roles{
		private String code;
		private String name;
		public Roles(String code, String name) {
			this.code=code;
			this.name = name;
		}
		public String name() {return name;}
		public String code() {return code;}
	}
	
}
