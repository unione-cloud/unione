package com.unione.cloud.form.page.dto;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.form.data.dto.DataDefineDto.ConditionStyleDto;
import com.unione.cloud.form.data.dto.DataDefineDto.DataConvertDto;
import com.unione.cloud.form.data.dto.DataDefineDto.DataParamDto;
import com.unione.cloud.form.data.dto.DataDefineDto.DataQueryDto;
import com.unione.cloud.form.data.dto.DataDefineDto.DataRuleDto;
import com.unione.cloud.form.data.dto.DataDefineDto.DataSortDto;
import com.unione.cloud.form.data.dto.DataDefineDto.FieldWidgetDto;
import com.unione.cloud.form.data.dto.DataDefineDto.ForeignKeyDto;
import com.unione.cloud.form.page.dto.PageDefineDto.PageConfigDto;
import com.unione.cloud.form.page.model.SysPageDefine;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@ApiModel("页面定义Dto")
public class PageDefineDto<T extends PageConfigDto> extends SysPageDefine{
	/**
	 * 
	 */
	private static final long serialVersionUID = -177492027121128764L;

	@JsonProperty("configs")
	@ApiModelProperty("页面配置对象")
	private T configDto;
	
	
	@JsonIgnore
	@SuppressWarnings("unchecked")
	public PageDefineDto<T> setConfigs(String configs) {
		super.setConfigs(configs);
		Type[] types = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments();
		configDto=JSONUtil.toBean(configs, (Class<T>)types[0]);
		return this;
	}
	
	@JsonIgnore
	public String getConfigs() {
		if(configDto!=null) {
			super.setConfigs(JSONUtil.toJsonStr(configDto));
		}
		return super.getConfigs();
	}
	
	
	
	@Data
	@ApiModel("页面配置DTO")
	public static class PageConfigDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7542938311504351628L;

		
		@ApiModelProperty("页面组件集合")
		@JsonDeserialize(using = WidgetDeserializer.class)
		private List<WidgetDto> widgets;
		
		@ApiModelProperty("数据模型sn集合")
		private List<String> dsnList;
		
		@JsonProperty(access = Access.READ_ONLY)
		@ApiModelProperty(value="页面权限",notes = "页面组件权限,key:组件id,value:权限定义")
		private Map<String, WidgetPermisDto> permis;
		
