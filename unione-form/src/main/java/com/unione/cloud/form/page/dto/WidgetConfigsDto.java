package com.unione.cloud.form.page.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("组件配置Dto")
public class WidgetConfigsDto {

	private String name;
	
	private String label;
	
	private String placeholder;
	
	private Boolean required;
	
	private String value;
	
	private String className;
	
	private String style;
	
	private Object attrs;
	
	private EventModelDto event;
	
	private List<Object> rules;
	
}
