package com.unione.cloud.form.data.storage.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据提交对象")
public class DataCommit {
	
	@Schema(title="数据定义编码",description= "")
	private String dsn;
	
	@Schema(title="数据定义版本号",description= "")
	private Integer vers;
	
	@Schema(title="数据主键")
	private Long id;
	
	@Schema(title="主键集合")
	private List<Long> ids=new ArrayList<>();
	
	@Schema(title="表单数据对象",description= "嵌套表单则是对象方式提交，如：user:{age,addr}")
	private Map<String, Object> data=new HashMap<>();
	
	@Schema(title="表单参数对象")
	private Map<String, Object> params=new HashMap<>();

}
