package com.unione.cloud.beetsql.builder;

public enum SqlType {
	COUNT("SELECT"),
	SELECT("SELECT"),SELECT_BYID("SELECT"),
	UPDATE("UPDATE"),UPDATE_BYID("UPDATE"),
	DELETE("DELETE"),DELETE_BYID("DELETE");
	
	private String value;
	private SqlType(String value) {
		this.value=value;
	}
	public String value() {
		return this.value;
	}
	
}
