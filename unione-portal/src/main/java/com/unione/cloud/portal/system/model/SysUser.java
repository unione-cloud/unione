package com.unione.cloud.portal.system.model;
import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore.QueryType;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysUser Entity
 * @描述	系统管理：用户信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 21:18:03
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysUser")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user")
public class SysUser extends Pojo {
	/**
	* 用户类型，字典USERTYPE 1管理员，2普通用户，9其他
	*/
	@NotNull(message = "用户类型不能为空",groups = {Validator.save.class,Validator.update.class})
	@ApiModelProperty(value="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",notes="长度为：10")
	private Integer userType;
	/**
	* 登录帐号
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="登录帐号",notes="长度为：100")
	@NotNull(message = "登录帐号不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "登录帐号不能为空",groups = {Validator.save.class})
	private String username;
	/**
	* 用户密码
	*/
	@JsonIgnore
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="用户密码",notes="长度为：200")
	@NotNull(message = "用户密码不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "用户密码不能为空",groups = {Validator.save.class})
	private String pwdText;
	/**
	* 密码加密盐
	*/
	@JsonIgnore
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="密码加密盐",notes="长度为：50")
	private String pwdSalt;
	/**
	* 真实姓名
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="真实姓名",notes="长度为：50")
	private String realName;
	/**
	* 别名
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="别名",notes="长度为：50")
	private String aliasName;
	/**
	* 头像
	*/
	@ApiModelProperty(value="头像",notes="长度为：300")
	private String avatar;
	/**
	* 生日，YYYY-MM-DD
	*/
	@ApiModelProperty(value="生日，YYYY-MM-DD",notes="长度为：10")
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	/**
	* 性别，字典SEX 1女，2男
	*/
	@ApiModelProperty(value="性别，字典SEX 1女，2男",notes="长度为：10")
	private Integer sex;
	/**
	* 邮箱
	*/
	@ApiModelProperty(value="邮箱",notes="长度为：200")
	private String email;
	/**
	* 联系qq
	*/
	@ApiModelProperty(value="联系qq",notes="长度为：50")
	private String qq;
	/**
	* 联系电话
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="联系电话",notes="长度为：30")
	private String tel;
	/**
	* 密保问题
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="密保问题",notes="长度为：200")
	private String securityQuestion;
	/**
	* MFA设备标识
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="MFA设备标识",notes="长度为：100")
	private String sucurityMfa;
	/**
	* 上次登录时间
	*/
	@ApiModelProperty(value="上次登录时间",notes="yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;
	/**
	* 上次登录ip
	*/
	@ApiModelProperty(value="上次登录ip",notes="长度为：30")
	private String lastLoginIp;
	/**
	* 累计成功登陆次数
	*/
	@ApiModelProperty(value="累计成功登陆次数",notes="长度为：19")
	private Long totalLoginSuccess;
	/**
	* 累计失败登陆次数
	*/
	@ApiModelProperty(value="累计失败登陆次数",notes="长度为：19")
	private Long totalLoginFailue;
	/**
	* 用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
	*/
	@ApiModelProperty(value="用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定",notes="长度为：10")
	private Integer status;
	/**
	* 审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过
	*/
	@ApiModelProperty(value="审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过",notes="长度为：10")
	private Integer auditSts;
	/**
	* 锁定时间，锁定时间后才能继续登录
	*/
	@ApiModelProperty(value="锁定时间，锁定时间后才能继续登录",notes="yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lockTime;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：400")
	private String descs;

}
