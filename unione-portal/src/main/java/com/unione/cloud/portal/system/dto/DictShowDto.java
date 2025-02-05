package com.unione.cloud.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictShowDto {
	
	@Schema(title="显示类型",description= "text,tag")
	private String type;
	
	@Schema(title="显示颜色",description= "颜色16进制码")
	private String color;
}
