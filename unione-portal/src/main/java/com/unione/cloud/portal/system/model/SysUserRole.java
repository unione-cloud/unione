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
 * @标题 	SysUserRole Entity
 * @描述	系统管理：用户角色
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysUserRole")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user_role")
public class SysUserRole extends Pojo {
	/**
	* 角色ID
	*/
	@Schema(title="角色ID",description="长度为：19")
	private Long roleId;
	/**
	* 是否可传递授权，1是，0否
	*/
	@Schema(title="是否可传递授权，1是，0否",description="长度为：10")
	private Integer enDilivery;

}
