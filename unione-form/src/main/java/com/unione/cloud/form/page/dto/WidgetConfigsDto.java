package com.unione.cloud.form.page.dto;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("组件配置Dto")
public class WidgetConfigsDto {

	@ApiModelProperty(value="组件name")
	private String name;
	
	@ApiModelProperty(value="组件label")
	private String label;
	
	@ApiModelProperty(value="组件提示信息")
	private String placeholder;
	
	@ApiModelProperty(value="是否必填")
	private Boolean required;
	
	@ApiModelProperty(value="组件初始值")
	private String value;
	
	@ApiModelProperty(value="组件样式名称")
	private String className;
	
	@ApiModelProperty(value="组件样式")
	private String style;
	
	@ApiModelProperty(value="组件属性")
	private Map<String, Object> attrs;
	
	@ApiModelProperty(value="组件事件")
	private EventDto event;
	
	@ApiModelProperty(value="组件验证规则",notes = "表单组件验证规则")
	private RuleDto rules;
	
	@Data
	@ApiModel("事件Dto")
	public static class EventDto {

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
	
	
	@Data
	@ApiModel("规则Dto")
	public static class RuleDto{
		
		@ApiModelProperty(value="触发事件",notes = "change,blur")
		private String trigger;
		
		@ApiModelProperty(value="是否忽略空格",notes = "必填验证开启时，是否忽略空格")
		private Boolean whitespace;
		
		@ApiModelProperty(value="预设规则名称",notes = "手机号：tel，邮箱：email，身份证号：idcard等等")
		private String  advance;
		
		@ApiModelProperty(value="最小值")
		private Integer rangeMin;
		
		@ApiModelProperty(value="最大值")
		private Integer rangeMax;
		
		@ApiModelProperty(value="正则表达式")
		private String regExpress;
		
		@ApiModelProperty(value="正则表达式验证失败消息")
		private String regMessage;
		
	}
	
	
	
}
