package com.unione.cloud.form.page.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("组件模型Dto")
public class WidgetModelDto {

	@ApiModelProperty("组件id")
	private String wid;
	
	@ApiModelProperty("组件名称")
	private String widget;
	
	@ApiModelProperty("组件渲染器")
	private String render;
	
	@ApiModelProperty("组件标题")
	private String title;
	
	@ApiModelProperty("是否隐藏")
	private Boolean isHide;
	
	@ApiModelProperty("组件configs")
	private Map<String, Object> configs;
	
	
	/**
	 * 	转换configs
	 * @param <T>
	 * @param cla
	 * @return
	 */
	@JsonIgnore
	public <T> T transform(Class<T> cla) {
		return BeanUtil.toBean(configs, cla);
	}
	
	
	
	
	
}
