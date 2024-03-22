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
 * @标题 	SysPost Entity
 * @描述	岗位信息，岗位对应的行政区划信息直接存储在数据权限：岗位权限表中。一个岗位可以有多个行政区划，但都是当前用户所属机构关联
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysPost")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_post")
public class SysPost extends Pojo {
	/**
	* 上级岗位ID
	*/
	@ApiModelProperty(value="上级岗位ID",notes="长度为：19")
	private Long parentId;
	/**
	* 岗位名称
	*/
	@ApiModelProperty(value="岗位名称",notes="长度为：200")
	private String name;
	/**
	* 岗位编码
	*/
	@ApiModelProperty(value="岗位编码",notes="长度为：30")
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
	* 岗位类型，字典POSTTYPES 9其他
	*/
	@ApiModelProperty(value="岗位类型，字典POSTTYPES 9其他",notes="长度为：10")
	private Integer types;
	/**
	* 岗位图标
	*/
	@ApiModelProperty(value="岗位图标",notes="长度为：20")
	private String iconFont;
	/**
	* 岗位图片
	*/
	@ApiModelProperty(value="岗位图片",notes="长度为：200")
	private String iconPic;
	/**
	* 岗位职责
	*/
	@ApiModelProperty(value="岗位职责",notes="长度为：1000")
	private String duty;
	/**
	* 岗位说明
	*/
	@ApiModelProperty(value="岗位说明",notes="长度为：1000")
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
	* 岗位状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="岗位状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;

}
