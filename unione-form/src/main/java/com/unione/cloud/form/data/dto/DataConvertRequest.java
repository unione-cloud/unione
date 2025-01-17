package com.unione.cloud.form.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="数据转换请求对象")
public class DataConvertRequest {
	
	@Schema(title="数据项记录ID")
	private Long id;
	
	@Schema(title="数据项父级ID")
	private Long pid;

	@Schema(title="搜索关键字")
	private String keywords;
	
	@Schema(title="转换value")
	private String value;
	
}
