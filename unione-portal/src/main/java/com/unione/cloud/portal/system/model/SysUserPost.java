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
 * @标题 	SysUserPost Entity
 * @描述	系统管理：用户岗位
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysUserPost")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user_post")
public class SysUserPost extends Pojo {
	/**
	* 岗位ID
	*/
	@Schema(title="岗位ID",description="长度为：19")
	private Long postId;
	/**
	* 用户所属机构ID
	*/
	@Schema(title="用户所属机构ID",description="长度为：19")
	private Long userOrgId;
	/**
	* 用户所属机构名称
	*/
	@Schema(title="用户所属机构名称",description="长度为：255")
	private String userOrgName;
	/**
	* 成员姓名
	*/
	@Schema(title="成员姓名",description="长度为：100")
	private String name;
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
	* 成员状态，字典UGROUPMENSTATUS 1正常，2离开
	*/
	@Schema(title="成员状态，字典UGROUPMENSTATUS 1正常，2离开",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 成员描述
	*/
	@Schema(title="成员描述",description="长度为：1000")
	private String descs;

}
