package com.unione.cloud.form.data.storage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataField;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据定义对象")
public class DataDefine extends SysDataDefine{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7987670113848476469L;
	
	
	@JsonProperty("configs")
	@ApiModelProperty(value="数据配置对象")
	private DataDefineConfig configDto;
	
	@JsonIgnore
	public DataDefine setConfigs(String configs) {
		super.setConfigs(configs);
		configDto=JSONUtil.toBean(configs, DataDefineConfig.class);
		return this;
	}
	
	@JsonIgnore
	public String getConfigs() {
		if(configDto!=null) {
			super.setConfigs(JSONUtil.toJsonStr(configDto));
		}
		return super.getConfigs();
	}
	
	
	@JsonIgnore
	public List<DataField> getFields(){
		if(this.configDto!=null && this.configDto.getFields()!=null) {
			return this.configDto.getFields();
		}		
		return new ArrayList<>();
	}
	
	/**
	 * 	获取指定标准字段对象
	 * @param field
	 * @return
	 */
	public DataField getStsField(BaseField field) {
		if(this.getFields()!=null) {
			Optional<DataField> optional = this.getFields().stream()
				.filter(f->field.getColumn().equals(f.getName()) || field.getName().equals(f.getStsField()))
				.findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}
	
	@Data
	@ApiModel(value="数据定义配置DTO")
	public static class DataDefineConfig{
		
		@ApiModelProperty(value="字段列表")
		private List<DataField> fields=new ArrayList<>();
		
		
		@ApiModelProperty(value="数据过滤集合",notes = "高级数据过滤")
		private List<DataFilter> filters=new ArrayList<>();
		
		
		@ApiModelProperty(value="数据权限集合",notes = "")
		private List<DataPermis> permis=new ArrayList<>();
	}
	
	
	@Data
	@ApiModel(value="数据权限DTO")
	public static class DataPermis{
		
		@ApiModelProperty(value="权限标识",notes="")
		private Long id;
		
		@ApiModelProperty(value="权限名称",notes="")
		private String name;
		
		@ApiModelProperty(value="是否需要授权",notes="长度为：10")
		private Integer needAuth;
		
		@ApiModelProperty(value="权限SQL表达式",notes="长度为：1000")
		private String express;
		
		@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
		private Integer status;
	}
	
	
	@Data
	@ApiModel(value="数据过滤DTO")
	public static class DataFilter{
		
		@ApiModelProperty(value="过滤标题",notes="")
		private String title;
		
		@ApiModelProperty(value="过滤名称",notes="用于识别过滤，前端通过该名称传递相关参数，eg:agests=0-3，查询年龄0-3岁")
		private String name;
		
		@ApiModelProperty(value="过滤脚本",notes="只需要过滤逻辑部分，eg:  CEIL(MONTHS_BETWEEN(SYSDATE,BIRTH_DATE)/12) between 0 and 3")
		private String filter;
	}
	
	
	
	@Data
	@ApiModel("数据字段DTO")
	public static class DataField extends SysDataField{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2468858077101285404L;
		
		@JsonProperty("configs")
		@ApiModelProperty(value="字段配置对象")
		private DataFieldConfig configDto;
		
		@JsonIgnore
		public DataField setConfigs(String configs) {
			super.setConfigs(configs);
			configDto=JSONUtil.toBean(configs, DataFieldConfig.class);
			return this;
		}
		
		@JsonIgnore
		public String getConfigs() {
			if(configDto!=null) {
				super.setConfigs(JSONUtil.toJsonStr(configDto));
			}
			return super.getConfigs();
		}
		
		@JsonIgnore
		public String getAlias() {
			if(!StringUtils.isEmpty(this.getName())) {
				return StrUtil.toCamelCase(this.getName());
			}
			return null;
		}
	}
	
	@Data
	@ApiModel("字段配置DTO")
	public static class DataFieldConfig implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8683418205486661110L;
		
		@ApiModelProperty(value="组件设置")
		private FieldWidget widget;
		
		@ApiModelProperty(value="外键设置")
		private ForeignKey fkey;
		
		@ApiModelProperty(value="排序设置")
		private DataSort sort;
		
		@ApiModelProperty(value="数据规则")
		private DataRule rule;
		
		@ApiModelProperty(value="数据转换")
		private DataConvert convert;
		
		@ApiModelProperty(value="数据查询")
		private DataQuery query;
		
		@ApiModelProperty(value="条件样式")
		private List<ConditionStyle> conditionStyle;
		
	}
	
	
	
