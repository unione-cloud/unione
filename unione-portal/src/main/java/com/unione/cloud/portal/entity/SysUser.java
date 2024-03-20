package com.unione.cloud.portal.entity;
import org.beetl.sql.annotation.entity.*;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/*
* 系统管理：用户信息
* gen by unione cloud 2024-03-20
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user")
public class SysUser extends Pojo {
	/**
	* 用户类型，字典USERTYPE 1管理员，2普通用户，9其他
	*/
	@ApiModelProperty(value="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",notes="长度为：10")
	private Integer userType;
	/**
	* 登录帐号
	*/
	@ApiModelProperty(value="登录帐号",notes="长度为：100")
	private String username;
	/**
	* 用户密码
	*/
	@ApiModelProperty(value="用户密码",notes="长度为：200")
	private String pwdText;
	/**
	* 密码加密盐
	*/
	@ApiModelProperty(value="密码加密盐",notes="长度为：50")
	private String pwdSalt;
	/**
	* 真实姓名
	*/
	@ApiModelProperty(value="真实姓名",notes="长度为：50")
	private String realName;
	/**
	* 别名
	*/
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
	private String birthday;
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
	@ApiModelProperty(value="联系电话",notes="长度为：30")
	private String tel;
	/**
	* 密保问题
	*/
	@ApiModelProperty(value="密保问题",notes="长度为：200")
	private String securityQuestion;
	/**
	* MFA设备标识
	*/
	@ApiModelProperty(value="MFA设备标识",notes="长度为：100")
	private String sucurityMfa;
	/**
	* 上次登录时间
	*/
	@ApiModelProperty(value="上次登录时间",notes="长度为：19")
	private Long lastLoginTime;
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
	@ApiModelProperty(value="锁定时间，锁定时间后才能继续登录",notes="长度为：19")
	private Long lockTime;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：400")
	private String descs;

}
