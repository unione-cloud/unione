package com.unione.cloud.core.model;

public interface BaseField {
	
	public static Field ID=new Field("id","ID");
	public static Field TENANT_ID=new Field("tenantId","TENANT_ID");
	public static Field ORGAN_ID=new Field("orgId","ORG_ID");
	public static Field ORGAN_CODE=new Field("orgCode","ORG_CODE");
	public static Field USER_ID=new Field("userId","USER_ID");
	
	public static Field CREATED=new Field("created","CREATED");
	public static Field CREATED_BY=new Field("createdBy","CREATED_BY");
	public static Field LAST_UPDATED=new Field("lastUpdated","LAST_UPDATED");
	public static Field LAST_UPDATED_BY=new Field("lastUpdatedBy","LAST_UPDATED_BY");
	
	public static class Field {
		private Field(String name,String column) {
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