	@Data
	@ApiModel("字段组件配置DTO")
	public static class FieldWidget implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -337922081283257936L;
		
		
		@ApiModelProperty(value="表单组件name",notes = "eg：input,select")
		private String name;
		
		@ApiModelProperty(value="输入提示")
		private String placeholder;
		
		@ApiModelProperty(value="输入帮助")
		private String help;
		
		@ApiModelProperty(value="组件属性")
		private Map<String,Object> props;
		
		
	}
	
	
	@Data
	@ApiModel("外键字段配置DTO")
	public static class ForeignKey implements Serializable{
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
		private List<ForeignField> fields;
		
	}
	
	
	@Data
	@ApiModel("外键显示字段DTO")
	public static class ForeignField implements Serializable{
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
	public static class FieldShow implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327912081282257936L;
		
		@ApiModelProperty(value="列表显示",notes = "字段在列表页面显示配置")
		private FieldList list;
		
		@ApiModelProperty(value="表单显示",notes = "字段在表单页面显示配置")
		private FieldForm form;
		
	}
	
	@Data
	@ApiModel("字段表单页面显示DTO")
	public static class FieldList extends FieldView{
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
	public static class FieldForm extends FieldView{
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
	public static class FieldView implements Serializable{
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
	public static class DataQuery implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -317912081282257936L;
		
		@ApiModelProperty(value="是否查询")
		private boolean enable;
		
		@ApiModelProperty(value="查询类型",notes = "字典：DATAQUERYTYPE EQ：精确查询，LIKE：模糊查询，LLIKE:左模糊，RLIKE：右模糊,RANGE：范围查询,ADVANCE：高级查询")
		private String type;
		
		@ApiModelProperty(value="默认查询",notes = "默认查询：即加入关键字查询")
		private boolean defoult;
		
		@ApiModelProperty(value="默认显示",notes = "true:默认显示该查询,false：高级查询中自行勾选")
		private boolean visible;
		
		@ApiModelProperty(value = "查询名称",notes="查询方式为：高级查询时绑定的查询名称")
		private String name;
		
	}
	
	
	@Data
	@ApiModel("数据转换DTO")
	public static class DataConvert implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -397912081282257936L;

		@ApiModelProperty(value="转换类型",notes = "dict:字典,option:静态选项")
		private String type;
		
		@ApiModelProperty(value="字典名称")
		private String dictName;
		
		@ApiModelProperty(value="选项集合")
		private List<DataOption> options;
		
	}
	
	
	
	
	@Data
	@ApiModel("数据规则")
	public static class DataRule implements Serializable{
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
	public static class DataSort implements Serializable{
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
	public static class DataParam implements Serializable{
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
	public static class DataOption implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7995835231421560625L;

		@ApiModelProperty(value="选项key")
		private String key;
		
		
		@ApiModelProperty(value="选项标签")
		private String label;
	}
	
	
	@Data
	@ApiModel(value = "条件样式DTO")
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
	
	
	
	public static enum DataDefineCategory{
		SQL("sql"),NOSQL("nosql"),API("api");
		
		private String value;
		private DataDefineCategory(String value) {
			this.value=value;
		}
		public String value() {
			return value;
		}
	}
	
	public static enum DataQueryType{
		EQ("精确查询"),LIKE("模糊查询"),LLIKE("左模糊"),RLIKE("右模糊"),RANGE("范围查询"),ADVANCE("高级查询");
		private String value;
		private DataQueryType(String value) {
			this.value=value;
		}
		public String value() {
			return value;
		}
	}
	
	
	
}
