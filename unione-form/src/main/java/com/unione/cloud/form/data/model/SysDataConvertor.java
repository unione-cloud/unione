package com.unione.cloud.form.data.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 数据源ID
	*/
	@Schema(title="数据源ID",description="长度为：19")
	private Long dsId;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：100")
	private String title;
	/**
	* 类型，dict：字典，option：静态选项，dbtable：数据集，api：接口
	*/
	@Schema(title="类型，dict：字典，option：静态选项，dbtable：数据集，api：后端接口(后端代理调用),rest:前端接口（前端直接调用）",description="长度为：20")
	private String types;
	/**
	* 字典名称
	*/
	@Schema(title="字典名称",description="长度为：50")
	private String dictName;
	/**
	* 静态选项
	*/
	@Schema(title="静态选项",description="长度为：65535")
	private String options;
	/**
	* 搜索是否可用
	*/
	@Schema(title="搜索是否可用",description="长度为：1")
	private boolean search;
	/**
	* 接口url地址
	*/
	@Schema(title="接口url地址",description="长度为：200")
	private String url;
	/**
	* table名称
	*/
	@Schema(title="table名称",description="长度为：50")
	private String tableName;
	/**
	* 数据字段集合
	*/
	@Schema(title="数据字段集合",description="长度为：1000")
	private String tableField;
	/**
	* 数据过滤
	*/
	@Schema(title="数据过滤",description="长度为：65535")
	private String tableWhere;
	/**
	* 数据排序
	*/
	@Schema(title="数据排序",description="长度为：50")
	private String tableOrder;
	/**
	* 主键字段，默认ID
	*/
	@Schema(title="主键字段，默认ID",description="长度为：50")
	private String idField;
	/**
	* 父级字段，默认PID
	*/
	@Schema(title="父级字段，默认PID",description="长度为：50")
	private String pidField;
	/**
	* value字段
	*/
	@NotEmpty(message = "value字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "value字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@Schema(title="value字段",description="长度为：50")
	private String valueField;
	/**
	* label字段
	*/
	@NotEmpty(message = "label字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "label字段名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@Schema(title="label字段",description="长度为：50")
	private String labelField;
	/**
	* 显示层级
	*/
	@Schema(title="显示层级",description="长度为：10")
	private Integer showLevel;
	/**
	* 树形结构是否异步加载
	*/
	@Schema(title="树形结构是否异步加载",description="长度为：10")
	private boolean isAsync;
	/**
	* 是否分页加载
	*/
	@Schema(title="是否分页加载",description="长度为：10")
	private boolean isPaging;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="使用状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
