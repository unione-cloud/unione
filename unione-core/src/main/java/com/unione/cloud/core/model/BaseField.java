package com.unione.cloud.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;

public interface BaseField {
	
	public static StsField ID=new StsField("id","ID");
	public static StsField TENANT_ID=new StsField("tenantId","TENANT_ID");
	public static StsField ORGAN_ID=new StsField("orgId","ORG_ID");
	public static StsField ORGAN_CODE=new StsField("orgCode","ORG_CODE");
	public static StsField AREA_CODE=new StsField("areaCode","AREA_CODE");
	public static StsField USER_ID=new StsField("userId","USER_ID");
	
	public static StsField CREATED=new StsField("created","CREATED");
	public static StsField CREATED_BY=new StsField("createdBy","CREATED_BY");
	public static StsField LAST_UPDATED=new StsField("lastUpdated","LAST_UPDATED");
	public static StsField LAST_UPDATED_BY=new StsField("lastUpdatedBy","LAST_UPDATED_BY");
	
	
	public static StsField isBase(String field) {
		return isBase(field, null);
	}
	
	public static StsField isBaseColume(String field) {
		return isBase(field, "column");
	}
	
	public static StsField isBaseProp(String field) {
		return isBase(field, "prop");
	}
	
	static StsField isBase(String field,String type) {
		Field fields[]=ReflectUtil.getFields(BaseField.class);
		try {
			for(int i=0;i<fields.length;i++) {
				Field fd=fields[i];
				if(Modifier.isStatic(fd.getModifiers())) {
					fd.setAccessible(true);
					Object value=fd.get(null);
					if(value!=null && value instanceof StsField) {
						StsField sfd=(StsField)value;
						if(StringUtils.isEmpty(type) || "prop".equals(type)) {
							if(sfd.name().equals(field)) {
								return sfd;
							}
						}
						if(StringUtils.isEmpty(type) || "column".equals(type)) {
							if(sfd.column().equals(field)) {
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
	
	public static class StsField {
		private StsField(String name,String column) {
			this.name=name;
			this.column=column;
		}
		private String name;
		private String column;
		
		public String name() {
			return this.name;
		}
		public String column() {
			return this.column;
		}
	}
}
