package com.unione.cloud.beetsql.builder;

public enum SqlType {
	COUNT("SELECT"),
	SELECT("SELECT"),SELECT_ONE("SELECT"),SELECT_BYID("SELECT"),
	UPDATE("UPDATE"),UPDATE_BYID("UPDATE"),
	DELETE("DELETE"),DELETE_BYID("DELETE"),
	DELETE_LOGIC("UPDATE"),DELETE_LOGIC_BYID("UPDATE");
	
	private String value;
	private SqlType(String value) {
		this.value=value;
	}
	public String value() {
		return this.value;
	}
	
}
