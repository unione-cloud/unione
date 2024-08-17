package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.unione.cloud.core.model.BaseField;

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
	 * 	查询条件集合
	 */
	private List<SqlCondition> conditions=new ArrayList<>();
	
//	/**
//	 * 	id查询条件集合
//	 */
//	private List<SqlCondition> idsConditions=new ArrayList<>();
//	
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
	
	/**
	 * 获取指定标准字段
	 * @param stsField
	 * @return
	 */
	public SqlField getStsField(BaseField stsField) {
		for(int i=0;i<this.fields.size();i++) {
			SqlField field=this.fields.get(i);
			if(stsField.equals(field.getStsField())) {
				return field;
			}
		}
		return null;
	}
	
	
	public List<SqlField> getStsFields(BaseField... stsFields) {
		List<SqlField> list=new ArrayList<>();
		if(stsFields.length>0) {
			Map<BaseField,SqlField> map=new HashMap<>();
			for(int i=0;i<this.fields.size();i++) {
				SqlField field=this.fields.get(i);
				if(field.getStsField()!=null) {
					map.put(field.getStsField(), field);
				}
			}
			for(BaseField stsField:stsFields) {
				SqlField field=map.get(stsField);
				if(field!=null) {
					list.add(field);
				}
			}
		}
		return list;
	}
	
}
