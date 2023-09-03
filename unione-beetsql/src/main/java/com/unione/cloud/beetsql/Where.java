package com.unione.cloud.beetsql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Where implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2029148476637433169L;
	private String where;
	private Object params;
	private String group;
	private String having;
	private String sql;
	private List<Object> values=new ArrayList<Object>();
	
	public Where(Object params,String where) {
		this.params=params;
		this.where=where;
		build();
	}
	
	private void build() {
		// name=? and age>? and realname like ? and time>#{timeBegin} and time<=#{timeEnd}
		log.info("where:{}",where);
		
		Pattern pattern=Pattern.compile("[]");
		
	}
	
	public Where group(String group) {
		this.group=group;
		return this;
	}
	
	public Where having(String having) {
		this.having=having;
		return this;
	}
	
	public String sql() {
		if(group!=null || having!=null) {
			return String.format("%s %s %s", sql,group,having);
		}
		if(group!=null) {
			return String.format("%s %s", sql,group);
		}
		return sql;
	}
	public List<Object> values() {
		return values;
	}
	
}
