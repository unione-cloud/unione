package com.unione.cloud.web.model;

import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LoginRole extends Pojo{
	// fields start
	/**
	 * 名称
	 */
	@ApiModelProperty(value="名称",notes="字符长度为：100")
	private String name;
	/**
	 * 编码
	 */
	@ApiModelProperty(value="编码",notes="字符长度为：50")
	private String codes;
	/**
	 * 类型，字典ROLETYPE 
	 */
	@ApiModelProperty(value="类型，字典ROLETYPE ",notes="字符长度为：10")
	private Integer types;
	// fields end

}
