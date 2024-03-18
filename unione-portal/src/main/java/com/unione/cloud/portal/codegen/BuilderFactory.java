package com.unione.cloud.portal.codegen;

import java.util.List;

import org.beetl.sql.clazz.ColDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.JavaType;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.gen.Attribute;
import org.beetl.sql.gen.PackageList;
import org.beetl.sql.gen.SourceBuilder;
import org.beetl.sql.gen.SourceConfig;

import com.unione.cloud.core.model.BaseField;

public class BuilderFactory extends SourceConfig {
	
	private SQLManager sqlManager;

	public BuilderFactory(SQLManager sqlManager, List<SourceBuilder> sourceBuilder) {
		super(sqlManager, sourceBuilder);
		this.sqlManager=sqlManager;
	}

	@Override
	protected Attribute toAttribute(TableDesc tableDesc, ColDesc colDesc, PackageList packageList) {
		PojoAttribute attribute = new PojoAttribute();
		attribute.setAuto(colDesc.isAuto());
		attribute.setColName(colDesc.getColName());
		attribute.setComment(colDesc.getRemark());
		String javaType = JavaType.mapping.get(colDesc.getSqlType());
		attribute.setJavaType(javaType);
		attribute.setName(sqlManager.getNc().getPropertyName(colDesc.getColName()));
		attribute.setJavaType(getJavaType(colDesc, packageList));
		if (tableDesc.getIdNames().contains(colDesc.getColName())) {
			attribute.setId(true);
		}
		attribute.setColSize(colDesc.getSize());
		return attribute;
	}

}
