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
 * @日期	2024-03-22 08:03:37
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
	@ApiModelProperty(value="上级分组ID",notes="长度为：19")
	private Long parentId;
	/**
	* 分组名称
	*/
	@ApiModelProperty(value="分组名称",notes="长度为：200")
	private String name;
	/**
	* 分组编码
	*/
	@ApiModelProperty(value="分组编码",notes="长度为：30")
	private String sn;
	/**
	* 层级编码
	*/
	@ApiModelProperty(value="层级编码",notes="长度为：40")
	private String lvsn;
	/**
	* 所在层级
	*/
	@ApiModelProperty(value="所在层级",notes="长度为：10")
	private Integer level;
	/**
	* 分组类型，字典UGROUPTYPES 9其他
	*/
	@ApiModelProperty(value="分组类型，字典UGROUPTYPES 9其他",notes="长度为：10")
	private Integer types;
	/**
	* 分组图标
	*/
	@ApiModelProperty(value="分组图标",notes="长度为：20")
	private String iconFont;
	/**
	* 分组图片
	*/
	@ApiModelProperty(value="分组图片",notes="长度为：200")
	private String iconPic;
	/**
	* 分组说明
	*/
	@ApiModelProperty(value="分组说明",notes="长度为：1000")
	private String descs;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否叶子节点，字典 TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isLeaf;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 分组状态，字典UGROUPSTATUS 1正常，2解散
	*/
	@ApiModelProperty(value="分组状态，字典UGROUPSTATUS 1正常，2解散",notes="长度为：10")
	private Integer status;

}
