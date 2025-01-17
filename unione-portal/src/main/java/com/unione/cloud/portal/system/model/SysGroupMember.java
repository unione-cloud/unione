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
 * @标题 	SysGroupMember Entity
 * @描述	系统管理：分组成员
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysGroupMember")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_group_member")
public class SysGroupMember extends Pojo {
	/**
	* 分组ID
	*/
	@Schema(title="分组ID",description="长度为：19")
	private Long groupId;
	/**
	* 成员类型，字典GROUPTYPES 1用户分组，9其他
	*/
	@Schema(title="成员类型，字典GROUPTYPES 1用户分组，9其他",description="长度为：10")
	private Integer mbType;
	/**
	* 成员ID
	*/
	@Schema(title="成员ID",description="长度为：19")
	private Long mbId;
	/**
	* 成员机构名称
	*/
	@Schema(title="成员机构名称",description="长度为：255")
	private String orgName;
	/**
	* 成员标题
	*/
	@Schema(title="成员标题",description="长度为：100")
	private String name;
	/**
	* 成员编码
	*/
	@Schema(title="成员编码",description="长度为：20")
	private String sn;
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
