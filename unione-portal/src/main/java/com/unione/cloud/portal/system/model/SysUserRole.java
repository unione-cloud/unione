package com.unione.cloud.portal.system.model;

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
 * @类名 <p>SysUserRole
 * 
 * @描述 SysUserRole类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long userId		用户ID
 * 		<p>4.Long roleId		角色ID
 * 		<p>5.Integer enDilivery		是否可传递授权，1是，0否
 * 		<p>6.Date created		
 * 		<p>7.Long createdBy		
 * 		<p>8.Date lastUpdated		
 * 		<p>9.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_USER_ROLE
 * @数据库表备注:	 	系统管理：用户角色
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
@Table(name="SYS_USER_ROLE")
public class SysUserRole extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 角色ID
	 */
	@ApiModelProperty(value="角色ID",notes="字符长度为：19")
	private Long roleId;
	/**
	 * 是否可传递授权，1是，0否
	 */
	@ApiModelProperty(value="是否可传递授权，1是，0否",notes="字符长度为：10")
	private Integer enDilivery;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
