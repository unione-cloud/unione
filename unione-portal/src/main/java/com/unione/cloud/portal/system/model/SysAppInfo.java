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
 * @类名 <p>SysAppInfo
 * 
 * @描述 SysAppInfo类属性
 * 		<p>1.Long sid		
 * 		<p>2.String name		应用名称
 * 		<p>3.String codes		应用编码
 * 		<p>4.Integer isMp		是否微应用
 * 		<p>5.String url		应用URL
 * 		<p>6.String welcome		首页URL
 * 		<p>7.String versNo		版本号
 * 		<p>8.String versDesc		版本说明
 * 		<p>9.String icon		图标（字体图标）
 * 		<p>10.String picMax		大图标(图片图标)
 * 		<p>11.String picMid		中图标(图片图标)
 * 		<p>12.String picMix		小图标(图片图标)
 * 		<p>13.Integer ordered		显示顺序
 * 		<p>14.Integer status		状态，字典，使用状态 USEORNOT 1使用，0停用
 * 		<p>15.String descs		描述
 * 		<p>16.Date created		
 * 		<p>17.Long createdBy		
 * 		<p>18.Date lastUpdated		
 * 		<p>19.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_APP_INFO
 * @数据库表备注:	 	系统管理：应用信息
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
public class SysAppInfo extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 应用名称
	 */
	@ApiModelProperty(value="应用名称",notes="字符长度为：100")
	private String name;
	/**
	 * 应用编码
	 */
	@ApiModelProperty(value="应用编码",notes="字符长度为：50")
	private String codes;
	/**
	 * 是否微应用
	 */
	@ApiModelProperty(value="是否微应用",notes="字符长度为：10")
	private Integer isMp;
	/**
	 * 应用URL
	 */
	@ApiModelProperty(value="应用URL",notes="字符长度为：250")
	private String url;
	/**
	 * 首页URL
	 */
	@ApiModelProperty(value="首页URL",notes="字符长度为：250")
	private String welcome;
	/**
	 * 版本号
	 */
	@ApiModelProperty(value="版本号",notes="字符长度为：30")
	private String versNo;
	/**
	 * 版本说明
	 */
	@ApiModelProperty(value="版本说明",notes="字符长度为：500")
	private String versDesc;
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
	// fields end
	
}
