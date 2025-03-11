package com.unione.cloud.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 	用户角色 	，通用角色定义，项目中可以自行扩展
 * 	通用角色包括：
 * 	1、超级管理员：管理平台所有数据
 * 	2、
 * @author Jeking Yang
 * @since 1.0.0
 */
public interface UserRoles {
	static Logger log=LoggerFactory.getLogger(UserRoles.class);

	/**
	 * 	超级管理员
	 */
	public static final Roles SUPPER_ADMIN=new Roles("SUPPER-ADMIN","超级管理员",0);
	public static final String SUPPERADMIN="SUPPER-ADMIN";
	
	/**
	 * 	系统运维人员
	 */
	public static final Roles SYSOPS_USER=new Roles("SYSOPS-USER","系统运维人员",1);
	public static final String SYSOPSUSER="SYSOPS-USER";

	/**
     *  系统三权管理
     */
	public static final Roles SYS3P_CONFIG=new Roles("THREE-POWERS-CONFIG","系统三权管理-配置",3);
	public static final Roles SYS3P_AUTH=new Roles("THREE-POWERS-AUTH","系统三权管理-授权",3);
	public static final Roles SYS3P_AUDIT=new Roles("THREE-POWERS-AUDIT","系统三权管理-审计",3);
	public static final String SYS3PCONFIG="THREE-POWERS-CONFIG";
	public static final String SYS3PAUTH="THREE-POWERS-AUTH";
	public static final String SYS3PAUDIT="THREE-POWERS-AUDIT";
	
	/**
	 * 	管理员角色（租户管理员）
	 */
	public static final Roles TENANT_ADMIN=new Roles("TENANT-ADMIN","租户管理员",2);
	public static final String TENANTADMIN="TENANT-ADMIN";
	
	/**
	 * 	管理员角色（机构管理员、二级管理员）
	 */
	public static final Roles ORGAN_ADMIN=new Roles("ORGAN-ADMIN","机构管理员",4);
	public static final String ORGANADMIN="ORGAN-ADMIN";
	
	/**
	 * 	普通用户（正常用户）
	 */
	public static final Roles NORMAL_USER=new Roles("NORMAL-USER","普通用户",5);
	public static final String NORMALUSER="NORMAL-USER";
	
	/**
	 * 	新注册用户（待审用户）
	 */
	public static final Roles NEW_USER=new Roles("NEWS-USER","新注册用户",6);
	public static final String NEWUSER="NEWS-USER";

	/**
	 * 开发人员
	 */
	public static final Roles FORM_DEV=new Roles("FORM-DEV","表单开发人员",3);
	public static final String FORMDEV="FORM-DEV";
	public static final Roles ONLINE_DEV=new Roles("ONLINE-DEV","在线开发人员",3);
	public static final String ONLINEDEV="ONLINE-DEV";

	public static Roles fromCode(String code) {
		try {
			// 获取 UserRoles 类的所有字段
			java.lang.reflect.Field[] fields = UserRoles.class.getDeclaredFields();
			for (java.lang.reflect.Field field : fields) {
				// 检查字段是否为静态的且类型为 Roles
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == Roles.class) {
					// 获取字段的值
					Roles role = (Roles) field.get(null);
					if (role.code().equals(code)) {
						return role;
					}
				}
			}
		} catch (IllegalAccessException e) {
			log.error("获取角色失败,code:{}",code, e);
		}
		return null;
	}
	
	@ToString
	public static class Roles{
		/**
		 * 角色层级,用于权限比较，越小，权限层级越大
		 */
		private Integer level;
		/**
		 * 角色编码，唯一
		 */
		private String code;
		/**
		 * 角色名称
		 */
		private String name;
		public Roles(String code, String name,Integer level) {
			this.code=code;
			this.name = name;
			this.level=level;
		}
		public String name() {return name;}
		public String code() {return code;}
		public Integer level() {return level;}
	}
	
}
