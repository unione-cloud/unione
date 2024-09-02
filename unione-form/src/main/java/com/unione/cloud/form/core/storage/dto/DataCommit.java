package com.unione.cloud.form.core.storage.dto;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "数据提交对象")
public class DataCommit {
	
	@ApiModelProperty(value="数据主键")
	private Long id;
	
	@ApiModelProperty(value="主键集合")
	private List<Long> ids;
	
	@ApiModelProperty(value="表单数据对象",notes = "嵌套表单则是对象方式提交，如：user:{age,addr}")
	private Map<String, Object> data;
	
	@ApiModelProperty(value="表单参数对象")
	private Map<String, Object> params;

}
