package com.unione.cloud.form.data.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysDataPermis Entity
 * @描述	系统管理：数据权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_permis")
public class SysDataPermis extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2228469034937505383L;
	/**
	* 数据模型ID
	*/
	@ApiModelProperty(value="数据模型ID",notes="长度为：19")
	private Long modelId;
	/**
	* 权限名称
	*/
	@ApiModelProperty(value="权限名称",notes="长度为：50")
	private String name;
	/**
	* 是否需要授权
	*/
	@ApiModelProperty(value="是否需要授权",notes="长度为：10")
	private Integer needAuth;
	/**
	* 权限SQL表达式
	*/
	@ApiModelProperty(value="权限SQL表达式",notes="长度为：1000")
	private String express;
	/**
	* 权限配置，json存储{}
	*/
	@ApiModelProperty(value="权限配置，json存储{}",notes="长度为：65535")
	private String configs;
	/**
	* 权限描述
	*/
	@ApiModelProperty(value="权限描述",notes="长度为：250")
	private String descs;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;

}
