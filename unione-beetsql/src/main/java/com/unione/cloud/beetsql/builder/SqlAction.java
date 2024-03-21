package com.unione.cloud.beetsql.builder;

public enum SqlAction {
	EQ(" = ","#{params.","}"),NEQ("!=","#{params.","}"),
	LT(" < ","#{params.","}"),LTE("<=","#{params.","}"),
	GT(" > ","#{params.","}"),GTE(">=","#{params.","}"),
	IN(" IN ","(#{join(params.",")})"),
	NIN(" NOT IN ","(#{join(params.",")})"),
	LIKE(" LIKE ","#{'%'+params.","+'%'}"),
	LIKER(" LIKE ","#{params.","+'%'}"),
	LIKEL(" LIKE ","#{'%'+params.","}"),
	
	ID(" = ","#{query.","}"),
	IDS(" IN ","(#{join(query.",")})"),
	KEYWORD(" LIKE ","#{'%'+query.","+'%'}");
	
	private SqlAction(String action,String prefix,String sufix) {
		this.action=action;
		this.prefix=prefix;
		this.sufix=sufix;
	}
	
	private String action;
	private String prefix;
	private String sufix;
	
	public String getAction() {
		return action;
	}
	public String express(String field) {
		return String.format("%s%s%s",prefix, field,sufix);
	}
	
	
}
