package com.unione.cloud.form.data.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
	@Schema(title="应用ID",description="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 数据定义ID
	*/
	@Schema(title="数据定义ID",description="长度为：19")
	private Long defineId;
	/**
	* 数据权限ID
	*/
	@Schema(title="数据权限ID",description="长度为：19")
	private Long permisId;
	/**
	* 目标类型，字典DMSDATAAUTHTYPE  dc：中心， role：角色，user：用户，organ：机构
	*/
	@Schema(title="目标类型，字典DMSDATAAUTHTYPE  dc：中心， role：角色，user：用户，organ：机构",description="长度为：10")
	private String targetType;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="使用状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
