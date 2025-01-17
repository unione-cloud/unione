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
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataDefineRelease;
import com.unione.cloud.form.data.model.SysDataField;
import com.unione.cloud.form.data.util.DataUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="数据定义对象")
public class DataDefine extends SysDataDefine{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7987670113848476469L;
	
	
	@JsonProperty("configs")
	@Schema(title="数据配置对象")
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
	@Schema(title="数据定义配置DTO")
	public static class DataDefineConfig implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;


		@Schema(title="字段列表")
		private List<DataField> fields=new ArrayList<>();
		
		
		@Schema(title="数据过滤集合",description= "高级数据过滤")
		private List<DataFilter> filters=new ArrayList<>();
		
		
		@Schema(title="数据权限集合",description= "")
		private List<DataPermis> permis=new ArrayList<>();
		
		
		@Schema(title="数据显示配置",description= "")
		private DataShow show;
		
		@Schema(title="是否静默",description= "静默模式，不提示异常信息")
		private boolean silence;
		
	}
	
	@Data
	@Schema(title="数据显示DTO")
	public static class DataShow implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Schema(title="表单显示列数",description= "表单显示列数，默认3")
		private Integer showColumn;
		
		@Schema(title="表单显示列数",description= "表单项label显示宽度，默认9")
		private Integer labelWidget;
		
		@Schema(title="列表页面模版",description= "")
		private String  listTmpl;
		
	}
	
	
	@Data
	@Schema(title="数据权限DTO")
	public static class DataPermis implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Schema(title="权限标识",description="")
		private Long id;
		
		@Schema(title="权限名称",description="")
		private String name;
		
		@Schema(title="是否需要授权",description="长度为：10")
		private Integer needAuth;
		
		@Schema(title="权限SQL表达式",description="长度为：1000")
		private String express;
		
		@Schema(title="使用状态，字典USEORNOT 1使用，0停用",description="长度为：10")
		private Integer status;
	}
	
	
	@Data
	@Schema(title="数据过滤DTO")
	public static class DataFilter implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Schema(title="过滤标题",description="")
		private String title;
		
		@Schema(title="过滤名称",description="用于识别过滤，前端通过该名称传递相关参数，eg:agests=0-3，查询年龄0-3岁")
		private String name;
		
		@Schema(title="过滤脚本",description="只需要过滤逻辑部分，eg:  CEIL(MONTHS_BETWEEN(SYSDATE,BIRTH_DATE)/12) between 0 and 3")
		private String filter;
	}
	
	
	
	@Data
	@Schema(title="数据字段DTO")
	public static class DataField extends SysDataField{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2468858077101285404L;
		
		@JsonProperty("configs")
		@Schema(title="字段配置对象")
		private DataFieldConfig configDto;
		
		@JsonIgnore
		private String alias;
		
		
		public DataField setName(String name) {
			super.setName(name);
			if(!StringUtils.isEmpty(name)) {
				this.alias=DataUtil.toHump(name);
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
	@Schema(title="字段配置DTO")
	public static class DataFieldConfig implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8683418205486661110L;
		
		@Schema(title="组件设置")
		private FieldWidget widget;
		
		@Schema(title="外键设置")
		private ForeignKey fkey;
		
		@Schema(title="排序设置")
		private DataSort sort;
		
		@Schema(title="数据规则")
		private DataRule rule;
		
		@Schema(title="数据转换")
		private DataConvert convert;
		
		@Schema(title="数据查询")
		private DataQuery query;
		
		@Schema(title="显示设置")
		private FieldShow show;
		
		@Schema(title="条件样式")
		private List<ConditionStyle> termStyle;
		
	}
	
	
	
	@Data
	@Schema(title="字段组件配置DTO")
	public static class FieldWidget implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -337922081283257936L;
		
		
		@Schema(title="表单组件name",description= "eg：input,select")
		private String name;
		
		@Schema(title="输入提示")
		private String placeholder;
		
		@Schema(title="输入帮助")
		private String help;
		
		@Schema(title="输入提示")
		private String tooltip;
		
		@Schema(title="组件属性")
		private Map<String,Object> props;
		
		
	}
	
	
	@Data
	@Schema(title="外键字段配置DTO")
	public static class ForeignKey implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081283257936L;
		
		@Schema(title="是否外键")
		private boolean enable;
		
		@Schema(title="外键关联数据编码",description= "")
		private String dsn;
		
		@Schema(title="外键关联数据id",description= "")
		private String dsnId;
		
		@Schema(title="外键关联数据名称",description= "")
		private String dsnName;
		
		@Schema(title="外键关联数据标题",description= "")
		private String dsnTitle;
		
		@Schema(title="外键关联字段名称",description= "")
		private String fieldName;
		
		@Schema(title="外键关联字段标题",description= "")
		private String fieldTitle;
		
		@Schema(title="外键显示字段名称",description= "如果不为空，则数据列表中显示该字段指定名称的字段数据")
		private String labelName;
		
		@Schema(title="外键显示字段标题",description= "如果不为空，则数据列表表头中显示该字段标题")
		private String labelTitle;

		@Schema(title="额外显示的外键数据字段",description= "")
		private List<ForeignField> fields;
		
	}
	
	
	@Data
	@Schema(title="外键显示字段DTO")
	public static class ForeignField implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081283257936L;
		
		@Schema(title="字段名称",description= "")
		private String name;
		
		@Schema(title="字段标题",description= "")
		private String title;
		
		@Schema(title="显示宽度",description= "占用空间大小，24单元格")
		private Integer width;
		
		@Schema(title="显示顺序",description= "默认是根据字段列表索引顺序显示，可以通过该字段指定显示顺序")
		private Integer index;
		
	}
	
	
	@Data
	@Schema(title="字段显示DTO")
	public static class FieldShow implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327912081282257936L;
		
		@Schema(title="列表显示",description= "字段在列表页面显示配置")
		private FieldList list;
		
		@Schema(title="表单显示",description= "字段在表单页面显示配置")
		private FieldForm form;
		
		@Schema(title="详情显示",description= "字段在表单页面显示配置")
		private FieldForm view;
		
	}
	
	@Data
	@Schema(title="字段表单页面显示DTO")
	public static class FieldList extends FieldView{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081282257936L;
		
		@Schema(title="固定方式",description= "可选：'left' | 'right'")
		private String fixed;
		
		@Schema(title="对齐方式",description= "默认：left,可选：'center' | 'left' | 'right'")
		private String align;
		
		@Schema(title="行合并开关",description= "数据列表页面")
		private boolean rowMergeEnable;
		
		@Schema(title="列合并开关",description= "数据列表页面")
		private boolean colMergeEnable;
		
	}
	
	@Data
	@Schema(title="字段表单页面显示DTO")
	public static class FieldForm extends FieldView{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327922081282257936L;
		
		@Schema(title="label宽度",description= "占用空间大小，24单元格")
		private Integer labelWidth;
		
		@Schema(title="widget宽度",description= "占用空间大小，24单元格")
		private Integer valueWidth;
		
	}
	
	
	@Data
	@Schema(title="字段视图配置DTO")
	public static class FieldView implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -327912081282257936L;
		
		@Schema(title="是否查询")
		private boolean enable;
		
		@Schema(title="显示宽度",description= "占用空间大小，24单元格")
		private Integer width;
		
		@Schema(title="显示顺序",description= "默认是根据字段列表索引顺序显示，可以通过该字段指定显示顺序")
		private Integer index;
		
	}
	
	
	
	@Data
	@Schema(title="数据转换DTO")
	public static class DataQuery implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -317912081282257936L;
		
		@Schema(title="是否查询")
		private boolean enable;
		
		@Schema(title="查询类型",description= "字典：DATAQUERYTYPE EQ：精确查询，LIKE：模糊查询，LLIKE:左模糊，RLIKE：右模糊,RANGE：范围查询,ADVANCE：高级查询")
		private String types;
		
		@Schema(title="默认查询",description= "默认查询：即加入关键字查询")
		private boolean defoult;
		
		@Schema(title="默认显示",description= "true:默认显示该查询,false：高级查询中自行勾选")
		private boolean visible;
		
		@Schema(title="查询名称",description="查询方式为：高级查询时绑定的查询名称")
		private String name;
		
	}
	
	
	@Data
	@Schema(title="数据转换DTO")
	public static class DataConvert implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -397912081282257936L;
		@Schema(title="转换器ID")
		private Long id;
		
		@Schema(title="转换类型",description= "dict:字典,option:静态选项,dbtable:数据集,remote：远程接口(后端代理调用),local:本地接口（前端直接调用）")
		private String types;
		
		@Schema(title="字典名称")
		private String dictName;
		
		@Schema(title="选项集合")
		private List<DataOption> options;
		
		@Schema(title="搜索是否可用")
		private boolean search;
		
		@Schema(title="树形结构是否异步加载",description="长度为：10")
		private boolean isAsync;
		
		@Schema(title="是否分页加载",description="长度为：10")
		private boolean isPaging;
		
		@Schema(title="api url地址",description= "统一使用POST请求，json提交,标准结构:{body:{参数}}")
		private String url;
		
		@Schema(title="api 接口参数",description= "标准json字符串,直接使用该参数进行接口请求")
		private Map<String, Object> params;
		
		@Schema(title="数据源ID")
		private Long dsId;
		
		@Schema(title="table名称")
		private String tableName;
		
		@Schema(title="数据字段集合",description= "默认只加载value,label字段，可以通过该属性加载其他更多字段,多个字段用逗号分隔")
		private String tableField;
		
		@Schema(title="数据过滤",description= "支持数据条件过滤，eg：sex=1")
		private String tableWhere;
		
		@Schema(title="数据排序",description= "指定排序规则")
		private String tableOrder;
		
		@Schema(title="主键字段",description= "默认ID")
		private String idField;
		
		@Schema(title="父级字段",description= "默认PID")
		private String pidField;
		
		@Schema(title="value字段",description= "不能为空")
		private String valueField;
		
		@Schema(title="label字段",description= "不能为空")
		private String labelField;
		
		@Schema(title="显示层级",description= "树形组件生效")
		private Integer showLevel;
		
	}
	
	
	
	
	
	@Data
	@Schema(title="数据规则")
	public static class DataRule implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1474367822075160446L;
		
		@Schema(title="规则触发时机",description= "change,blur")
		private List<String> trigger;

		@Schema(title="是否忽略空格",description= "必填验证开启时，是否忽略空格")
		private Boolean whitespace;
		
		@Schema(title="预设规则",description= "字典：FORMDATARULE,手机号：tel，邮箱：email，身份证号：idcard等等")
		private String  advance;
		
		@Schema(title="最小值")
		private Integer rangeMin;
		
		@Schema(title="最大值")
		private Integer rangeMax;
		
		@Schema(title="范围验证消息")
		private String rangeMessage;
		
		@Schema(title="正则表达式")
		private String regExpress;
		
		@Schema(title="正则表达式验证失败消息")
		private String regMessage;
		
	}
	
	
	@Data
	@Schema(title="数据排序DTO")
	public static class DataSort implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -397912081282257936L;

		@Schema(title="是否排序")
		private boolean enable;
		
		@Schema(title="是否默认")
		private boolean defoult;
		
		@Schema(title="是否升序")
		private boolean asc;
		
	}
	
	
	
	@Data
	@Schema(title="数据参数DTO")
	public static class DataParam implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7995835230421560625L;

		@Schema(title="参数标题")
		private String title;
		
		@Schema(title="参数名称")
		private String name;
		
		@Schema(title="参数默认值")
		private String value;
	}
	
	
	@Data
	@Schema(title="数据选项DTO")
	public static class DataOption implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7995835231421560625L;

		@Schema(title="选项value")
		private String value;
		
		
		@Schema(title="选项label")
		private String label;
	}
	
	
	@Data
	@Schema(title = "条件样式DTO")
	public static class ConditionStyle{
		
		@Schema(title="表达式类型",description= "eq：值匹配,reg:正则")
		private String type;
		
		@Schema(title="条件表达式")
		private String express;
		
		@Schema(title="字体颜色")
		private String fontColor;
		
		@Schema(title="提示信息")
		private String tips;
		
		@Schema(title="是否可用")
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
