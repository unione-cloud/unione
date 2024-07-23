package com.unione.cloud.form.page.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("事件模型Dto")
public class EventModelDto {

	private Boolean clickEnable;
	private String clickScript;
	private String clickHelp;
	
	private Boolean titleEnable;
	private String titleScript;
	private String titleHelp;
	
	private Boolean disableEnable;
	private String disableScript;
	private String disableHelp;
	
	private Boolean visibleEnable;
	private String visibleScript;
	private String visibleHelp;
	
	private Boolean requiredEnable;
	private String requiredScript;
	private String requiredHelp;
	
	private Boolean changeEnable;
	private String changeScript;
	private String changeHelp;
	
}
