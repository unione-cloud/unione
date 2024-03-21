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
 * @类名 <p>SysApiInfo
 * 
 * @描述 SysApiInfo类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long appId		应用ID
 * 		<p>3.Long parentId		上级ID(根节点为-1)
 * 		<p>4.String name		名称（英文名称，一个应用中保持唯一）
 * 		<p>5.String title		标题
 * 		<p>6.String url		URL地址,/开头，不包含应用ctx
 * 		<p>7.String method		请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD
 * 		<p>8.String params		请求参数
 * 		<p>9.String response		接口响应
 * 		<p>10.Integer isLeaf		是否叶子节点，字典 TUREORFALSE 1是，0否
 * 		<p>11.Integer isNeedPermis		是否需要授权，字典 TUREORFALSE 1是，0否
 * 		<p>12.Integer ordered		显示顺序
 * 		<p>13.Integer status		状态，字典，使用状态 USEORNOT 1使用，0停用
 * 		<p>14.String descs		描述
 * 		<p>15.String docBody		文档内容
 * 		<p>16.Date created		
 * 		<p>17.Long createdBy		
 * 		<p>18.Date lastUpdated		
 * 		<p>19.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_API_INFO
 * @数据库表备注:	 	系统管理：接口信息
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
public class SysApiInfo extends Pojo{
	
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
	 * 上级ID(根节点为-1)
	 */
	@ApiModelProperty(value="上级ID(根节点为-1)",notes="字符长度为：19")
	private Long parentId;
	/**
	 * 名称（英文名称，一个应用中保持唯一）
	 */
	@ApiModelProperty(value="名称（英文名称，一个应用中保持唯一）",notes="字符长度为：50")
	private String name;
	/**
	 * 标题
	 */
	@ApiModelProperty(value="标题",notes="字符长度为：100")
	private String title;
	/**
	 * URL地址,/开头，不包含应用ctx
	 */
	@ApiModelProperty(value="URL地址,/开头，不包含应用ctx",notes="字符长度为：250")
	private String url;
	/**
	 * 请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD
	 */
	@ApiModelProperty(value="请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD",notes="字符长度为：50")
	private String method;
	/**
	 * 请求参数
	 */
	@ApiModelProperty(value="请求参数",notes="字符长度为：2,147,483,647")
	private String params;
	/**
	 * 接口响应
	 */
	@ApiModelProperty(value="接口响应",notes="字符长度为：2,147,483,647")
	private String response;
	/**
	 * 是否叶子节点，字典 TUREORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否叶子节点，字典 TUREORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isLeaf;
	/**
	 * 是否需要授权，字典 TUREORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否需要授权，字典 TUREORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isNeedPermis;
	/**
	 * 显示顺序
	 */
	@ApiModelProperty(value="显示顺序",notes="字符长度为：10")
	private Integer ordered;
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
	/**
	 * 文档内容
	 */
	@ApiModelProperty(value="文档内容",notes="字符长度为：2,147,483,647")
	private String docBody;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
