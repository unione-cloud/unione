package com.unione.cloud.form.data.model;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	SysDataConvertor Entity
 * @描述	系统管理：数据转换器
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:03:41
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataConvertor")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_convertor")
public class SysDataConvertor extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5687866197282746000L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 数据源ID
	*/
	@ApiModelProperty(value="数据源ID",notes="长度为：19")
	private Long dsId;
	/**
	* 标题
	*/
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	/**
	* 类型，dict：字典，option：静态选项，dbtable：数据集，api：接口
	*/
	@ApiModelProperty(value="类型，dict：字典，option：静态选项，dbtable：数据集，api：接口",notes="长度为：20")
	private String types;
	/**
	* 字典名称
	*/
	@ApiModelProperty(value="字典名称",notes="长度为：50")
	private String dictName;
	/**
	* 静态选项
	*/
	@ApiModelProperty(value="静态选项",notes="长度为：65535")
	private String options;
	/**
	* 搜索是否可用
	*/
	@ApiModelProperty(value="搜索是否可用",notes="长度为：1")
	private boolean search;
	/**
	* 接口url地址
	*/
	@ApiModelProperty(value="接口url地址",notes="长度为：200")
	private String url;
	/**
	* table名称
	*/
	@ApiModelProperty(value="table名称",notes="长度为：50")
	private String tableName;
	/**
	* 数据字段集合
	*/
	@ApiModelProperty(value="数据字段集合",notes="长度为：1000")
	private String tableField;
	/**
	* 数据过滤
	*/
	@ApiModelProperty(value="数据过滤",notes="长度为：65535")
	private String tableWhere;
	/**
	* 数据排序
	*/
	@ApiModelProperty(value="数据排序",notes="长度为：50")
	private String tableOrder;
	/**
	* 主键字段，默认ID
	*/
	@ApiModelProperty(value="主键字段，默认ID",notes="长度为：50")
	private String idField;
	/**
	* 父级字段，默认PID
	*/
	@ApiModelProperty(value="父级字段，默认PID",notes="长度为：50")
	private String pidField;
	/**
	* value字段
	*/
	@NotEmpty(message = "value字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "value字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@ApiModelProperty(value="value字段",notes="长度为：50")
	private String valueField;
	/**
	* label字段
	*/
	@NotEmpty(message = "label字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "label字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@ApiModelProperty(value="label字段",notes="长度为：50")
	private String labelField;
	/**
	* 显示层级
	*/
	@ApiModelProperty(value="显示层级",notes="长度为：10")
	private Integer showLevel;
	/**
	* 树形结构是否异步加载
	*/
	@ApiModelProperty(value="树形结构是否异步加载",notes="长度为：10")
	private boolean isAsync;
	/**
	* 是否分页加载
	*/
	@ApiModelProperty(value="是否分页加载",notes="长度为：10")
	private boolean isPaging;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;

}
