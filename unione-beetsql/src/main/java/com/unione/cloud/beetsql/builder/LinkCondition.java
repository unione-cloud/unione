package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

@Data
public class LinkCondition {

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
	 * 	(括号复杂查询)
	 */
	private List<LinkCondition> childrens=new ArrayList<>();
	
	
	public void toSql(String fkField, StringBuffer buffer) {
				
		// 常规处理
		if(this.childrens==null || this.childrens.isEmpty()) {
			buffer.append("-- @if(notNull(").append(fkField).append("LinkParams.").append(this.column).append(")){\n")
				  .append(this.fun.name()).append(" ")
				  .append(this.column.replaceAll("[A-Z]", "_$0").toUpperCase())
				  .append(this.action.getAction())
				  .append(this.action.express(this.column).replaceAll("params.", String.format("%sLinkParams.", fkField))).append("\n")
				  .append("-- @}\n");
		}else {
			buffer.append("-- @sqlTrim(){\n")
				  .append(this.fun.name()).append(" (\n-- @sqlTrim(){ \n");
			this.childrens.stream().forEach(child->{
				child.toSql(fkField,buffer);
			});
			buffer.append("-- @}\n ) \n")
				  .append("-- @}\n");
		}
	}
	
	
}
