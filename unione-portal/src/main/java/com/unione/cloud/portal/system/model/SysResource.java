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
 * @类名 <p>SysResource
 * 
 * @描述 SysResource类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long appId		应用ID
 * 		<p>3.Long parentId		上级菜单ID(根节点为-1)
 * 		<p>4.String name		资源名称，唯一
 * 		<p>5.String title		资源标题
 * 		<p>6.String alias		资源别名（授权树区别重复菜单名称）
 * 		<p>7.String types		资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
 * 		<p>8.String url		资源URL地址
 * 		<p>9.Integer isIframe		是否iframe打开，字典TRUEORFALSE 1是，0否
 * 		<p>10.Integer isExternal		是否外部链接，字典TRUEORFALSE 1是，0否
 * 		<p>11.Integer isHide		是否隐藏，字典 TUREORFALSE 1是，0否
 * 		<p>12.Integer isLeaf		是否叶子节点，字典 TUREORFALSE 1是，0否
 * 		<p>13.Integer isNeedPermis		是否需要授权，字典 TUREORFALSE 1是，0否
 * 		<p>14.String icon		图标（字体图标）
 * 		<p>15.String picMax		大图标(图片图标)
 * 		<p>16.String picMid		中图标(图片图标)
 * 		<p>17.String picMix		小图标(图片图标)
 * 		<p>18.Integer ordered		显示顺序
 * 		<p>19.Integer status		状态，字典，使用状态 USEORNOT 1使用，0停用
 * 		<p>20.String descs		描述
 * 		<p>21.String configs		资源设置,JSON存储
 * 		<p>22.Date created		
 * 		<p>23.Long createdBy		
 * 		<p>24.Date lastUpdated		
 * 		<p>25.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_RESOURCE
 * @数据库表备注:	 	系统管理：系统资源
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
public class SysResource extends Pojo{
	
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
	 * 上级菜单ID(根节点为-1)
	 */
	@ApiModelProperty(value="上级菜单ID(根节点为-1)",notes="字符长度为：19")
	private Long parentId;
	/**
	 * 资源名称，唯一
	 */
	@ApiModelProperty(value="资源名称，唯一",notes="字符长度为：100")
	private String name;
	/**
	 * 资源标题
	 */
	@ApiModelProperty(value="资源标题",notes="字符长度为：100")
	private String title;
	/**
	 * 资源别名（授权树区别重复菜单名称）
	 */
	@ApiModelProperty(value="资源别名（授权树区别重复菜单名称）",notes="字符长度为：100")
	private String alias;
	/**
	 * 资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
	 */
	@ApiModelProperty(value="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",notes="字符长度为：20")
	private String types;
	/**
	 * 资源URL地址
	 */
	@ApiModelProperty(value="资源URL地址",notes="字符长度为：250")
	private String url;
	/**
	 * 是否iframe打开，字典TRUEORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否iframe打开，字典TRUEORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isIframe;
	/**
	 * 是否外部链接，字典TRUEORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否外部链接，字典TRUEORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isExternal;
	/**
	 * 是否隐藏，字典 TUREORFALSE 1是，0否
	 */
	@ApiModelProperty(value="是否隐藏，字典 TUREORFALSE 1是，0否",notes="字符长度为：10")
	private Integer isHide;
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
	 * 图标（字体图标）
	 */
	@ApiModelProperty(value="图标（字体图标）",notes="字符长度为：100")
	private String icon;
	/**
	 * 大图标(图片图标)
	 */
	@ApiModelProperty(value="大图标(图片图标)",notes="字符长度为：250")
	private String picMax;
	/**
	 * 中图标(图片图标)
	 */
	@ApiModelProperty(value="中图标(图片图标)",notes="字符长度为：250")
	private String picMid;
	/**
	 * 小图标(图片图标)
	 */
	@ApiModelProperty(value="小图标(图片图标)",notes="字符长度为：250")
	private String picMix;
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
	 * 资源设置,JSON存储
	 */
	@ApiModelProperty(value="资源设置,JSON存储",notes="字符长度为：1,000")
	private String configs;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
}
