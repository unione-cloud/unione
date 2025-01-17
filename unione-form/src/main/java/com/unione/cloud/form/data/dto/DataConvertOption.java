package com.unione.cloud.form.data.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="数据转换结果选项")
public class DataConvertOption {
	
	@Schema(title="选项id")
	private Long id;
	
	@Schema(title="选项pid")
	private Long pid;
	
	@Schema(title="选项value")
	private String value;
	
	@Schema(title="选项label")
	private String label;
	
	@Schema(title="选项属性")
	private Map<String, Object> props;
	
	@Schema(title="子选项集合")
	private List<DataConvertOption> children;

}
