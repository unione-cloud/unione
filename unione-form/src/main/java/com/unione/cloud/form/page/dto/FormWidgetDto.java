package com.unione.cloud.form.page.dto;

import java.io.Serializable;
import java.util.List;

import com.unione.cloud.form.page.dto.FormWidgetDto.FormWidgetConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表单组件配置Dto")
public class FormWidgetDto extends WidgetDefineDto<FormWidgetConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5448770098453865691L;
	
	@ApiModelProperty(value="是否主表单",notes = "一个表单页面有且只有一个主表单")
	private boolean isPrimarry;

	@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
	private String dsn;
	
	@ApiModelProperty("表单项/控件集合")
	private List<WidgetDefineDto<?>> widgets;
	
	
	@Data
	@ApiModel("表单组件配置DTo")
	public static class FormWidgetConfig implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5448771098453865691L;
		
		@ApiModelProperty(value="表单显示列数",notes = "表单显示列数，默认3")
		private Integer showColumn;
		
		@ApiModelProperty(value="表单显示列数",notes = "表单项label显示宽度，默认9")
		private Integer labelWidget;
		
	}

}
