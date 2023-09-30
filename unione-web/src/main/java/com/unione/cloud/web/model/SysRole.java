package com.unione.cloud.web.model;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysRole
 * 
 * @描述 SysRole类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long orgId		机构ID
 * 		<p>4.String name		名称
 * 		<p>5.String codes		编码
 * 		<p>6.Integer types		类型，字典ROLETYPE 
 * 		<p>7.Integer status		状态，字典，使用状态 USEORNOT 1使用，0停用
 * 		<p>8.String descs		描述
 * 		<p>9.Date created		
 * 		<p>10.Long createdBy		
 * 		<p>11.Date lastUpdated		
 * 		<p>12.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_ROLE
 * @数据库表备注:	 	系统管理：角色信息
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
@Table(name="SYS_ROLE")
public class SysRole extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 名称
	 */
	@ApiModelProperty(value="名称",notes="字符长度为：100")
	private String name;
	/**
	 * 编码
	 */
	@ApiModelProperty(value="编码",notes="字符长度为：50")
	private String codes;
	/**
	 * 类型，字典ROLETYPE 
	 */
	@ApiModelProperty(value="类型，字典ROLETYPE ",notes="字符长度为：10")
	private Integer types;
	/**
	 * 状态，字典，使用状态 USEORNOT 1使用，0停用
	 */
	@ApiModelProperty(value="状态，字典，使用状态 USEORNOT 1使用，0停用",notes="字符长度为：10")
	private Integer status;
	/**
	 * 描述
	 */
	@ApiModelProperty(value="描述",notes="字符长度为：500")
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
