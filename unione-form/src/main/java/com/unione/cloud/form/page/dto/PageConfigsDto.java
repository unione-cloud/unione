package com.unione.cloud.form.page.dto;

import java.util.List;

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
	
}








