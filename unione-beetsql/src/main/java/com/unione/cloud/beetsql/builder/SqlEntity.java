package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class SqlEntity {
	
	/**
	 * table or view name
	 */
	private String table;
	
	/**
	 * 	查询字段列表，为空则是 *
	 */
	private List<SqlField> fields=new ArrayList<>();
	
	/**
	 * 	查询条件集合
	 */
	private List<SqlCondition> conditions=new ArrayList<>();
	
	/**
	 * 	查询条件
	 */
	private String where;
	
	
	public List<String> getFieldList(){
		if(this.fields==null || this.fields.isEmpty()) {
			return Arrays.asList("*");
		}
		return this.fields.stream().map(field->{
			if(StringUtils.isEmpty(field.getAlias())) {
				return StringUtils.trim(field.getColumn());
			}
			return String.format("%s AS %s", StringUtils.trim(field.getColumn()),StringUtils.trim(field.getAlias()));
		}).collect(Collectors.toList());
	}
	
	/**
	 * 	获取主键字段
	 * @return
	 */
	public SqlField getPkField() {
		SqlField field=null;
		
		for(int i=0;i<this.fields.size();i++) {
			SqlField tmp=this.fields.get(i);
			if(tmp.isPk()) {
				field=tmp;
				break;
			}
		}
		
		return field;
	}
	
	
}
