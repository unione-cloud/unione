package com.unione.cloud.beetsql.builder;

public enum SqlAction {
	EQ(" = ","#{%s}"),NEQ("!=","#${\"{params.%s}\"}"),
	LT(" < ","#{%s}"),LTE("<=","#${\"{params.%s}\"}"),
	GT(" > ","#{%s}"),GTE(">=","#${\"{params.%s}\"}"),
	IN(" IN ","(#${\"{join(params.%s)}\"})"),
	NIN(" NOT IN ","(#${\"{join(params.%s)}\"})"),
	LIKE(" LIKE ","#${\"{'%'+params.%s+'%'}\"}"),
	LIKER(" LIKE ","#${\"{params.%s+'%'}\"}"),
	LIKEL(" LIKE ","#${\"{'%'+params.%s}\"}");
	
	private SqlAction(String action,String express) {
		this.action=action;
		this.express=express;
	}
	
	private String action;
	private String express;
	
	public String getAction() {
		return action;
	}
	public String express(String field) {
		return String.format(express, field);
	}
}
