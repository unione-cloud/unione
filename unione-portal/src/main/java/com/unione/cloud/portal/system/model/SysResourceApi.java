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
 * @类名 <p>SysResourceApi
 * 
 * @描述 SysResourceApi类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long appId		应用ID
 * 		<p>3.Long resId		资源ID
 * 		<p>4.String resType		资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
 * 		<p>5.Long apiId		接口ID
 * 		<p>6.Integer status		状态，字典，使用状态 USEORNOT 1使用，0停用
 * 		<p>7.Date created		
 * 		<p>8.Long createdBy		
 * 		<p>9.Date lastUpdated		
 * 		<p>10.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_RESOURCE_API
 * @数据库表备注:	 	系统管理：资源接口
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
public class SysResourceApi extends Pojo{
	
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
	 * 接口ID
	 */
	@ApiModelProperty(value="接口ID",notes="字符长度为：19")
	private Long apiId;
	/**
	 * 状态，字典，使用状态 USEORNOT 1使用，0停用
	 */
	@ApiModelProperty(value="状态，字典，使用状态 USEORNOT 1使用，0停用",notes="字符长度为：10")
	private Integer status;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
