package com.unione.cloud.form.page.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.unione.cloud.form.page.dto.PageDefineDto.PageConfigDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("页面定义Dto")
public class PageDefineDto<T extends PageConfigDto> extends WidgetDefineDto<T>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -177492027121128764L;

	@ApiModelProperty("页面组件集合")
	private List<WidgetDefineDto<?>> widgets;
	
	@ApiModelProperty("数据模型sn集合")
	private List<String> dsnList;
	
	
	@JsonProperty(access = Access.READ_ONLY)
	@ApiModelProperty(value="页面权限",notes = "页面组件权限,key:组件id,value:权限定义")
	private Map<String, WidgetPermisDto> permis;
	
	
	@Data
	@ApiModel("页面配置DTO")
	public static class PageConfigDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8234983161647012935L;
		
		@ApiModelProperty("接口上下文")
		private String ctx;
		@ApiModelProperty("接口超时时间")
		private Long timeout;
		@ApiModelProperty(value="平台名称",notes = "pc,app")
		private String platform;
	}
	
	
	@ApiModel("组件权限定义DTO")
	public static enum WidgetPermisDto{
		WRITE,READ,NONE
	}
	
	
}








