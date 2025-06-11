package com.unione.cloud.beetsql.builder;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.beetl.sql.clazz.ClassDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.core.SQLManager;

import com.unione.cloud.beetsql.annotation.DataDelFlag;
import com.unione.cloud.beetsql.annotation.DataSensitive;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.core.model.BaseField;

import cn.hutool.core.bean.BeanUtil;

public class SqlKit {

    private static ConcurrentHashMap<Class<?>, List<SqlField>> sqlFieldMap = new ConcurrentHashMap<>();

    public static List<SqlField> loadFields(SQLManager sqlManager,Class<?> clazz){
        String tableName=sqlManager.getNc().getTableName(clazz);
        return loadFields(sqlManager,clazz,tableName);
    }
    
	public static List<SqlField> loadFields(SQLManager sqlManager,Class<?> clazz,String tableName){
        List<SqlField> fields = sqlFieldMap.get(clazz);
        if (fields == null) {
            fields=new ArrayList<>();
            TableDesc tableDesc = sqlManager.getTableDesc(tableName);
		    ClassDesc classDesc= tableDesc.genClassDesc(clazz, sqlManager.getNc());
            List<String> idCols = classDesc.getIdCols();
			Iterator<String> cols = classDesc.getInCols().iterator();
			Iterator<String> props = classDesc.getAttrs().iterator();
			while (cols.hasNext() && props.hasNext()) {
				SqlField field=new SqlField();
				field.setAlias(props.next());
				field.setColumn(cols.next());
				if(idCols.contains(field.getColumn())) {
					field.setPk(true);
				}
				
				// 判断是否为基础字段
				BaseField stsField=BaseField.isBaseColumn(field.getColumn());
				field.setStsField(stsField);
				DataDelFlag dataDelFlag=BeanKit.getAnnotation(clazz, field.getAlias(),DataDelFlag.class);
				if(dataDelFlag!=null) {
					field.setStsField(BaseField.DEL_FLAG);
				}
				
				PropertyDescriptor pd = BeanUtil.getPropertyDescriptor(clazz, field.getAlias());
				field.setType(pd.getPropertyType().getSimpleName());
				
				// 如果设置了数据脱敏
				DataSensitive dataSensitive=BeanKit.getAnnotation(clazz, field.getAlias(),DataSensitive.class);
				if(dataSensitive!=null && "String".contentEquals(field.getType())) {
					field.setSensitive(SqlSensitive.build(dataSensitive));
				}
				
				QueryIgnore ignorQuery=BeanKit.getAnnotation(clazz, field.getAlias(),QueryIgnore.class);
				field.setQueryIgnore(ignorQuery);
				
				fields.add(field);
			}
            sqlFieldMap.put(clazz, fields);
        }
		return fields;
	}

    public static SqlEntity buildEntity(SQLManager sqlManager,Class<?> clazz) {
        String tableName=sqlManager.getNc().getTableName(clazz);
        return buildEntity(sqlManager,clazz,tableName);
    }

     public static SqlEntity buildEntity(SQLManager sqlManager,Class<?> clazz,String tableName) {
    	SqlEntity entity=new SqlEntity();
        entity.setTable(tableName);
        List<SqlField> fields = loadFields(sqlManager,clazz,tableName);
        entity.setFields(fields);
        return entity;
    }

}
