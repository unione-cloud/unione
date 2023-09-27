package com.unione.cloud.portal.system.model;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysUser
 * 
 * @描述 SysUser类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long tenantId		租户ID
 * 		<p>3.Long orgId		机构ID，用户默认机构
 * 		<p>4.Integer userType		用户类型，字典USERTYPE 1管理员，2普通用户，9其他
 * 		<p>5.String username		登录帐号
 * 		<p>6.String pwdText		用户密码
 * 		<p>7.String pwdSalt		密码加密盐
 * 		<p>8.String realName		真实姓名
 * 		<p>9.String aliasName		别名
 * 		<p>10.String portrait		头像
 * 		<p>11.Date birthday		生日
 * 		<p>12.Integer sex		性别，字典SEX 1女，2男
 * 		<p>13.String email		邮箱
 * 		<p>14.String qq		联系qq
 * 		<p>15.String tel		联系电话
 * 		<p>16.String securityQuestion		密保问题
 * 		<p>17.String sucurityMfa		MFA设备标识
 * 		<p>18.Date lastLoginTime		上次登录时间
 * 		<p>19.String lastLoginIp		上次登录ip
 * 		<p>20.Integer status		用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
 * 		<p>21.Date lockTime		锁定时间，锁定时间后才能继续登录
 * 		<p>22.String descs		描述
 * 		<p>23.Date created		
 * 		<p>24.Long createdBy		
 * 		<p>25.Date lastUpdated		
 * 		<p>26.Long lastUpdatedBy		
 *      
 * @数据库表名称:		SYS_USER
 * @数据库表备注:	 	系统管理：用户信息
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
public class SysUser extends Pojo{
	
//	/**
//	 * 	数据验证demo
//	 */
//	@NotNull(message = "xxx不能为空",groups = {Validator.save.class,Validator.update.class})
//	@ApiModelProperty("demo")
//	private Long demo;
	
	// fields start
	/**
	 * 用户类型，字典USERTYPE 1管理员，2普通用户，9其他
	 */
	@ApiModelProperty(value="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",notes="字符长度为：10")
	private Integer userType;
	/**
	 * 登录帐号
	 */
	@ApiModelProperty(value="登录帐号",notes="字符长度为：250")
	private String username;
	/**
	 * 用户密码
	 */
	@ApiModelProperty(value="用户密码",notes="字符长度为：250")
	private String pwdText;
	/**
	 * 密码加密盐
	 */
	@ApiModelProperty(value="密码加密盐",notes="字符长度为：50")
	private String pwdSalt;
	/**
	 * 真实姓名
	 */
	@ApiModelProperty(value="真实姓名",notes="字符长度为：50")
	private String realName;
	/**
	 * 别名
	 */
	@ApiModelProperty(value="别名",notes="字符长度为：50")
	private String aliasName;
	/**
	 * 头像
	 */
	@ApiModelProperty(value="头像",notes="字符长度为：500")
	private String portrait;
	/**
	 * 生日
	 */
	@ApiModelProperty(value="生日",notes="字符长度为：10")
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	/**
	 * 性别，字典SEX 1女，2男
	 */
	@ApiModelProperty(value="性别，字典SEX 1女，2男",notes="字符长度为：10")
	private Integer sex;
	/**
	 * 邮箱
	 */
	@ApiModelProperty(value="邮箱",notes="字符长度为：250")
	private String email;
	/**
	 * 联系qq
	 */
	@ApiModelProperty(value="联系qq",notes="字符长度为：50")
	private String qq;
	/**
	 * 联系电话
	 */
	@ApiModelProperty(value="联系电话",notes="字符长度为：30")
	private String tel;
	/**
	 * 密保问题
	 */
	@ApiModelProperty(value="密保问题",notes="字符长度为：250")
	private String securityQuestion;
	/**
	 * MFA设备标识
	 */
	@ApiModelProperty(value="MFA设备标识",notes="字符长度为：100")
	private String sucurityMfa;
	/**
	 * 上次登录时间
	 */
	@ApiModelProperty(value="上次登录时间",notes="字符长度为：26")
	private Long lastLoginTime;
	/**
	 * 上次登录ip
	 */
	@ApiModelProperty(value="上次登录ip",notes="字符长度为：30")
	private String lastLoginIp;
	/**
	 * 用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
	 */
	@ApiModelProperty(value="用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定",notes="字符长度为：10")
	private Integer status;
	/**
	 * 锁定时间，锁定时间后才能继续登录
	 */
	@ApiModelProperty(value="锁定时间，锁定时间后才能继续登录",notes="字符长度为：26")
	private Long lockTime;
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
