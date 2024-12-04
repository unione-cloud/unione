package com.unione.cloud.form.page.dto;

import java.util.List;

import com.unione.cloud.form.page.dto.ButtonDefineDto.EventDefineDto;
import com.unione.cloud.form.page.dto.FormPageDto.FormPageConfigDto;
import com.unione.cloud.form.page.dto.PageDefineDto.PageConfigDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表单页面定义Dto")
public class FormPageDto extends PageDefineDto<FormPageConfigDto>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8234983161647019935L;
	
	@ApiModelProperty(value="按钮列表",notes = "页面按钮列表")
	private List<ButtonDefineDto> btns;
	
	@Data
	@ApiModel("表单事件Dto")
	public static class FormEventDto {
		
		@ApiModelProperty(value="表单验证事件",notes = "自定义表单验证逻辑")
		private EventDefineDto onValidate;
		
		@ApiModelProperty(value="表单显示事件",notes="动态显示事件，根据逻辑动态显示表单")
		private EventDefineDto visible;
		
		@ApiModelProperty(value="表单保存前置事件",notes="提交表单数据前触发的事件")
		private EventDefineDto onPrepSave;
		
		@ApiModelProperty(value="表单保存后置事件",notes="表单保存成功后触发的事件")
		private EventDefineDto onPostSaved;
		
	}
	
	
	@Data
	@ApiModel("表单页面显示配置")
	public static class FormPageConfigDto extends PageConfigDto{

		
		
	}
	
	
}
