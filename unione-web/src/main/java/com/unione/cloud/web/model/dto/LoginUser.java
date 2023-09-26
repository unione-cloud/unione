package com.unione.cloud.web.model.dto;

import java.util.Date;
import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="SYS_USER")
public class LoginUser extends Pojo{
	
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lockTime;
	/**
	 * 描述
	 */
	@ApiModelProperty(value="描述",notes="字符长度为：500")
	private String descs;
	// fields end

}
