package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SqlCondition {

	private SqlFun fun=SqlFun.AND;
	
	/**
	 * 	字段名称
	 */
	private String column;
	
	/**
	 * 	比较方式
	 */
	private SqlAction action;
	
	/**
	 * 	参数名称
	 */
	private String name;
	
	/**
	 * 	(括号复杂查询)
	 */
	private List<SqlCondition> childrens=new ArrayList<>();
	
	
	public void toSql(StringBuffer buffer) {
		
		// ID,IDS搜索特殊处理
		if(SqlAction.ID.equals(this.action) || SqlAction.IDS.equals(this.action)) {
			buffer.append("-- @if(varNotNull(query.").append(this.name).append(")){\n")
			  .append(this.fun.name()).append(" ")
			  .append(this.column).append(this.action.getAction()).append(this.action.express(this.name)).append("\n")
			  .append("-- @}\n");
			return;
		}
		
		// 常规处理
		if(this.childrens==null || this.childrens.isEmpty()) {
			buffer.append("-- @if(varNotNull(params.").append(this.name).append(")){\n")
				  .append(this.fun.name()).append(" ")
				  .append(this.column).append(this.action.getAction()).append(this.action.express(this.name)).append("\n")
				  .append("-- @}\n");
		}else {
			buffer.append("-- @sqlTrim(){\n")
				  .append(this.fun.name()).append(" (\n-- @sqlTrim(){ \n");
			this.childrens.stream().forEach(child->{
				child.toSql(buffer);
			});
			buffer.append("-- @}\n ) \n")
				  .append("-- @}\n");
		}
	}
	
	
}
