package com.unione.cloud.portal.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DictShowDto {
	
	@ApiModelProperty(value="显示类型",notes = "text,tag")
	private String type;
	
	@ApiModelProperty(value="显示颜色",notes = "颜色16进制码")
	private String color;
}
