package com.unione.cloud.system.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysUserPost Entity
 * @描述	系统管理：用户岗位
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user_post")
@SqlResource("system.SysUserPost")
public class SysUserPost extends Pojo {
	/**
	* 岗位ID
	*/
	@Schema(title="岗位ID",description="长度为：19")
	private Long postId;
	/**
	* 用户所属机构名称
	*/
	@Schema(title="用户所属机构名称",description="长度为：255")
	private String orgName;
	/**
	* 成员姓名
	*/
	@Schema(title="成员姓名",description="长度为：100")
	private String name;
	/**
	* 加入时间
	*/
	@Schema(title="加入时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeJoin;
	/**
	* 离开时间
	*/
	@Schema(title="离开时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeLeave;
	/**
	* 成员状态，字典MENBERSTATUS 1正常，2离开
	*/
	@Schema(title="成员状态，字典MENBERSTATUS 1正常，2离开",description="长度为：10")
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
