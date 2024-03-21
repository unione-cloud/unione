package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class SqlEntity {
	
	private String sql;
	
	/**
	 * schema name
	 */
	private String schema;
	
	/**
	 * table or view name
	 */
	private String table;
	
	/**
	 * 	主键字段
	 */
	private SqlField keyField;
	
	/**
	 * 	查询字段列表，为空则是 *
	 */
	private List<SqlField> fields=new ArrayList<>();
	
	/**
	 * 	当前table拥有的集成字段名称集合
	 */
	private List<SqlField> baseFields=new ArrayList<>();
	
	/**
	 * 	查询条件集合
	 */
	private List<SqlCondition> conditions=new ArrayList<>();
	
	/**
	 * 	查询条件
	 */
	private String where;
	
	
	/**
	 * 	获取主键字段
	 * @return
	 */
	public SqlField getKeyField() {
		if(this.keyField!=null) {
			return this.keyField;
		}
		
		for(int i=0;i<this.fields.size();i++) {
			SqlField tmp=this.fields.get(i);
			if(tmp.isPk()) {
				this.keyField=tmp;
				break;
			}
		}
		
		return this.keyField;
	}
	
	
}
