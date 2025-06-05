package com.unione.cloud.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;

public enum BaseField {
	
	ID("id","ID"),
	TENANT_ID("tenantId","TENANT_ID"),
	ORGAN_ID("orgId","ORG_ID"),
	ORGAN_CODE("orgCode","ORG_CODE"),
	AREA_CODE("areaCode","AREA_CODE"),
	USER_ID("userId","USER_ID"),
	DEL_FLAG("delFlag","DEL_FLAG"),
	CREATED("created","CREATED"),
	CREATED_BY("createdBy","CREATED_BY"),
	LAST_UPDATED("lastUpdated","LAST_UPDATED"),
	LAST_UPDATED_BY("lastUpdatedBy","LAST_UPDATED_BY");
	
	private String name;
	private String column;
	
	private BaseField(String name,String column) {
		this.name=name;
		this.column=column;
	}
	
	public String getName() {
		return name;
	}
	public String getColumn() {
		return column;
	}

	public boolean is(String field) {
		return name.equals(field) || column.equals(field);
	}

	public static BaseField isBase(String field) {
		return isBase(field, null);
	}
	
	public static BaseField isBaseColumn(String field) {
		return isBase(field, "column");
	}
	
	public static BaseField isBaseProp(String field) {
		return isBase(field, "prop");
	}
	
	
	static BaseField isBase(String field,String type) {
		Field fields[]=ReflectUtil.getFields(BaseField.class);
		try {
			for(int i=0;i<fields.length;i++) {
				Field fd=fields[i];
				if(Modifier.isStatic(fd.getModifiers())) {
					fd.setAccessible(true);
					Object value=fd.get(null);
					if(value!=null && value instanceof BaseField) {
						BaseField sfd=(BaseField)value;
						if(StringUtils.isEmpty(type) || "prop".equals(type)) {
							if(sfd.getName().equals(field)) {
								return sfd;
							}
						}
						if(StringUtils.isEmpty(type) || "column".equals(type)) {
							if(sfd.getColumn().equals(field)) {
								return sfd;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.get().warn("判断是否基础字段失败,field:{},type:{}", field,type,e);
		}
		return null;
	}
	
}
