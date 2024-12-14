package com.unione.cloud.form.data.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据转换结果选项")
public class DataConvertOption {
	
	@ApiModelProperty("选项id")
	private Long id;
	
	@ApiModelProperty("选项pid")
	private Long pid;
	
	@ApiModelProperty("选项value")
	private String value;
	
	@ApiModelProperty("选项label")
	private String label;
	
	@ApiModelProperty("选项属性")
	private Map<String, Object> props=new HashMap<>();
	
	@ApiModelProperty("子选项集合")
	private List<DataConvertOption> children=new ArrayList<>();

}
