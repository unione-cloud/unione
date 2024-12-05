package com.unione.cloud.form.page.dto;

import java.io.Serializable;
import java.util.List;

import com.unione.cloud.form.data.dto.DataDefineDto.DataParamDto;
import com.unione.cloud.form.page.dto.ButtonDefineDto.ButtonWidgetConfigDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("按钮组件DTO")
public class ButtonDefineDto extends WidgetDefineDto<ButtonWidgetConfigDto>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4064275584042514224L;

	@ApiModelProperty(value="按钮名称",notes = "用户识别按钮点击事件名称，在一个组件内部，应该要保持唯一")
	private String name;
	
	@ApiModelProperty("是否显示")
	private Boolean visible;
	
	@ApiModelProperty("是否禁用")
	private Boolean disabled;
	
	@ApiModelProperty("响应设置")
	private ButtonActionDto action;
	
	@ApiModelProperty("按钮事件")
	private ButtonEventDto event;
	
	@ApiModelProperty(value="显示顺序",notes = "按钮显示顺序")
	private Integer index;
	
	@Data
	@ApiModel("按钮响应设置DTO")
	public static class ButtonActionDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3653219835676601174L;

		@ApiModelProperty(value = "响应方式",notes = "响应方式：link，route，dialog，drawer")
		private String type;
		
		@ApiModelProperty(value = "连接url",notes = "支持变量，动态设置参数")
		private String href;
		
		@ApiModelProperty(value = "连接target",notes = "连接跳转方式 _self,_blank")
		private String target;
		
		@ApiModelProperty(value = "抽屉显示位置",notes = "抽屉：左侧：left，右侧：right")
		private String position;
		
		@ApiModelProperty(value = "点击遮罩层是否关闭",notes = "抽屉，对话框有效")
		private boolean maskClosable;
		
		@ApiModelProperty(value = "响应组件名称",notes = "预留属性")
		private String component;
		
		@ApiModelProperty(value = "页面编码",notes = "预留属性")
		private String psn;
		
		@ApiModelProperty(value = "响应标题",notes = "对话框，抽屉")
		private String title;
		
		@ApiModelProperty(value = "显示宽度",notes = "对话框，抽屉，单位：px,%,vw")
		private String width;
		
		@ApiModelProperty(value = "显示高度",notes = "对话框单位：px,%,vw")
		private String height;
		
		@ApiModelProperty(value = "响应参数集合",notes = "")
		private List<DataParamDto> params;
		
	}
	
	@Data
	@ApiModel("按钮事件Dto")
	public static class ButtonEventDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8084738054778023214L;

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
	@ApiModel("事件定义Dto")
	public static class EventDefineDto implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2883289916188473146L;

		@ApiModelProperty("是否启用")
		private boolean enable;
		
		@ApiModelProperty("事件处理脚本")
		private String  scriptText;
		
		@ApiModelProperty("事件脚本说明")
		private String  scriptHelp;
		
	}
	
	@Data
	@ApiModel("按钮属性DTO")
	public static class ButtonWidgetConfigDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8392444232066625861L;

		@ApiModelProperty(value = "按钮icon图标")
		private String icon;
		
		@ApiModelProperty(value = "按钮shape形状",notes = "可选:default | circle | round")
		private String   shape;
		
		@ApiModelProperty(value = "按钮type类型",notes = "可选:primary | ghost | dashed | link | text | default")
		private String   type;
		
		@ApiModelProperty(value = "按钮size大小",notes = "可选:large | middle | small")
		private String   size;
		
		@ApiModelProperty(value = "按钮trigger事件触发",notes = "可选:large | middle | small")
		private String trigger;
		
		@ApiModelProperty("是否危险按钮")
		private boolean danger;
		
		@ApiModelProperty(value="自适应按钮",notes = "将按钮宽度调整为其父宽度的选项")
		private boolean block;
		
		@ApiModelProperty(value="幽灵按钮",notes = "使按钮背景透明")
		private boolean ghost;
	}
	
	
	
}
