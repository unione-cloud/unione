package com.unione.cloud.form.data.model;
import javax.validation.constraints.NotNull;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysDataAuth Entity
 * @描述	系统管理：数据授权
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataAuth")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_auth")
public class SysDataAuth extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8536381881140054070L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 数据模型ID
	*/
	@ApiModelProperty(value="数据模型ID",notes="长度为：19")
	private Long modelId;
	/**
	* 数据权限ID
	*/
	@ApiModelProperty(value="数据权限ID",notes="长度为：19")
	private Long permisId;
	/**
	* 目标类型，字典DMSDATAAUTHTYPE  dc：中心， role：角色，user：用户，organ：机构
	*/
	@ApiModelProperty(value="目标类型，字典DMSDATAAUTHTYPE  dc：中心， role：角色，user：用户，organ：机构",notes="长度为：10")
	private String targetType;
	/**
	* 目标ID
	*/
	@ApiModelProperty(value="目标ID",notes="长度为：19")
	private Long targetId;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;

}
