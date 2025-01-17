package com.unione.cloud.form.data.storage.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据加载对象")
public class DataLoad {
	
	@Schema(title="数据定义编码",description= "")
	private String dsn;
	
	@Schema(title="数据定义版本号",description= "")
	private Integer vers;
	
	@Schema(title="数据主键")
	private Long id;
	
	@Schema(title="主键集合")
	private List<Long> ids=new ArrayList<>();
	
	@Schema(title="字段集合",description= "字段别名：驼峰")
	private List<String> fields=new ArrayList<>();

}
