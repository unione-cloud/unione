package com.unione.cloud.form.data.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysDataField Entity
 * @描述	系统管理：数据字段
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-08 20:17:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataField")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_field")
public class SysDataField extends Pojo {
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 数据定义ID
	*/
	@ApiModelProperty(value="数据定义ID",notes="长度为：19")
	private Long defineId;
	/**
	* 标题
	*/
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	/**
	* 名称
	*/
	@ApiModelProperty(value="名称",notes="长度为：50")
	private String name;
	/**
	* 默认值
	*/
	@ApiModelProperty(value="默认值",notes="长度为：100")
	private String dataValue;
	/**
	* 数据类型，直接使用java映射类型，如：String，Double，Float，Boolean，Date 等
	*/
	@ApiModelProperty(value="数据类型，直接使用java映射类型，如：String，Double，Float，Boolean，Date 等",notes="长度为：20")
	private String dataType;
	/**
	* 数据格式
	*/
	@ApiModelProperty(value="数据格式",notes="长度为：50")
	private String dataFormat;
	/**
	* 数据长度
	*/
	@ApiModelProperty(value="数据长度",notes="长度为：10")
	private Integer dataLen;
	/**
	* 数据精度
	*/
	@ApiModelProperty(value="数据精度",notes="长度为：10")
	private Integer dataPrec;
	/**
	* 是否主键，字典TUREORNOT 1是，0否
	*/
	@ApiModelProperty(value="是否主键，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isPk;
	/**
	* 是否可以为空，字典TUREORNOT 1是，0否
	*/
	@ApiModelProperty(value="是否可以为空，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isNull;
	/**
	* 标准字段，关联的标准字段名称
	*/
	@ApiModelProperty(value="标准字段，关联的标准字段名称",notes="长度为：50")
	private String stsField;
	/**
	* 字段配置，JSON结构,{}
            {
                fkey:{外键配置，关联字段集合，列表显示控制等等},
                widget:{表单控件配置，默认值，是否只读},
                convert:{转换器配置},
                show:{表单显示配置}
            }
	*/
	@ApiModelProperty(value="字段配置，JSON结构,{}"+
            "{"+
                "fkey:{外键配置，关联字段集合，列表显示控制等等},"+
                "widget:{表单控件配置，默认值，是否只读},"+
                "convert:{转换器配置},"+
                "show:{表单显示配置}"+
           "}",notes="长度为：2147483647")
	@JsonIgnore
	private String configs;
	/**
	* 是否需要授权
	*/
	@ApiModelProperty(value="是否需要授权",notes="长度为：10")
	private Integer needAuth;
	/**
	* 同步开关，是否需要同步去到数据库，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="同步开关，是否需要同步去到数据库，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer syncEnable;
	/**
	* 同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer syncFlag;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
