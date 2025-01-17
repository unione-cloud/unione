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
 * @日期	2024-03-25 20:34:17
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
	@Schema(title="上级岗位ID",description="长度为：19")
	private Long parentId;
	/**
	* 岗位名称
	*/
	@Schema(title="岗位名称",description="长度为：200")
	private String name;
	/**
	* 岗位编码，携带层级的岗位编码
	*/
	@Schema(title="岗位编码，携带层级的岗位编码",description="长度为：50")
	private String sn;
	/**
	* 岗位类型，字典POSTTYPES 9其他
	*/
	@Schema(title="岗位类型，字典POSTTYPES 9其他",description="长度为：10")
	private Integer types;
	/**
	* 岗位图标
	*/
	@Schema(title="岗位图标",description="长度为：20")
	private String iconFont;
	/**
	* 岗位图片
	*/
	@Schema(title="岗位图片",description="长度为：200")
	private String iconPic;
	/**
	* 岗位职责
	*/
	@Schema(title="岗位职责",description="长度为：1000")
	private String duty;
	/**
	* 岗位说明
	*/
	@Schema(title="岗位说明",description="长度为：1000")
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
	* 岗位状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="岗位状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
