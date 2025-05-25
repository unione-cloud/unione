package com.unione.cloud.codegen;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Template;
import org.beetl.sql.core.engine.template.Beetl;
import org.beetl.sql.core.engine.template.BeetlTemplateEngine;
import org.beetl.sql.gen.Attribute;
import org.beetl.sql.gen.BaseProject;
import org.beetl.sql.gen.Entity;
import org.beetl.sql.gen.SourceConfig;
import org.beetl.sql.gen.simple.MDSourceBuilder;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.BaseField;

public class SqlMdSourceBuilder extends MDSourceBuilder {
	public static  String mapperTemplate=String.format("codegen%smd.btl", File.separator);
	
	public SqlMdSourceBuilder() {
		super();
		this.name="";
	}
	
	public SqlMdSourceBuilder(String name) {
		super();
		this.name=name;
	}
	

	@Override
	public void generate(BaseProject project, SourceConfig config, Entity entity) {
		//BeetlSQl中的配置
		Beetl beetl = ((BeetlTemplateEngine)config.getSqlManager().getSqlTemplateEngine()).getBeetl();
		//模板
		Template template = groupTemplate.getTemplate(mapperTemplate);
		template.binding("tableName", entity.getTableName());
		template.binding("cols", entity.getCols());
		template.binding("props", entity.getList());
		
		List<Attribute> updateCols=new ArrayList<>();
		List<Attribute> lastUpdateCols=new ArrayList<>();
		entity.getList().stream()
			.filter(attr->!attr.isId())
			.filter(attr->!BaseField.ID.getName().equals(attr.getName()))
			.filter(attr->!BaseField.ID.getColumn().equals(attr.getColName()))
			.filter(attr->!BaseField.TENANT_ID.getName().equals(attr.getName()))
			.filter(attr->!BaseField.TENANT_ID.getColumn().equals(attr.getColName()))
			.filter(attr->!BaseField.CREATED.getName().equals(attr.getName()))
			.filter(attr->!BaseField.CREATED.getColumn().equals(attr.getColName()))
			.filter(attr->!BaseField.CREATED_BY.getName().equals(attr.getName()))
			.filter(attr->!BaseField.CREATED_BY.getColumn().equals(attr.getColName()))
			.forEach(attr->{
				if(!BaseField.LAST_UPDATED.getName().equals(attr.getName()) && 
						!BaseField.LAST_UPDATED.getColumn().equals(attr.getColName()) &&
						!BaseField.LAST_UPDATED_BY.getName().equals(attr.getName()) &&
						!BaseField.LAST_UPDATED_BY.getColumn().equals(attr.getColName())) {
					updateCols.add(attr);
				}else {
					lastUpdateCols.add(attr);
				}
			});
		template.binding("updateCols", updateCols);
		template.binding("lastUpdateCols", lastUpdateCols);
		
		// 获取id字段
		List<Attribute> idAttrs = entity.getList().stream().filter(f->f.isId()).collect(Collectors.toList());
		AssertUtil.database()
			.isTrue(!idAttrs.isEmpty(), "数据库表["+entity.getTableName()+"]未设置主键")
			.isTrue(idAttrs.size()==1, "数据库表["+entity.getTableName()+"]是符合主键，不支持复合主键");
		template.binding("idAttr", idAttrs.get(0));
		
		// 绑定基础字段
		template.binding("tenantId", BaseField.TENANT_ID);
		template.binding("orgId", BaseField.ORGAN_ID);
		template.binding("delFlag", BaseField.DEL_FLAG);
		template.binding("lastUpdate", BaseField.LAST_UPDATED);
		template.binding("lastUpdateBy", BaseField.LAST_UPDATED_BY);
				
		
		template.binding("nc", config.getSqlManager().getNc());
		template.binding("PS", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_START"));
		template.binding("PE", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_END"));
		template.binding("SS", beetl.getPs().getProperty("DELIMITER_STATEMENT_START"));
		template.binding("SE", beetl.getPs().getProperty("DELIMITER_STATEMENT_END"));
		String mdFileName = entity.getName()+".md";
		String rsName="resources.sql";
		if(!StringUtils.isEmpty(this.name)) {
			rsName=String.format("%s.%s", rsName,this.name);
		}
		Writer writer = project.getWriterByName(rsName,mdFileName);
		template.renderTo(writer);
	}

}
