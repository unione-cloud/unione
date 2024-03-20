package com.unione.cloud.portal.codegen;

import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import org.beetl.core.Template;
import org.beetl.sql.clazz.kit.StringKit;
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

	@Override
	public void generate(BaseProject project, SourceConfig config, Entity entity) {
		//BeetlSQl中的配置
		Beetl beetl = ((BeetlTemplateEngine)config.getSqlManager().getSqlTemplateEngine()).getBeetl();
		//模板
		Template template = groupTemplate.getTemplate(mapperTemplate);
		template.binding("tableName", entity.getTableName());
		template.binding("cols", entity.getCols());
		
		template.binding("props", entity.getList());
		List<Attribute> idAttrs = entity.getList().stream().filter(f->f.isId()).collect(Collectors.toList());
		AssertUtil.database()
			.isTrue(!idAttrs.isEmpty(), "数据库表["+entity.getTableName()+"]未设置主键")
			.isTrue(idAttrs.size()==1, "数据库表["+entity.getTableName()+"]是符合主键，不支持复合主键");
		template.binding("idAttr", idAttrs.get(0));
		
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
		String mdFileName = StringKit.toLowerCaseFirstOne(entity.getName())+".md";
		Writer writer = project.getWriterByName(this.name,mdFileName);
		template.renderTo(writer);
	}

}
