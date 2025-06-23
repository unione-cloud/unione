package com.unione.cloud.system.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.beetsql.annotation.KeyWords;
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
 * @标题 	SysGroupMember Entity
 * @描述	系统管理：分组成员
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_group_member")
@SqlResource("system.SysGroupMember")
public class SysGroupMember extends Pojo {
	/**
	* 分组ID
	*/
	@Schema(title="分组ID",description="长度为：19")
	@NotNull(message = "分组ID不能为空",groups= {Validator.save.class,Validator.update.class})
	private Long groupId;
	/**
	* 成员类型，字典GROUPTYPES 1用户分组，9其他
	*/
	@Schema(title="成员类型，字典GROUPTYPES 1用户分组，9其他",description="长度为：10")
	private Integer mbType;
	
	/**
	* 成员机构名称
	*/
	@KeyWords
	@Schema(title="成员机构名称",description="长度为：255")
	private String orgName;
	/**
	* 成员标题
	*/
	@KeyWords
	@Schema(title="成员标题",description="长度为：100")
	private String name;
	/**
	* 成员编码
	*/
	@KeyWords
	@Schema(title="成员编码",description="长度为：20")
	private String sn;
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
	@KeyWords
	@Schema(title="成员描述",description="长度为：1000")
	private String descs;

}
