package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface UniDataPermis {
	
	DataPermis value() default DataPermis.TENANTID;
	
	public static enum DataPermis{
		ALL,		//	所有权限：不启用数据权限
		TENANTID,	// 	租户权限：默认情况下可以查看自己当前租户的数据
		ORGANID,	// 	机构权限：默认情况下可以查看自己当前机构的数据
		ORGANCODE,	//	机构权限：默认情况下可以查看自己当前机构和下级机构的数据
		AREACODE,	//	行政区划：默认情况下可以查看自己所在行政区划数据，包括下级
		USERID		// 	用户权限：默认情况只能查看自己创建的数据
	}

	
}
