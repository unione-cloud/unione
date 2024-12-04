package com.unione.cloud.form.page.dto;

import java.util.List;

import com.unione.cloud.form.page.dto.ButtonDefineDto.EventDefineDto;
import com.unione.cloud.form.page.dto.FormItemDto.FormItemConfigDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表单项/控件Dto")
public class FormItemDto extends WidgetDefineDto<FormItemConfigDto> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -28563820147504303L;
	
	@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
	private String dsn;

	@ApiModelProperty(value="组件name",notes = "数据绑定字段名称")
	private String name;
	
	@ApiModelProperty(value="组件初始值")
	private String value;
	
	
	
	@ApiModelProperty(value="隐藏表单",notes = "特指：新增表单：add，修改表单:edit，详情表单:view，中是否隐藏，为空时不受限，不为空时指定表单隐藏")
	private List<String> hidden;
	
	@ApiModelProperty(value="只读表单",notes = "特指：新增表单：add，修改表单:edit，详情表单:view，中是否只读，为空时所有表单可写，不为空时指定表单只读")
	private List<String> readonly;
	
	@ApiModelProperty(value="表单项事件")
	private FormItemEventDto event;
	
	@ApiModelProperty(value="组件验证规则",notes = "表单组件验证规则")
	private FormRuleDto rules;
	
	
	
	@Data
	@ApiModel("表单项事件Dto")
	public static class FormItemEventDto {
		
		@ApiModelProperty(value="点击事件",notes = "按钮点击后触发的脚本")
		private EventDefineDto click;
		
		@ApiModelProperty(value="标题事件",notes="动态标题事件，根据逻辑动态显示按钮标题")
		private EventDefineDto title;
		
		@ApiModelProperty(value="禁用事件",notes="动态禁用事件，根据逻辑动态禁用按钮")
		private EventDefineDto disable;
		
		@ApiModelProperty(value="显示事件",notes="动态显示事件，根据逻辑动态显示按钮")
		private EventDefineDto visible;
		
	}
	
	
	@Data
	@ApiModel("规则Dto")
	public static class FormRuleDto{
		
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
	
	
	@Data
	@ApiModel("表单项配置  DTO")
	public static class FormItemConfigDto{
		
		@ApiModelProperty(value="是否必填")
		private Boolean required;
		
		@ApiModelProperty(value="组件提示信息")
		private String placeholder;
		
		
	}
	
	
	
}
