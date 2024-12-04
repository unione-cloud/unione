package com.unione.cloud.form.page.dto;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("组件定义Dto")
public abstract class WidgetDefineDto<T> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1203629620171596812L;

	@ApiModelProperty("组件id")
	private String wid;
	
	@ApiModelProperty("组件名称")
	private String widget;
	
	@ApiModelProperty("组件渲染器")
	private String render;
	
	@ApiModelProperty("组件标题")
	private String title;
	
	@ApiModelProperty("样式设置")
	private CssDto css;
	
	
	@ApiModelProperty(value="组件属性")
	private T configs;
	
	
	@Data
	@ApiModel("样式设置DTo")
	public static class CssDto{
		
		@ApiModelProperty(value="样式名称")
		private String cssName;
		
		@ApiModelProperty(value="样式定义")
		private String cssText;
		
		@ApiModelProperty(value="样式属性",notes = "字体大小:fontSize,字体颜色:color等等")
		private Map<String, String> props;
		
	}
	
	
}

