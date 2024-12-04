package com.unione.cloud.form.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据模型Dto")
public class DataDefineDto {

	
	
	
	
	
	
	
	
	@Data
	@ApiModel("数据参数DTO")
	public static class DataParams{
		
		@ApiModelProperty(value="参数标题")
		private String title;
		
		@ApiModelProperty(value="参数名称")
		private String name;
		
		@ApiModelProperty(value="参数默认值")
		private String value;
	}
	
}
