package com.unione.cloud.portal.system.model;

import java.util.List;

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
 * @类名 <p>SysUserPermis
 * 
 * @描述 SysUserPermis类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long userId		用户ID
 * 		<p>4.Long appId		应用ID
 * 		<p>5.Long resId		资源ID
 * 		<p>6.String resType		资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
 * 		<p>7.Integer enDilivery		是否可传递授权，1是，0否
 * 		<p>8.Date created		
 * 		<p>9.Long createdBy		
 * 		<p>10.Date lastUpdated		
 * 		<p>11.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_USER_PERMIS
 * @数据库表备注:	 	系统管理：用户权限
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
public class SysUserPermis extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 应用ID
	 */
	@ApiModelProperty(value="应用ID",notes="字符长度为：19")
	private Long appId;
	/**
	 * 资源ID
	 */
	@ApiModelProperty(value="资源ID",notes="字符长度为：19")
	private Long resId;
	/**
	 * 资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
	 */
	@ApiModelProperty(value="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",notes="字符长度为：20")
	private String resType;
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
