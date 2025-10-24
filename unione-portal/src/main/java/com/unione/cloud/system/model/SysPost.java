package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.beetsql.annotation.QueryAction;
import com.unione.cloud.beetsql.builder.SqlAction;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysPost Entity
 * @描述	岗位信息，岗位对应的行政区划信息直接存储在数据权限：岗位权限表中。一个岗位可以有多个行政区划，但都是当前用户所属机构关联
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("system.SysPost")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_post")
public class SysPost extends Pojo {
	/**
	* 上级岗位ID
	*/
	@JsonProperty("pid")
	@Schema(title="上级岗位ID",description="长度为：19")
	private Long parentId;
	/**
	* 岗位名称
	*/
	@KeyWords
	@Schema(title="岗位名称",description="长度为：200")
	private String name;
	/**
	* 岗位编码
	*/
	@KeyWords
	@Schema(title="岗位编码",description="长度为：50")
	private String sn;
	/**
	* 层级编码：自动生成
	*/
	@QueryAction(SqlAction.LIKER)
	@Schema(title="层级编码",description="层级编码：自动生成，长度为：100")
	private String lvSn;
	/**
	* 所在层级
	*/
	@Schema(title="所在层级",description="长度为：10")
	private Integer lvNo;
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
	@KeyWords
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
