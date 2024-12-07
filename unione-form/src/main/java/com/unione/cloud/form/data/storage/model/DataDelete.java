package com.unione.cloud.form.data.storage.model;

import java.util.HashSet;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "数据删除对象")
public class DataDelete {
	
	@ApiModelProperty(value="数据定义编码",notes = "")
	private String dsn;
	
	@ApiModelProperty(value="数据定义版本号",notes = "")
	private Integer vers;
	
	@ApiModelProperty(value="数据主键")
	private Long id;
	
	@ApiModelProperty(value="主键集合")
	private Set<Long> ids=new HashSet<>();

}
