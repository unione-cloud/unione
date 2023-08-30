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
 * @类名 <p>SysOrgan
 * 
 * @描述 SysOrgan类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long areaId		所在区域ID
 * 		<p>4.Long parentId		上级ID
 * 		<p>5.String name		企业/机构名称
 * 		<p>6.String alias		企业/机构别名
 * 		<p>7.String codes		编码
 * 		<p>8.Integer types		类型：字典ORGTYPES 1企业，2机构，3部门
 * 		<p>9.String busiMain		主营业务
 * 		<p>10.String busiScop		经营范围
 * 		<p>11.String addr		企业/机构地址
 * 		<p>12.String tel		联系电话
 * 		<p>13.Integer levels		级别
 * 		<p>14.Integer isLeaf		是否叶子节点 1：是叶子节点 0：非叶子节点
 * 		<p>15.Integer ordered		显示顺序
 * 		<p>16.Integer status		状态
 * 		<p>17.String descs		说明
 * 		<p>18.Date created		
 * 		<p>19.Long createdBy		
 * 		<p>20.Date lastUpdated		
 * 		<p>21.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_ORGAN
 * @数据库表备注:	 	系统管理：机构信息
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
public class SysOrgan extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 所在区域ID
	 */
	@ApiModelProperty(value="所在区域ID",notes="字符长度为：19")
	private Long areaId;
	/**
	 * 上级ID
	 */
	@ApiModelProperty(value="上级ID",notes="字符长度为：19")
	private Long parentId;
	/**
	 * 企业/机构名称
	 */
	@ApiModelProperty(value="企业/机构名称",notes="字符长度为：250")
	private String name;
	/**
	 * 企业/机构别名
	 */
	@ApiModelProperty(value="企业/机构别名",notes="字符长度为：250")
	private String alias;
	/**
	 * 编码
	 */
	@ApiModelProperty(value="编码",notes="字符长度为：100")
	private String codes;
	/**
	 * 类型：字典ORGTYPES 1企业，2机构，3部门
	 */
	@ApiModelProperty(value="类型：字典ORGTYPES 1企业，2机构，3部门",notes="字符长度为：10")
	private Integer types;
	/**
	 * 主营业务
	 */
	@ApiModelProperty(value="主营业务",notes="字符长度为：500")
	private String busiMain;
	/**
	 * 经营范围
	 */
	@ApiModelProperty(value="经营范围",notes="字符长度为：500")
	private String busiScop;
	/**
	 * 企业/机构地址
	 */
	@ApiModelProperty(value="企业/机构地址",notes="字符长度为：250")
	private String addr;
	/**
	 * 联系电话
	 */
	@ApiModelProperty(value="联系电话",notes="字符长度为：50")
	private String tel;
	/**
	 * 级别
	 */
	@ApiModelProperty(value="级别",notes="字符长度为：10")
	private Integer levels;
	/**
	 * 是否叶子节点 1：是叶子节点 0：非叶子节点
	 */
	@ApiModelProperty(value="是否叶子节点 1：是叶子节点 0：非叶子节点",notes="字符长度为：10")
	private Integer isLeaf;
	/**
	 * 显示顺序
	 */
	@ApiModelProperty(value="显示顺序",notes="字符长度为：10")
	private Integer ordered;
	/**
	 * 状态
	 */
	@ApiModelProperty(value="状态",notes="字符长度为：10")
	private Integer status;
	/**
	 * 说明
	 */
	@ApiModelProperty(value="说明",notes="字符长度为：500")
	private String descs;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
