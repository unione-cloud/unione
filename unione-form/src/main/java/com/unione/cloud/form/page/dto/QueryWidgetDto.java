package com.unione.cloud.form.page.dto;

import com.unione.cloud.form.page.dto.QueryWidgetDto.QueryWidgetConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询组件配置Dto")
public class QueryWidgetDto extends WidgetDefineDto<QueryWidgetConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5448770098453865691L;

	@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
	private String dsn;
	
	
	
	@Data
	@ApiModel("查询组件配置DTo")
	public static class QueryWidgetConfig{
		
		
	}

}
