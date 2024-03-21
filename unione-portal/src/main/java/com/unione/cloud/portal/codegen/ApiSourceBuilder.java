package com.unione.cloud.portal.codegen;

import java.io.File;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Template;
import org.beetl.sql.clazz.kit.StringKit;
import org.beetl.sql.core.engine.template.Beetl;
import org.beetl.sql.core.engine.template.BeetlTemplateEngine;
import org.beetl.sql.gen.BaseProject;
import org.beetl.sql.gen.Entity;
import org.beetl.sql.gen.SourceConfig;
import org.beetl.sql.gen.simple.BaseTemplateSourceBuilder;

public class ApiSourceBuilder extends BaseTemplateSourceBuilder {
	public static  String mapperTemplate=String.format("codegen%sapi.btl", File.separator);
	
	public ApiSourceBuilder() {
		super("");
		this.name="";
	}
	
	public ApiSourceBuilder(String name) {
		super("");
		this.name=name;
	}
	

	@Override
	public void generate(BaseProject project, SourceConfig config, Entity entity) {
		//BeetlSQl中的配置
		Beetl beetl = ((BeetlTemplateEngine)config.getSqlManager().getSqlTemplateEngine()).getBeetl();
		//模板
		Template template = groupTemplate.getTemplate(mapperTemplate);
		template.binding("entity", entity);
		template.binding("modelName", entity.getTableName());
		template.binding("tableName", entity.getTableName());
		template.binding("table", entity.getTableName());
		template.binding("comment", entity.getComment());
		
		template.binding("package", project.getBasePackage(this.name));
		template.binding("className", entity.getName());
		template.binding("entityName", String.format("%s.model.%s",project.getBasePackage(null), entity.getName()));
		template.binding("requestMapping",StringKit.toLowerCaseFirstOne(entity.getName()));
		
		
		template.binding("nc", config.getSqlManager().getNc());
		template.binding("PS", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_START"));
		template.binding("PE", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_END"));
		template.binding("SS", beetl.getPs().getProperty("DELIMITER_STATEMENT_START"));
		template.binding("SE", beetl.getPs().getProperty("DELIMITER_STATEMENT_END"));
		String apiFileName = entity.getName()+"Controller.java";
		
		String rsName="api";
		if(!StringUtils.isEmpty(this.name)) {
			rsName=String.format("%s.%s",this.name,rsName);
			template.binding("entityName", String.format("%s.%s.model.%s",project.getBasePackage(null), this.name,entity.getName()));
			template.binding("requestMapping",String.format("%s/%s",this.name.replaceAll("\\.", "/"),StringKit.toLowerCaseFirstOne(entity.getName())));
		}
		
		
		Writer writer = project.getWriterByName(rsName,apiFileName);
		template.renderTo(writer);
	}



}
