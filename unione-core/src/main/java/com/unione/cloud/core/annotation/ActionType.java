package com.unione.cloud.core.annotation;

/**
 * 操作类型
 */
public enum ActionType {
	
	Query("query"),Save("save"),Delete("delete"),
	Upload("upload"),Download("download"),Import("import"),Export("export"),
	Register("register"),Login("login"),Logout("logout"),ResetPwd("resetpwd"),
	Sensitive("sensitive"),
	Other("other");
	
	private String value;
	ActionType(String value){
		this.value=value;
	}
	public String value() {
		return value;
	}
}
