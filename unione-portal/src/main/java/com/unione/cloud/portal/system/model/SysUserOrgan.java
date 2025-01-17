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
 * @标题 	SysUserOrgan Entity
 * @描述	系统管理：用户机构
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysUserOrgan")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user_organ")
public class SysUserOrgan extends Pojo {
	/**
	* 加入时间
	*/
	@Schema(title="加入时间",description="长度为：19")
	private Long timeJoin;
	/**
	* 离开时间
	*/
	@Schema(title="离开时间",description="长度为：19")
	private Long timeLeave;
	/**
	* 状态，字典UGROUPMENSTATUS 1正常，2离开
	*/
	@Schema(title="状态，字典UGROUPMENSTATUS 1正常，2离开",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;

}
