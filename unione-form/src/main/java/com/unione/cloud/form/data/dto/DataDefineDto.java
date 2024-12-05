package com.unione.cloud.form.data.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据模型Dto")
public class DataDefineDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7987670113848476469L;
	
	
	@ApiModelProperty(value="字段列表")
	private List<DataFieldDto> fields;
	
	
	
	
	
	@Data
	@ApiModel("数据字段DTO")
	public static class DataFieldDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2468858077101285404L;

		@ApiModelProperty(value="字段名称")
		private String name;
		
		@ApiModelProperty(value="字段标题")
		private String title;
		
		@ApiModelProperty(value="组件设置")
		private FieldWidgetDto widget;
		
		@ApiModelProperty(value="是否主键")
		private boolean isPk;
		
		@ApiModelProperty(value="外键设置")
		private ForeignKeyDto fkey;
		
		@ApiModelProperty(value="是否不能为空")
		private boolean isNull;
		
		@ApiModelProperty(value="输入提示")
		private String placeholder;
		
		@ApiModelProperty(value="数据格式",notes = "数值类型/日期类型:显示格式")
		private String dataFormat;
		
		
		@ApiModelProperty(value="排序设置")
		private DataSortDto sort;
		
		@ApiModelProperty(value="数据规则")
		private DataRuleDto rule;
		
		@ApiModelProperty(value="数据转换")
		private DataConvertDto convert;
		
		@ApiModelProperty(value="数据查询")
		private DataQueryDto query;
		
	}
	
	
	@Data
	@ApiModel("表单组件配置DTO")
	public static class FieldWidgetDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -337922081283257936L;
		
		
		@ApiModelProperty(value="表单组件name",notes = "eg：input,select")
		private String name;
		
		
		@ApiModelProperty(value="组件属性")
		private Map<String,Object> props;
		
		
	}
	
	
	@Data
	@ApiModel("外键字段配置DTO")
	public static class ForeignKeyDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081283257936L;
		
		@ApiModelProperty(value="是否外键")
		private boolean enable;
		
		@ApiModelProperty(value="外键关联数据模型编码",notes = "")
		private String dsn;
		
		@ApiModelProperty(value="外键关联字段名称",notes = "")
		private String fieldName;
		
		@ApiModelProperty(value="外键显示字段名称",notes = "如果不为空，则数据列表中显示该字段指定名称的字段数据")
		private String labelName;
		
		@ApiModelProperty(value="外键显示字段标题",notes = "如果不为空，则数据列表表头中显示该字段标题")
		private String labeTitle;

		@ApiModelProperty(value="额外显示的外键数据字段",notes = "")
		private List<ForeignFieldDto> fields;
		
	}
	
	
	@Data
	@ApiModel("外键显示字段DTO")
	public static class ForeignFieldDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081283257936L;
		
		@ApiModelProperty(value="字段名称",notes = "")
		private String name;
		
		@ApiModelProperty(value="字段标题",notes = "")
		private String title;
		
		@ApiModelProperty(value="显示宽度",notes = "占用空间大小，24单元格")
		private Integer width;
		
		@ApiModelProperty(value="显示顺序",notes = "默认是根据字段列表索引顺序显示，可以通过该字段指定显示顺序")
		private Integer index;
		
	}
	
	
	@Data
	@ApiModel("字段显示DTO")
	public static class FieldShowDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327912081282257936L;
		
		@ApiModelProperty(value="列表显示",notes = "字段在列表页面显示配置")
		private FieldListDto list;
		
		@ApiModelProperty(value="表单显示",notes = "字段在表单页面显示配置")
		private FieldFormDto form;
		
	}
	
	@Data
	@ApiModel("字段表单页面显示DTO")
	public static class FieldListDto extends FieldViewDto{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081282257936L;
		
		@ApiModelProperty(value="固定方式",notes = "可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：left,可选：'center' | 'left' | 'right'")
		private String align;
		
	}
	
	@Data
	@ApiModel("字段表单页面显示DTO")
	public static class FieldFormDto extends FieldViewDto{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081282257936L;
		
		@ApiModelProperty(value="label宽度",notes = "占用空间大小，24单元格")
		private Integer labelWidth;
		
		@ApiModelProperty(value="widget宽度",notes = "占用空间大小，24单元格")
		private Integer valueWidth;
		
	}
	
	
	@Data
	@ApiModel("字段视图配置DTO")
	public static class FieldViewDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327912081282257936L;
		
		@ApiModelProperty(value="是否查询")
		private boolean enable;
		
		@ApiModelProperty(value="显示宽度",notes = "占用空间大小，24单元格")
		private Integer width;
		
		@ApiModelProperty(value="显示顺序",notes = "默认是根据字段列表索引顺序显示，可以通过该字段指定显示顺序")
		private Integer index;
		
	}
	
	
	
	@Data
	@ApiModel("数据转换DTO")
	public static class DataQueryDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -317912081282257936L;
		
		@ApiModelProperty(value="是否查询")
		private boolean enable;
		
		@ApiModelProperty(value="查询类型",notes = "字典：DATAQUERYTYPE eq：精确查询，like：模糊查询，likeL:左模糊，likeR：右模糊,range：范围查询")
		private String type;
		
		@ApiModelProperty(value="默认查询",notes = "默认查询：即加入关键字查询")
		private boolean defoult;
		
		@ApiModelProperty(value="默认显示",notes = "true:默认显示该查询,false：高级查询中自行勾选")
		private boolean visible;
		
	}
	
	
	@Data
	@ApiModel("数据转换DTO")
	public static class DataConvertDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -397912081282257936L;

		@ApiModelProperty(value="转换类型",notes = "dict:字典,option:静态选项")
		private String type;
		
		@ApiModelProperty(value="字典名称")
		private String dictName;
		
		@ApiModelProperty(value="选项集合")
		private List<DataOptionDto> options;
		
	}
	
	
	
	
	@Data
	@ApiModel("数据规则Dto")
	public static class DataRuleDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1474367822075160446L;

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
	@ApiModel("数据排序DTO")
	public static class DataSortDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -397912081282257936L;

		@ApiModelProperty(value="是否排序")
		private boolean enable;
		
		@ApiModelProperty(value="是否默认")
		private boolean defoult;
		
		@ApiModelProperty(value="是否升序")
		private boolean asc;
		
	}
	
	
	
	@Data
	@ApiModel("数据参数DTO")
	public static class DataParamDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7995835230421560625L;

		@ApiModelProperty(value="参数标题")
		private String title;
		
		@ApiModelProperty(value="参数名称")
		private String name;
		
		@ApiModelProperty(value="参数默认值")
		private String value;
	}
	
	
	@Data
	@ApiModel("数据选项DTO")
	public static class DataOptionDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7995835231421560625L;

		@ApiModelProperty(value="选项key")
		private String key;
		
		
		@ApiModelProperty(value="选项标签")
		private String label;
	}
	
	
}
