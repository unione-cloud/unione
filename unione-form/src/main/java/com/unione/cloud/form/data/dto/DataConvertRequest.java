package com.unione.cloud.form.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据转换请求对象")
public class DataConvertRequest {
	
	@ApiModelProperty(value="数据项记录ID")
	private Long id;
	
	@ApiModelProperty(value="数据项父级ID")
	private Long pid;

	@ApiModelProperty(value="搜索关键字")
	private String keywords;
	
	@ApiModelProperty(value="转换value")
	private String value;
	
}
