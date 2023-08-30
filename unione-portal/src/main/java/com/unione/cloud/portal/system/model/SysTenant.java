package com.unione.cloud.portal.system.model;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysTenant
 * 
 * @描述 SysTenant类属性
 * 		<p>1.Long sid		
 * 		<p>2.String sn		租户标识
 * 		<p>3.String name		租户名称
 * 		<p>4.String domain		租户域名,系统访问二级域名
 * 		<p>5.String logo		租户LOGO
 * 		<p>6.String loginAd		登录广告
 * 		<p>7.Integer registeWay		租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建
 * 		<p>8.String linkMan		联系人
 * 		<p>9.String linkAdd		联系地址
 * 		<p>10.String linkTel		联系电话
 * 		<p>11.String locationCity		所在城市
 * 		<p>12.String locationProvince		所在省份
 * 		<p>13.Date openTime		开户时间
 * 		<p>14.Integer maxUserCount		最大用户数量
 * 		<p>15.Integer maxUserOnline		最大用户在线数量
 * 		<p>16.Integer maxOrganCount		最大机构数量
 * 		<p>17.Integer maxOrganUserCouint		每个结构最大用户数
 * 		<p>18.Integer status		租户状态,字典TENANTSTATUS 1新建，2开通，3关闭
 * 		<p>19.Integer delFlag		删除标记，字典TUREORFALSE 1是，0否
 * 		<p>20.String descs		租户备注
 * 		<p>21.Date created		创建时间
 * 		<p>22.Long createdBy		创建人员
 * 		<p>23.Date lastUpdated		更新时间
 * 		<p>24.Long lastUpdatedBy		更新人员
 *      
 * @数据库表名称:		SYS_TENANT
 * @数据库表备注:	 	系统管理：租户信息
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
public class SysTenant extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 租户标识
	 */
	@ApiModelProperty(value="租户标识",notes="字符长度为：50")
	private String sn;
	/**
	 * 租户名称
	 */
	@ApiModelProperty(value="租户名称",notes="字符长度为：100")
	private String name;
	/**
	 * 租户域名,系统访问二级域名
	 */
	@ApiModelProperty(value="租户域名,系统访问二级域名",notes="字符长度为：30")
	private String domain;
	/**
	 * 租户LOGO
	 */
	@ApiModelProperty(value="租户LOGO",notes="字符长度为：200")
	private String logo;
	/**
	 * 登录广告
	 */
	@ApiModelProperty(value="登录广告",notes="字符长度为：200")
	private String loginAd;
	/**
	 * 租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建
	 */
	@ApiModelProperty(value="租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建",notes="字符长度为：10")
	private Integer registeWay;
	/**
	 * 联系人
	 */
	@ApiModelProperty(value="联系人",notes="字符长度为：30")
	private String linkMan;
	/**
	 * 联系地址
	 */
	@ApiModelProperty(value="联系地址",notes="字符长度为：200")
	private String linkAdd;
	/**
	 * 联系电话
	 */
	@ApiModelProperty(value="联系电话",notes="字符长度为：20")
	private String linkTel;
	/**
	 * 所在城市
	 */
	@ApiModelProperty(value="所在城市",notes="字符长度为：50")
	private String locationCity;
	/**
	 * 所在省份
	 */
	@ApiModelProperty(value="所在省份",notes="字符长度为：50")
	private String locationProvince;
	/**
	 * 开户时间
	 */
	@ApiModelProperty(value="开户时间",notes="字符长度为：26")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date openTime;
	/**
	 * 最大用户数量
	 */
	@ApiModelProperty(value="最大用户数量",notes="字符长度为：10")
	private Integer maxUserCount;
	/**
	 * 最大用户在线数量
	 */
	@ApiModelProperty(value="最大用户在线数量",notes="字符长度为：10")
	private Integer maxUserOnline;
	/**
	 * 最大机构数量
	 */
	@ApiModelProperty(value="最大机构数量",notes="字符长度为：10")
	private Integer maxOrganCount;
	/**
	 * 每个结构最大用户数
	 */
	@ApiModelProperty(value="每个结构最大用户数",notes="字符长度为：10")
	private Integer maxOrganUserCouint;
	/**
	 * 租户状态,字典TENANTSTATUS 1新建，2开通，3关闭
	 */
	@ApiModelProperty(value="租户状态,字典TENANTSTATUS 1新建，2开通，3关闭",notes="字符长度为：10")
	private Integer status;
	/**
	 * 删除标记，字典TUREORFALSE 1是，0否
	 */
	@ApiModelProperty(value="删除标记，字典TUREORFALSE 1是，0否",notes="字符长度为：10")
	private Integer delFlag;
	/**
	 * 租户备注
	 */
	@ApiModelProperty(value="租户备注",notes="字符长度为：4,096")
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
