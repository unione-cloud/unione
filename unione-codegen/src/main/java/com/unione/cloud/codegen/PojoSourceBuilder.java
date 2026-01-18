package com.unione.cloud.codegen;

import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.ResourceLoader;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.sql.gen.BaseProject;
import org.beetl.sql.gen.Entity;
import org.beetl.sql.gen.SourceConfig;
import org.beetl.sql.gen.simple.BaseTemplateSourceBuilder;

public class PojoSourceBuilder extends BaseTemplateSourceBuilder {
	/**
	 * 	指定模板的路径
	 */
	public static  String pojoPath = "/codegen/pojo.btl";
	public static  String pojoAliasPath = "/codegen/pojoAlias.btl";
	private static ResourceLoader<String> resourceLoader=new ClasspathResourceLoader("templates");
	
	private boolean alias = false;
	public PojoSourceBuilder() {
		super("");
		this.name="";
	}
	
	public PojoSourceBuilder(String name) {
		super("");
		this.name=name;
	}

	public PojoSourceBuilder(boolean alias) {
		super("");
		this.alias = alias;
		this.name="";
	}

	public void setAlias(boolean alias) {
		this.alias = alias;
	}

	@Override
	public void generate(BaseProject project,SourceConfig config, Entity entity) {

		Template template =alias?groupTemplate.getTemplate(pojoAliasPath,resourceLoader): groupTemplate.getTemplate(pojoPath,resourceLoader);
		template.binding("attrs", entity.getList());
		template.binding("className", entity.getName());
		template.binding("table", entity.getTableName());
		template.binding("parentClass", entity.getParentClass());

		if(!config.isIgnoreDbCatalog()){
			template.binding("catalog",entity.getCatalog());
		}
		
		String sqlResource=entity.getName();
		if(!StringUtils.isEmpty(this.name)) {
			sqlResource=String.format("%s.%s", this.name,entity.getName());
		}
		template.binding("sqlResource", sqlResource);

		template.binding("package", project.getBasePackage(this.name));
		template.binding("imports", entity.getImportPackage());
		template.binding("comment", entity.getComment());

		String rsName="model";
		if(!StringUtils.isEmpty(this.name)) {
			rsName=String.format("%s.%s",this.name,rsName);
		}
		
		Writer writer = project.getWriterByName(rsName,entity.getName()+".java");

		template.renderTo(writer);

	}
}
