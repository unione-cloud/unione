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
 * @标题 	SysGroup Entity
 * @描述	系统管理：分组，分组对应的行政区划信息直接存储在数据权限：行政区划权限表中。一个分组可以有多个行政区划，但都是当前用户所
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysGroup")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_group")
public class SysGroup extends Pojo {
	/**
	* 上级分组ID
	*/
	@Schema(title="上级分组ID",description="长度为：19")
	private Long parentId;
	/**
	* 分组名称
	*/
	@Schema(title="分组名称",description="长度为：200")
	private String name;
	/**
	* 分组编码，携带层级的编码
	*/
	@Schema(title="分组编码，携带层级的编码",description="长度为：30")
	private String sn;
	/**
	* 所在层级
	*/
	@Schema(title="所在层级",description="长度为：10")
	private Integer level;
	/**
	* 分组类型，字典GROUPTYPES 1用户分组，9其他
	*/
	@Schema(title="分组类型，字典GROUPTYPES 1用户分组，9其他",description="长度为：10")
	private Integer types;
	/**
	* 分组图标
	*/
	@Schema(title="分组图标",description="长度为：20")
	private String iconFont;
	/**
	* 分组图片
	*/
	@Schema(title="分组图片",description="长度为：200")
	private String iconPic;
	/**
	* 分组说明
	*/
	@Schema(title="分组说明",description="长度为：1000")
	private String descs;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否叶子节点，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 分组状态，字典UGROUPSTATUS 1正常，2解散
	*/
	@Schema(title="分组状态，字典UGROUPSTATUS 1正常，2解散",description="长度为：10")
	private Integer status;

}
