package com.unione.cloud.form.page.dto;

import java.util.List;

import com.unione.cloud.form.data.dto.DataModelDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("页面配置Dto")
public class PageConfigsDto {

	@ApiModelProperty("页面组件集合")
	private List<WidgetModelDto> widgetList;
	
	@ApiModelProperty("数据模型集合")
	private List<DataModelDto> dataModels;
	
	@ApiModelProperty("页面样式定义")
	private String css;
	
	@ApiModelProperty("页面设置")
	private Setting settings;
	
	
	
	
	
	@Data
	public static class Setting{
		@ApiModelProperty("接口上下文")
		private String ctx;
		@ApiModelProperty("接口超时时间")
		private Long timeout;
		@ApiModelProperty(value="平台名称",notes = "pc,app")
		private String platform;
	}
	
	
}








