package com.unione.cloud.form.data.storage.model;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据删除对象")
public class DataDelete {
	
	@Schema(title="数据定义编码",description= "")
	private String dsn;
	
	@Schema(title="数据定义版本号",description= "")
	private Integer vers;
	
	@Schema(title="数据主键")
	private Long id;
	
	@Schema(title="主键集合")
	private Set<Long> ids=new HashSet<>();

}
