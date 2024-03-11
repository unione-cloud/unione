package com.unione.cloud.portal.system.model;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniDataPermis.DataPermis;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysUserOrgan
 * 
 * @描述 SysUserOrgan类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long orgId		机构ID
 * 		<p>4.Long userId		用户ID
 * 		<p>5.Date timeJoin		加入时间
 * 		<p>6.Date timeLeave		离开时间
 * 		<p>7.Integer status		状态，字典UGROUPMENSTATUS 1正常，2离开
 * 		<p>8.Integer ordered		显示顺序
 * 		<p>9.Integer delFlag		删除标记，0:正常,1:已删除
 * 		<p>10.Date created		
 * 		<p>11.Long createdBy		
 * 		<p>12.Date lastUpdated		
 * 		<p>13.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_USER_ORGAN
 * @数据库表备注:	 	系统管理：用户机构
 * 
 * @作者	Jeking Yang
 * @日期	2023-8-31 0:00:34
 * @版本	1.0.0
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@UniDataPermis(DataPermis.TENANTID)
public class SysUserOrgan extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 加入时间
	 */
	@ApiModelProperty(value="加入时间",notes="字符长度为：26")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date timeJoin;
	/**
	 * 离开时间
	 */
	@ApiModelProperty(value="离开时间",notes="字符长度为：26")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date timeLeave;
	/**
	 * 状态，字典UGROUPMENSTATUS 1正常，2离开
	 */
	@ApiModelProperty(value="状态，字典UGROUPMENSTATUS 1正常，2离开",notes="字符长度为：10")
	private Integer status;
	/**
	 * 显示顺序
	 */
	@ApiModelProperty(value="显示顺序",notes="字符长度为：10")
	private Integer ordered;
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
