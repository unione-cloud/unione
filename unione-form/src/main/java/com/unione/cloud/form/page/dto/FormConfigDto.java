package com.unione.cloud.form.page.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表单配置Dto")
public class FormConfigDto {
	
	@ApiModelProperty(value="子组件集合",notes = "表单组件，布局组件等包含子组件")
	private List<WidgetModelDto> widgets;
	
	@ApiModelProperty(value="数据模型编码",notes = "表单关联的数据模型编码")
	private String dataModel;
	
	@ApiModelProperty("表单设置")
	private Setting setting;
	
	
	
	
	
	
	@Data
	public static class Setting{
		@ApiModelProperty(value="显示列数",notes = "表单页面显示列数，默认3")
		private Integer showColumn;
		@ApiModelProperty(value="label显示宽度",notes = "表单控件label显示宽度，默认9")
		private Integer labelWidget;
		@ApiModelProperty(value="平台名称",notes = "pc,app")
		private String platform;
	}
	
}
