package com.unione.cloud.portal.codegen;

import java.io.Writer;

import org.beetl.core.Template;
import org.beetl.sql.clazz.kit.StringKit;
import org.beetl.sql.core.engine.template.Beetl;
import org.beetl.sql.core.engine.template.BeetlTemplateEngine;
import org.beetl.sql.gen.BaseProject;
import org.beetl.sql.gen.Entity;
import org.beetl.sql.gen.SourceConfig;
import org.beetl.sql.gen.simple.BaseTemplateSourceBuilder;

public class ApiSourceBuilder extends BaseTemplateSourceBuilder {
	public static  String mapperTemplate="api.btl";
	
	private String packageName="com.unione.cloud";
	
	
	public ApiSourceBuilder() {
		super("api");
	}
	
	

	@Override
	public void generate(BaseProject project, SourceConfig config, Entity entity) {
		//BeetlSQl中的配置
		Beetl beetl = ((BeetlTemplateEngine)config.getSqlManager().getSqlTemplateEngine()).getBeetl();
		//模板
		Template template = groupTemplate.getTemplate(mapperTemplate);
		template.binding("entity", entity);
		template.binding("tableName", entity.getTableName());
		template.binding("cols", entity.getCols());
		template.binding("nc", config.getSqlManager().getNc());
		template.binding("PS", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_START"));
		template.binding("PE", beetl.getPs().getProperty("DELIMITER_PLACEHOLDER_END"));
		template.binding("SS", beetl.getPs().getProperty("DELIMITER_STATEMENT_START"));
		template.binding("SE", beetl.getPs().getProperty("DELIMITER_STATEMENT_END"));
		String apiFileName = StringKit.toLowerCaseFirstOne(entity.getName())+"Controller.java";
		Writer writer = project.getWriterByName(this.name,apiFileName);
		template.renderTo(writer);
	}



}
