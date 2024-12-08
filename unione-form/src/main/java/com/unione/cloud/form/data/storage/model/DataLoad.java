package com.unione.cloud.form.data.storage.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "数据加载对象")
public class DataLoad {
	
	@ApiModelProperty(value="数据定义编码",notes = "")
	private String dsn;
	
	@ApiModelProperty(value="数据定义版本号",notes = "")
	private Integer vers;
	
	@ApiModelProperty(value="数据主键")
	private Long id;
	
	@ApiModelProperty(value="主键集合")
	private List<Long> ids=new ArrayList<>();
	
	@ApiModelProperty(value="字段集合",notes = "字段别名：驼峰")
	private List<String> fields=new ArrayList<>();

}
