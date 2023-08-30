package com.unione.cloud.portal.system.model;

import java.util.List;

import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysPost
 * 
 * @描述 SysPost类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long orgId		机构ID
 * 		<p>4.Long parentId		上级岗位ID
 * 		<p>5.String name		岗位名称
 * 		<p>6.String sn		岗位编码
 * 		<p>7.String lvsn		层级编码
 * 		<p>8.Integer level		所在层级
 * 		<p>9.Integer types		岗位类型，字典POSTTYPES 9其他
 * 		<p>10.String iconFont		岗位图标
 * 		<p>11.String iconPic		岗位图片
 * 		<p>12.String duty		岗位职责
 * 		<p>13.String descs		岗位说明
 * 		<p>14.Integer isLeaf		是否叶子节点，字典 TUREORFALSE 1是，0否
 * 		<p>15.Integer ordered		显示顺序
 * 		<p>16.Integer status		岗位状态，字典USEORNOT 1使用，0停用
 * 		<p>17.Integer delFlag		删除标记，0:正常,1:已删除
 * 		<p>18.Date created		
 * 		<p>19.Long createdBy		
 * 		<p>20.Date lastUpdated		
 * 		<p>21.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_POST
 * @数据库表备注:	 	岗位信息，岗位对应的行政区划信息直接存储在数据权限：岗位权限表中。一个岗位可以有多个行政区划，但都是当前用户所属机构关联
 * 
 * @作者	Jeking Yang
 * @日期	2023-8-31 0:00:33
 * @版本	1.0.0
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SysPost extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 上级岗位ID
	 */
	@ApiModelProperty(value="上级岗位ID",notes="字符长度为：19")
	private Long parentId;
	/**
	 * 岗位名称
	 */
	@ApiModelProperty(value="岗位名称",notes="字符长度为：200")
	private String name;
	/**
	 * 岗位编码
	 */
	@ApiModelProperty(value="岗位编码",notes="字符长度为：30")
	private String sn;
	/**
	 * 层级编码
	 */
	@ApiModelProperty(value="层级编码",notes="字符长度为：40")
	private String lvsn;
	/**
	 * 所在层级
	 */
	@ApiModelProperty(value="所在层级",notes="字符长度为：10")
	private Integer level;
	/**
	 * 岗位类型，字典POSTTYPES 9其他
	 */
	@ApiModelProperty(value="岗位类型，字典POSTTYPES 9其他",notes="字符长度为：10")
	private Integer types;
	/**
	 * 岗位图标
	 */
	@ApiModelProperty(value="岗位图标",notes="字符长度为：20")
	private String iconFont;
	/**
	 * 岗位图片
	 */
	@ApiModelProperty(value="岗位图片",notes="字符长度为：200")
	private String iconPic;
	/**
	 * 岗位职责
	 */
	@ApiModelProperty(value="岗位职责",notes="字符长度为：1,000")
	private String duty;
	/**
	 * 岗位说明
	 */
	@ApiModelProperty(value="岗位说明",notes="字符长度为：1,000")
	private String descs;
	/**
	 * 是否叶子节点，字典 TUREORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否叶子节点，字典 TUREORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isLeaf;
	/**
	 * 显示顺序
	 */
	@ApiModelProperty(value="显示顺序",notes="字符长度为：10")
	private Integer ordered;
	/**
	 * 岗位状态，字典USEORNOT 1使用，0停用
	 */
	@ApiModelProperty(value="岗位状态，字典USEORNOT 1使用，0停用",notes="字符长度为：10")
	private Integer status;
	/**
	 * 删除标记，0:正常,1:已删除
	 */
	@ApiModelProperty(value="删除标记，0:正常,1:已删除",notes="字符长度为：10")
	private Integer delFlag;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
