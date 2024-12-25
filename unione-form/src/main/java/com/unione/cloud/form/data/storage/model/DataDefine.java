package com.unione.cloud.form.data.storage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataDefineRelease;
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
		}else {
			super.setConfigs("{}");
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
	
	
	public static DataDefine from(SysDataDefine from) {
		DataDefine dataDefine=new DataDefine();
		BeanUtils.copy(from, dataDefine);
		return dataDefine;
	}
	public static DataDefine from(SysDataDefineRelease from) {
		DataDefine dataDefine=new DataDefine();
		BeanUtils.copy(from, dataDefine);
		return dataDefine;
	}
	
	
	@Data
	@ApiModel(value="数据定义配置DTO")
	public static class DataDefineConfig implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;


		@ApiModelProperty(value="字段列表")
		private List<DataField> fields=new ArrayList<>();
		
		
		@ApiModelProperty(value="数据过滤集合",notes = "高级数据过滤")
		private List<DataFilter> filters=new ArrayList<>();
		
		
		@ApiModelProperty(value="数据权限集合",notes = "")
		private List<DataPermis> permis=new ArrayList<>();
		
		
		@ApiModelProperty(value="数据显示配置",notes = "")
		private DataShow show;
		
		@ApiModelProperty(value="是否静默",notes = "静默模式，不提示异常信息")
		private boolean silence;
		
	}
	
	@Data
	@ApiModel(value="数据显示DTO")
	public static class DataShow implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@ApiModelProperty(value="表单显示列数",notes = "表单显示列数，默认3")
		private Integer showColumn;
		
		@ApiModelProperty(value="表单显示列数",notes = "表单项label显示宽度，默认9")
		private Integer labelWidget;
		
		@ApiModelProperty(value="列表页面模版",notes = "")
		private String  listTmpl;
		
	}
	
	
	@Data
	@ApiModel(value="数据权限DTO")
	public static class DataPermis implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
	public static class DataFilter implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
		private String alias;
		
		
		public DataField setName(String name) {
			super.setName(name);
			if(!StringUtils.isEmpty(name)) {
				String tt[]=name.toLowerCase().split("_");
				StringBuffer buf=new StringBuffer();
				buf.append(tt[0]);
				for(int i=1;i<tt.length;i++) {
					String t=tt[i];
					if(!StringUtils.isEmpty(t)) {
						buf.append((t.charAt(0)+"").toUpperCase());
						if(t.length()>1) {
							buf.append(t.substring(1));
						}
					}
				}
				this.alias=buf.toString();
			}
			return this;
		}
		
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
			}else {
				super.setConfigs("{}");
			}
			return super.getConfigs();
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
		
		@ApiModelProperty(value="显示设置")
		private FieldShow show;
		
		@ApiModelProperty(value="条件样式")
		private List<ConditionStyle> termStyle;
		
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
		
		@ApiModelProperty(value="输入提示")
		private String tooltip;
		
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
		
		@ApiModelProperty(value="外键关联数据编码",notes = "")
		private String dsn;
		
		@ApiModelProperty(value="外键关联数据id",notes = "")
		private String dsnId;
		
		@ApiModelProperty(value="外键关联数据名称",notes = "")
		private String dsnName;
		
		@ApiModelProperty(value="外键关联数据标题",notes = "")
		private String dsnTitle;
		
		@ApiModelProperty(value="外键关联字段名称",notes = "")
		private String fieldName;
		
		@ApiModelProperty(value="外键关联字段标题",notes = "")
		private String fieldTitle;
		
		@ApiModelProperty(value="外键显示字段名称",notes = "如果不为空，则数据列表中显示该字段指定名称的字段数据")
		private String labelName;
		
		@ApiModelProperty(value="外键显示字段标题",notes = "如果不为空，则数据列表表头中显示该字段标题")
		private String labelTitle;

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
		
		@ApiModelProperty(value="详情显示",notes = "字段在表单页面显示配置")
		private FieldForm view;
		
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
		
		@ApiModelProperty(value = "行合并开关",notes = "数据列表页面")
		private boolean rowMergeEnable;
		
		@ApiModelProperty(value = "列合并开关",notes = "数据列表页面")
		private boolean colMergeEnable;
		
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
		private String types;
		
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
		@ApiModelProperty(value="转换器ID")
		private Long id;
		
		@ApiModelProperty(value="转换类型",notes = "dict:字典,option:静态选项,dbtable:数据集,remote：远程接口(后端代理调用),local:本地接口（前端直接调用）")
		private String types;
		
		@ApiModelProperty(value="字典名称")
		private String dictName;
		
		@ApiModelProperty(value="选项集合")
		private List<DataOption> options;
		
		@ApiModelProperty(value="搜索是否可用")
		private boolean search;
		
		@ApiModelProperty(value="树形结构是否异步加载",notes="长度为：10")
		private boolean isAsync;
		
		@ApiModelProperty(value="是否分页加载",notes="长度为：10")
		private boolean isPaging;
		
		@ApiModelProperty(value="api url地址",notes = "统一使用POST请求，json提交,标准结构:{body:{参数}}")
		private String url;
		
		@ApiModelProperty(value="api 接口参数",notes = "标准json字符串,直接使用该参数进行接口请求")
		private Map<String, Object> params;
		
		@ApiModelProperty(value="数据源ID")
		private Long dsId;
		
		@ApiModelProperty(value="table名称")
		private String tableName;
		
		@ApiModelProperty(value = "数据字段集合",notes = "默认只加载value,label字段，可以通过该属性加载其他更多字段,多个字段用逗号分隔")
		private String tableField;
		
		@ApiModelProperty(value = "数据过滤",notes = "支持数据条件过滤，eg：sex=1")
		private String tableWhere;
		
		@ApiModelProperty(value = "数据排序",notes = "指定排序规则")
		private String tableOrder;
		
		@ApiModelProperty(value = "主键字段",notes = "默认ID")
		private String idField;
		
		@ApiModelProperty(value = "父级字段",notes = "默认PID")
		private String pidField;
		
		@ApiModelProperty(value = "value字段",notes = "不能为空")
		private String valueField;
		
		@ApiModelProperty(value = "label字段",notes = "不能为空")
		private String labelField;
		
		@ApiModelProperty(value = "显示层级",notes = "树形组件生效")
		private Integer showLevel;
		
	}
	
	
	
	
	
	@Data
	@ApiModel("数据规则")
	public static class DataRule implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1474367822075160446L;
		
		@ApiModelProperty(value="规则触发时机",notes = "change,blur")
		private List<String> trigger;

		@ApiModelProperty(value="是否忽略空格",notes = "必填验证开启时，是否忽略空格")
		private Boolean whitespace;
		
		@ApiModelProperty(value="预设规则",notes = "字典：FORMDATARULE,手机号：tel，邮箱：email，身份证号：idcard等等")
		private String  advance;
		
		@ApiModelProperty(value="最小值")
		private Integer rangeMin;
		
		@ApiModelProperty(value="最大值")
		private Integer rangeMax;
		
		@ApiModelProperty(value="范围验证消息")
		private String rangeMessage;
		
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

		@ApiModelProperty(value="选项value")
		private String value;
		
		
		@ApiModelProperty(value="选项label")
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
