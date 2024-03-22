package com.unione.cloud.portal.system.model;
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
 * @标题 	SysRole Entity
 * @描述	系统管理：角色信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysRole")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_role")
public class SysRole extends Pojo {
	/**
	* 名称
	*/
	@ApiModelProperty(value="名称",notes="长度为：100")
	private String name;
	/**
	* 编码
	*/
	@ApiModelProperty(value="编码",notes="长度为：50")
	private String codes;
	/**
	* 类型，字典ROLETYPE 
	*/
	@ApiModelProperty(value="类型，字典ROLETYPE ",notes="长度为：10")
	private Integer types;
	/**
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="状态，字典，使用状态 USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：500")
	private String descs;

}
