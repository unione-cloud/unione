package com.unione.cloud.system.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysTenant Entity
 * @描述	系统管理：租户信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
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
	@KeyWords
	@Schema(title="租户标识",description="长度为：50")
	private String sn;
	/**
	* 租户名称
	*/
	@KeyWords
	@Schema(title="租户名称",description="长度为：100")
	private String name;
	/**
	* 租户域名,系统访问二级域名
	*/
	@KeyWords
	@Schema(title="租户域名,系统访问二级域名",description="长度为：30")
	private String domain;
	/**
	* 租户LOGO
	*/
	@Schema(title="租户LOGO",description="长度为：200")
	private String logo;
	/**
	* 登录广告
	*/
	@Schema(title="登录广告",description="长度为：200")
	private String loginAd;
	/**
	* 租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建
	*/
	@Schema(title="租户注册方式,字典TENANTREGTYPE 1自主注册，2客服创建",description="长度为：10")
	private Integer registeWay;
	/**
	* 租户管理员id
	*/
	@Schema(title="租户管理员id",description="长度为：10")
	private Long adminId;
	/**
	* 联系人
	*/
	@Schema(title="联系人",description="长度为：30")
	private String linkMan;
	/**
	* 联系地址
	*/
	@Schema(title="联系地址",description="长度为：200")
	private String linkAdd;
	/**
	* 联系电话
	*/
	@Schema(title="联系电话",description="长度为：20")
	private String linkTel;
	/**
	* 所在城市
	*/
	@Schema(title="所在城市",description="长度为：50")
	private String locationCity;
	/**
	* 所在省份
	*/
	@Schema(title="所在省份",description="长度为：50")
	private String locationProvince;
	/**
	* 开户时间
	*/
	@Schema(title="开户时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date openTime;
	/**
	* 最大用户数量
	*/
	@Schema(title="最大用户数量",description="长度为：10")
	private Integer maxUserCount;
	/**
	* 最大用户在线数量
	*/
	@Schema(title="最大用户在线数量",description="长度为：10")
	private Integer maxUserOnline;
	/**
	* 最大机构数量
	*/
	@Schema(title="最大机构数量",description="长度为：10")
	private Integer maxOrganCount;
	/**
	* 每个结构最大用户数
	*/
	@Schema(title="每个结构最大用户数",description="长度为：10")
	private Integer maxOrganUserCouint;
	/**
	* 限制时间起
	*/
	@Schema(title="限制时间起",description="长度为：10")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date timeLimitStart;
	/**
	* 限制时间止
	*/
	@Schema(title="限制时间止",description="长度为：10")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date timeLimitEnd;
	/**
	* 租户状态,字典TENANTSTATUS 1试用，2开通，3关闭
	*/
	@Schema(title="租户状态,字典TENANTSTATUS 1试用，2开通，3关闭",description="长度为：10")
	private Integer status;
	/**
	* 租户备注
	*/
	@KeyWords
	@Schema(title="租户备注",description="长度为：1000")
	private String descs;

}