		@ApiModelProperty("页面设置对象")
		private PageSettingDto setting;
	}
	
	@Data
	@ApiModel("页面设置DTO")
	public static class PageSettingDto implements Serializable{
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
	
	
	
	@Data
	@ApiModel("Form页面定义Dto")
	public static class FormPageDefineDto extends PageDefineDto<FormPageConfigDto>{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3059345850174302894L;
		
	}
	
	
	@Data
	@ApiModel("Form页面配置Dto")
	public static class FormPageConfigDto extends PageConfigDto{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8234983161647019935L;
		
		@ApiModelProperty(value="按钮列表",notes = "页面按钮列表")
		private List<ButtonDto> btns;
	
		@ApiModelProperty("表单事件")
		private FormEventDto event;
		
	}
	
	
	@Data
	@ApiModel("表单事件Dto")
	public static class FormEventDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8532953889812739800L;

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
	@ApiModel("list页面定义Dto")
	public static class ListPageDefineDto extends PageDefineDto<ListPageConfigDto>{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3059345850174302894L;
		
		
		
	}
	
	
	@Data
	@ApiModel("Table页面配置Dto")
	public static class ListPageConfigDto extends PageConfigDto{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8134983161647019935L;
		
		
		
		
	}
	
	
	
	
	
	
	@Data
	@ApiModel("组件定义Dto")
	public static class WidgetDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1203629620171596812L;

		@ApiModelProperty("组件id")
		private String wid;
		
		@ApiModelProperty("组件名称")
		private String widget;
		
		@ApiModelProperty("组件渲染器")
		private String render;
		
		@ApiModelProperty("组件标题")
		private String title;
		
		@ApiModelProperty("样式设置")
		private CssDto css;
	
		@ApiModelProperty(value="子组件集合")
		public List<WidgetDto> getWidgets(){
			return null;
		}
	}
	
	/**
	 * 组件反序列化器
	 * 1、根据组件名称 widget 动态反序列化成对应的组件对象
	 */
	@Slf4j
	public static class WidgetDeserializer extends JsonDeserializer<Object>{
		
		public Object deserialize(JsonParser parser,JsonNode node) throws JsonProcessingException {
			String widget = node.get("widget").asText();
			switch (widget) {
			case "unione-button":
				return parser.getCodec().treeToValue(node, ButtonDto.class);
			case "unione-form-item":
				return parser.getCodec().treeToValue(node, FormItemDto.class);
			case "unione-form":
				return parser.getCodec().treeToValue(node, FormWidgetDto.class);
			case "unione-query":
				return parser.getCodec().treeToValue(node, QueryWidgetDto.class);
			case "unione-table":
				return parser.getCodec().treeToValue(node, TableWidgetDto.class);
			default:
				log.error("组件名称,widget:"+widget+",未注册", parser.currentValue());
				throw new ServiceException("组件名称,widget:"+widget+",未注册");
			}
		}
		
		@Override
		public Object deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			if(node.isArray()) {
				List<Object> list=new ArrayList<>();
				JsonNode item = null;
				while((item = node.elements().next())!=null) {
					list.add(deserialize(parser,item));
				}
				return list;
			}else {
				return deserialize(parser,node);
			}
		}
	}
	
	
	@Data
	@ApiModel("表单组件配置Dto")
	public static class FormWidgetDto extends WidgetDto {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5448770098453865691L;
		
		@ApiModelProperty(value="是否主表单",notes = "一个表单页面有且只有一个主表单")
		private boolean isPrimarry;

		@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
		private String dsn;
		
		@ApiModelProperty("表单项/控件集合")
		private List<WidgetDto> widgets;

	}
	
	
	@Data
	@ApiModel("表单组件配置DTo")
	public static class FormWidgetPropsDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5448771098453865691L;
		
		@ApiModelProperty(value="表单显示列数",notes = "表单显示列数，默认3")
		private Integer showColumn;
		
		@ApiModelProperty(value="表单显示列数",notes = "表单项label显示宽度，默认9")
		private Integer labelWidget;
		
	}
	
	
	@Data
	@ApiModel("查询组件配置Dto")
	public static class QueryWidgetDto extends WidgetDto {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5448770098453865691L;

		@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
		private String dsn;

		//TODO	补全组件定义
		
		
		
	}
	

	@Data
	@ApiModel("查询组件配置DTo")
	public static class QueryWidgetPropsDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4611136589521983464L;
		
		
	}
	
	
	
	@Data
	@ApiModel("表格组件配置Dto")
	public static class TableWidgetDto extends WidgetDto {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3079809143371203058L;
		
		
		@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
		private String dsn;
		
		@ApiModelProperty(value="表格列模型")
		private List<TableColumnDto> columns;
		
		@ApiModelProperty(value="分页配置")
		private TablePaginationDto pagination;
		
		@ApiModelProperty(value="是否开启复选框")
		private boolean selection;
		
		@ApiModelProperty(value="左侧按钮列表",notes = "列表左侧按钮列表")
		private List<ButtonDto> leftBtns;
		
		@ApiModelProperty(value="右侧按钮列表",notes = "右表左侧按钮列表")
		private List<ButtonDto> rightBtns;
		
		@ApiModelProperty("行号设置")
		private TableRownumDto rownum;
		
		@ApiModelProperty("操作设置")
		private TableOperationDto operation;

	}
	
	@Data
	@ApiModel("表格分页配置DTO")
	public static class TableColumnDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1290335572266633088L;
		
		@ApiModelProperty(value="字段标题",notes = "")
		private String title;
		
		@ApiModelProperty(value="字段名称",notes = "")
		private String name;
		
		@ApiModelProperty(value="是否主键")
		private boolean isPk;
		
		@ApiModelProperty(value="数据格式",notes = "数值类型/日期类型:显示格式")
		private String dataFormat;
		
		@ApiModelProperty(value="组件设置")
		private FieldWidgetDto widget;
		
		@ApiModelProperty(value="外键设置")
		private ForeignKeyDto fkey;
		
		@ApiModelProperty("字段排序")
		private DataSortDto sort;
		
		@ApiModelProperty("字段搜索")
		private DataQueryDto query;

		@ApiModelProperty(value="数据转换")
		private DataConvertDto convert;
		
		@ApiModelProperty(value="条件样式")
		private List<ConditionStyleDto> conditionStyle;
		
		@ApiModelProperty(value="固定方式",notes = "默认：left,可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：center,可选：'center' | 'left' | 'right'")
		private String align;
		
		@ApiModelProperty("显示宽度")
		private Integer width;
		
		@ApiModelProperty(value = "行合并开关",notes = "数据列表页面")
		private boolean rowMergeEnable;
		
		@ApiModelProperty(value = "列合并开关",notes = "数据列表页面")
		private boolean colMergeEnable;
		
	}
	
	@Data
	@ApiModel("表格分页配置DTO")
	public static class TablePaginationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1333732908409635861L;

		@ApiModelProperty("自定义记录总数显示逻辑")
		private String showTotalScript;
		
		@ApiModelProperty(value="每页记录数量",notes = "默认：10")
		private Integer pageSize;
		
		@ApiModelProperty(value="指定每页可以显示多少条",notes = "eg：['10', '20', '50', '100']")
		private List<Integer> pageSizeOptions;
		
		@ApiModelProperty(value="显示位置",notes = "")
		private String position;
		
		@ApiModelProperty(value = "分页大小",notes = "可选:large | middle | small")
		private String   size;
		
		@ApiModelProperty(value="当 size 未指定时，根据屏幕宽度自动调整尺寸",notes = "")
		private boolean responsive;
		
		@ApiModelProperty(value="当添加该属性时，显示为简单分页",notes = "")
		private boolean simple;
	}
	
	
	@Data
	@ApiModel("表格行号配置DTO")
	public static class TableRownumDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8764883326870087401L;

		@ApiModelProperty(value="显示标题",notes = "默认：序号")
		private String title;
		
		@ApiModelProperty(value="固定方式",notes = "默认：left,可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：center,可选：'center' | 'left' | 'right'")
		private String align;
		
		@ApiModelProperty("显示宽度")
		private Integer width;
		
		@ApiModelProperty("是否显示")
		private boolean visible=true;
	}
	
	
	@Data
	@ApiModel("表格行号配置DTO")
	public static class TableOperationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 296826468569608136L;

		@ApiModelProperty(value="显示标题",notes = "默认：操作")
		private String title;
		
		@ApiModelProperty(value="固定方式",notes = "默认：right,可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：center,可选：'center' | 'left' | 'right'")
		private String align;
		
		@ApiModelProperty(value="显示位置",notes = "默认最后面")
		private Integer index;
		
		@ApiModelProperty("显示宽度")
		private Integer width;
		
		@ApiModelProperty("显示操作数量")
		private Integer count;
		
		@ApiModelProperty("是否显示")
		private boolean visible=true;
		
		@ApiModelProperty("操作按钮列表")
		private List<ButtonDto> btns;
		
		@ApiModelProperty("更多设置")
		private MoreOperationDto more;
	}
	
	@Data
	@ApiModel("更多操作配置DTO")
	public static class MoreOperationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5609162190698708400L;

		@ApiModelProperty(value="标题",notes = "默认：更多")
		private String title;
		
		@ApiModelProperty(value="触发方式",notes = "默认：hover，可选：click|hover|contextmenu")
		private String trigger;
		
		@ApiModelProperty(value = "按钮size大小",notes = "可选：large | middle | small")
		private String size;
		
		@ApiModelProperty(value = "显示布局",notes = "可选：vertical | horizontal")
		private String layout;
		
	}
	
	
	@Data
	@ApiModel("表格组件属性DTO")
	public static class TableWidgetPropsDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8092409023579026643L;
		
		
	}
	
	
	@Data
	@ApiModel("表单项/控件Dto")
	public static class FormItemDto extends WidgetDto {
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
		
		@ApiModelProperty(value="是否不能为空")
		private boolean isNull;
		
		@ApiModelProperty(value="输入提示")
		private String placeholder;
		
		@ApiModelProperty(value="输入帮助")
		private String help;
		
		@ApiModelProperty(value="数据格式",notes = "数值类型/日期类型:显示格式")
		private String dataFormat;
		
		@ApiModelProperty(value="隐藏表单",notes = "特指：新增表单：add，修改表单:edit，详情表单:view，中是否隐藏，为空时不受限，不为空时指定表单隐藏")
		private List<String> hidden;
		
		@ApiModelProperty(value="只读表单",notes = "特指：新增表单：add，修改表单:edit，详情表单:view，中是否只读，为空时所有表单可写，不为空时指定表单只读")
		private List<String> readonly;
		
		@ApiModelProperty(value="表单项事件")
		private FormItemEventDto event;
		
		@ApiModelProperty(value="组件验证规则",notes = "表单组件验证规则")
		private FormRuleDto rules;
		
		@ApiModelProperty(value="外键设置")
		private ForeignKeyDto fkey;
		
		@ApiModelProperty(value="数据转换")
		private DataConvertDto convert;
		
		@ApiModelProperty(value="条件样式")
		private List<ConditionStyleDto> conditionStyle;
		
	}
	
	
	@Data
	@ApiModel("表单项事件Dto")
	public static class FormItemEventDto implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6780968062940236797L;

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
	public static class FormRuleDto extends DataRuleDto{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2149358082168501860L;
		
		@ApiModelProperty(value="触发事件",notes = "change,blur")
		private String trigger;
		
		
	}
	
	
	@Data
	@ApiModel("表单项配置  DTO")
	public static class FormItemPropsDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2367848605872325522L;

		@ApiModelProperty(value="是否必填")
		private Boolean required;
		
		@ApiModelProperty(value="组件提示信息")
		private String placeholder;
		
		
	}
	
	
	@Data
	@ApiModel("按钮组件DTO")
	public static class ButtonDto extends WidgetDto {
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
		
		@ApiModelProperty("按钮属性")
		private ButtonPropsDto props;
	}
	
	
	
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
	public static class ButtonPropsDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8392444232066625861L;

		@ApiModelProperty(value = "按钮icon图标")
		private String icon;
		
		@ApiModelProperty(value = "按钮shape形状",notes = "可选:default | circle | round")
		private String shape;
		
		@ApiModelProperty(value = "按钮type类型",notes = "可选:primary | ghost | dashed | link | text | default")
		private String type;
		
		@ApiModelProperty(value = "按钮size大小",notes = "可选:large | middle | small")
		private String size;
		
		@ApiModelProperty(value = "按钮trigger事件触发",notes = "可选:large | middle | small")
		private String trigger;
		
		@ApiModelProperty("是否危险按钮")
		private boolean danger;
		
		@ApiModelProperty(value="自适应按钮",notes = "将按钮宽度调整为其父宽度的选项")
		private boolean block;
		
		@ApiModelProperty(value="幽灵按钮",notes = "使按钮背景透明")
		private boolean ghost;
		
	}
	
	
	
	
	
	
	
	@Data
	@ApiModel("样式设置DTo")
	public static class CssDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -562216706109195212L;

		@ApiModelProperty(value="样式名称")
		private String cssName;
		
		@ApiModelProperty(value="样式定义")
		private String cssText;
		
		@ApiModelProperty(value="样式属性",notes = "字体大小:fontSize,字体颜色:color等等")
		private Map<String, String> props;
		
	}
	
	@ApiModel("组件权限定义DTO")
	public static enum WidgetPermisDto{
		WRITE,READ,NONE
	}
	
	
}








