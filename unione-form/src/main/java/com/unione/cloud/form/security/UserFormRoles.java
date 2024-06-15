package com.unione.cloud.form.security;

import com.unione.cloud.core.security.UserRoles;

public interface UserFormRoles extends UserRoles {

	/**
	 * 	表单管理员
	 */
	public static final Roles FORM_ADMIN=new Roles("FORM-ADMIN","表单管理员");
	
	/**
	 * 	表单开发人员
	 */
	public static final Roles FORM_DEV=new Roles("FORM-DEV","表单开发人员");
	
	/**
	 * 	表单配置人员
	 */
	public static final Roles FORM_CONFIG=new Roles("FORM-CONFIG","表单配置人员");
	
}
