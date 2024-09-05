package com.unione.cloud.form.data.storage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("数据字段配置对象")
public class DataFieldConfig implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8461778044525819895L;
	
	@ApiModelProperty(value = "组件设置")
	private Widget widget;
	
	@ApiModelProperty(value = "转换器设置")
	private Convert convert;
	
	@ApiModelProperty(value = "列表页面设置")
	private Show list;
	
	@ApiModelProperty(value = "详情页面设置")
	private Show view;
	
	@ApiModelProperty(value = "表单页面设置")
	private Form form;
	
	@ApiModelProperty(value = "外籍设置")
	private FKey fkey;
	
	@ApiModelProperty(value = "标签设置")
	private Tag tag;
	
	@ApiModelProperty(value = "排序设置")
	private Sort sort;
	
	@Default
	@ApiModelProperty(value = "验证规则集合")
	private List<Rule> rules=new ArrayList<>();
	
	@Default
	@ApiModelProperty(value = "条件样式集合",notes = "在列表，表单，详情页面都会使用")
	private List<ConditionStyle> conditionStyles=new ArrayList<>();
	
	
	@Data
	@ApiModel(value = "组件设置")
	public static class Widget{
		@ApiModelProperty(value = "组件名称")
		private String name;

		@ApiModelProperty(value = "表格-是否启动前端表头排序")
		private String sortFlag;

		@ApiModelProperty(value = "字段联动绑定的字段名")
		private List<String> refattr;

		@ApiModelProperty(value = "字段联动绑定的字段名")
		private Map<String,String> refattrFildMap;

		@ApiModelProperty(value = "字段联动绑定的字段名")
		private String refattrType;

		@ApiModelProperty(value = "字段状态启用的符合条件数据")
		private Boolean refattrValue;

		@ApiModelProperty(value = "字段联动触发功能 1状态（默认根据上述类型执行操作） 2数据加载（例如：选择orgId重新加载数据中的userId） 3数据设置（例如：选择userId回填该字段数据中的tel）")
		private String refattrAciton;

		@ApiModelProperty(value = "表单-是否只读")
		private Integer isReadOnly;

		@ApiModelProperty(value = "表单-字段必填校验")
		private List<Object> fieldMustInput;

		@ApiModelProperty(value = "前端回填字段配置中的字段默认值 defaultValue")
		private String defaultVal;

		@ApiModelProperty(value = "前端回填字段配置中的 dataLen")
		private Integer fieldLength;

		@ApiModelProperty(value = "日期格式：YYYY-MM-DD HH:mm:ss")
		private String format;

		@ApiModelProperty(value = "显示时分秒")
		private String showTime;

		@ApiModelProperty(value = "机构下拉key")
		private String initKey;

		@ApiModelProperty(value = "机构下拉登记")
		private String treeLevel;

		@ApiModelProperty(value = "标题路径")
		private String titlePath;

		@ApiModelProperty(value = "机构类型 集合")
		private List<Object> orgTypes;

	}
	
	
	
	@Data
	@ApiModel(value = "转换器设置")
	public static class Convert{
		@ApiModelProperty(value = "转换器类型",notes = "dict：字典，option：静态选项")
		private String types;
		
		@ApiModelProperty(value = "字典名称")
		private String dictName;

		@ApiModelProperty(value = "当types为option时候需要显示这个多行文本输入框或高级表格供填写多个选项值 数据例如 [{label:星期1，value:1}，{label:星期2，value:2}]")
		private List<Options> options;

		@Data
		@ApiModel(value = "属性数据")
		public static class Options{

			private String label;

			private String value;
		}
		
	}
	
	
	
	@Data
	@ApiModel(value = "显示设置")
	public static class Show{
		
		@ApiModelProperty(value = "是否可用")
		private boolean enable;
		
		@ApiModelProperty(value = "显示顺序",notes = "默认根据字段列表顺序显示")
		private Integer	index;
		
		@ApiModelProperty(value = "字段显示宽度，栅格布局，默认根据表单设置",notes = "表单设置，详情设置")
		private Integer	showWidth;
		
		@ApiModelProperty(value = "label宽度")
		private String  labelWidth;

		@ApiModelProperty(value = "value宽度")
		private String  valueWidth;
		
	}

	@Data
	@ApiModel(value = "表单显示设置")
	public static class Form extends Show{

		@ApiModelProperty(value = "只读")
		private boolean readyOnly;
	}
	
	@Data
	@ApiModel(value = "外键设置")
	public static class FKey{
		
		// 外键表id
		@ApiModelProperty(value = "外键表id")
		private Long fkTableId;
		
		// 外键表标题
		@ApiModelProperty(value = "外键表名称")
		private String fkTableTitle;

		@ApiModelProperty(value = "外键字段标题")
		private String fkFieldTitle;

		@ApiModelProperty(value = "外键babel标题")
		private String fkLabelTitle;
		
		@ApiModelProperty(value = "上级字段名称",notes = "如果配置了该字段说明是树形结构，将使用下拉搜索树来实现，否则使用下搜索组件")
		private String fkParentField;
		
		@ApiModelProperty(value = "显示字段集合")
		private List<FkField> fkFields;
		
	}
	
	@Data
	public static class FkField{
		@ApiModelProperty(value="标题",notes="字符长度为：100")
		private String title;
		@ApiModelProperty(value="名称",notes="字符长度为：50")
		private String name;
		@ApiModelProperty(value = "显示顺序",notes = "默认根据字段列表顺序显示")
		private Integer	index;
	}
	
	
	@Data
	@ApiModel(value = "标签设置")
	public static class Tag{
		
		@ApiModelProperty(value = "标签名称")
		private String name;
		
		@ApiModelProperty(value = "标签值",notes = "标签数据回显")
		private List<Integer> value=new ArrayList<>();
		
		@ApiModelProperty(value = "上级名称")
		private String parent;

		@ApiModelProperty(value = "子标签集合")
		private List<Tag> children=new ArrayList<>();
		
	}
	
	
	@Data
	@ApiModel(value = "验证规则")
	public static class Rule{
		
		@ApiModelProperty(value = "表达式")
		private String pattern;
		
		@ApiModelProperty(value = "验证失败消息")
		private String message;
		
	}
	
	@Data
	@ApiModel(value = "条件样式")
	public static class ConditionStyle{
		
		@ApiModelProperty(value = "表达式类型",notes = "eq：值匹配,reg:正则")
		private String type;
		
		@ApiModelProperty(value = "条件表达式")
		private String express;
		
		@ApiModelProperty(value = "字体颜色")
		private String fontColor;
		
		@ApiModelProperty(value = "提示信息")
		private String tips;
		
		@ApiModelProperty(value = "是否可用")
		private boolean enable;
		
	}
	
	
	@Data
	@ApiModel(value = "排序设置")
	public static class Sort{
		
		@ApiModelProperty(value = "排序方式",notes = "升序：asc,降序：desc")
		private String type;
		
		@ApiModelProperty(value = "是否可用")
		private boolean enable;
		
		@ApiModelProperty(value = "默认排序")
		private boolean defoult;
		
	}
	
}
