package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.List;

import org.beetl.sql.clazz.kit.DefaultKeyWordHandler;
import org.beetl.sql.clazz.kit.KeyWordHandler;

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
		toSql(fkField, buffer, new DefaultKeyWordHandler());
	}

	public void toSql(String fkField, StringBuffer buffer, KeyWordHandler keyWordHandler) {
				
		// 常规处理
		if(this.childrens==null || this.childrens.isEmpty()) {
			buffer.append("-- @if(notNull(").append(fkField).append("LinkParams.").append(this.column).append(")){\n")
				  .append(this.fun.name()).append(" ")
				  .append(keyWordHandler.getCol(this.column.replaceAll("[A-Z]", "_$0").toUpperCase()))
				  .append(this.action.getAction())
				  .append(this.action.express(this.column).replaceAll("params.", String.format("%sLinkParams.", fkField))).append("\n")
				  .append("-- @}\n");
		}else {
			buffer.append("-- @sqlTrim(){\n")
				  .append(this.fun.name()).append(" (\n-- @sqlTrim(){ \n");
			this.childrens.stream().forEach(child->{
				child.toSql(fkField, buffer, keyWordHandler);
			});
			buffer.append("-- @}\n ) \n")
				  .append("-- @}\n");
		}
	}
	
	
}
