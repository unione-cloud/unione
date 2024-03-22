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
 * @标题 	SysTenant Entity
 * @描述	系统管理：租户信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysTenant")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_tenant")
public class SysTenant extends Pojo {
	/**
	* 租户标识
	*/
	@ApiModelProperty(value="租户标识",notes="长度为：50")
	private String sn;
	/**
	* 租户名称
	*/
	@ApiModelProperty(value="租户名称",notes="长度为：100")
	private String name;
	/**
	* 租户域名,系统访问二级域名
	*/
	@ApiModelProperty(value="租户域名,系统访问二级域名",notes="长度为：30")
	private String domain;
	/**
	* 租户LOGO
	*/
	@ApiModelProperty(value="租户LOGO",notes="长度为：200")
	private String logo;
	/**
	* 登录广告
	*/
	@ApiModelProperty(value="登录广告",notes="长度为：200")
	private String loginAd;
	/**
	* 租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建
	*/
	@ApiModelProperty(value="租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建",notes="长度为：10")
	private Integer registeWay;
	/**
	* 联系人
	*/
	@ApiModelProperty(value="联系人",notes="长度为：30")
	private String linkMan;
	/**
	* 联系地址
	*/
	@ApiModelProperty(value="联系地址",notes="长度为：200")
	private String linkAdd;
	/**
	* 联系电话
	*/
	@ApiModelProperty(value="联系电话",notes="长度为：20")
	private String linkTel;
	/**
	* 所在城市
	*/
	@ApiModelProperty(value="所在城市",notes="长度为：50")
	private String locationCity;
	/**
	* 所在省份
	*/
	@ApiModelProperty(value="所在省份",notes="长度为：50")
	private String locationProvince;
	/**
	* 开户时间
	*/
	@ApiModelProperty(value="开户时间",notes="长度为：19")
	private Long openTime;
	/**
	* 最大用户数量
	*/
	@ApiModelProperty(value="最大用户数量",notes="长度为：10")
	private Integer maxUserCount;
	/**
	* 最大用户在线数量
	*/
	@ApiModelProperty(value="最大用户在线数量",notes="长度为：10")
	private Integer maxUserOnline;
	/**
	* 最大机构数量
	*/
	@ApiModelProperty(value="最大机构数量",notes="长度为：10")
	private Integer maxOrganCount;
	/**
	* 每个结构最大用户数
	*/
	@ApiModelProperty(value="每个结构最大用户数",notes="长度为：10")
	private Integer maxOrganUserCouint;
	/**
	* 租户状态,字典TENANTSTATUS 1新建，2开通，3关闭
	*/
	@ApiModelProperty(value="租户状态,字典TENANTSTATUS 1新建，2开通，3关闭",notes="长度为：10")
	private Integer status;
	/**
	* 租户备注
	*/
	@ApiModelProperty(value="租户备注",notes="长度为：1000")
	private String descs;

}
