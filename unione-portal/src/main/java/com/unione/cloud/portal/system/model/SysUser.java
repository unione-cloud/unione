package com.unione.cloud.portal.system.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.beetsql.annotation.QueryIgnore.QueryType;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user")
@SqlResource("system.SysUser")
// @UniDataPermis(DataPermis.ORGANCODE)
public class SysUser extends Pojo {
	/**
	* 用户类型，字典USERTYPE 1管理员，2普通用户，9其他
	*/
	@NotNull(message = "用户类型不能为空",groups = {Validator.save.class,Validator.update.class})
	@Schema(title="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",description="长度为：10")
	private Integer userType;
	/**
	* 登录帐号
	*/
	@KeyWords
	@Schema(title="登录帐号",description="长度为：100")
	@NotNull(message = "登录帐号不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "登录帐号不能为空",groups = {Validator.save.class})
	private String username;
	/**
	* 用户密码
	*/
	@JsonProperty(access = Access.WRITE_ONLY)
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="用户密码",description="长度为：200")
	@NotBlank(message = "用户密码不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "用户密码不能为空",groups = {Validator.save.class})
	private String pwdText;
	/**
	* 密码加密盐
	*/
	@JsonIgnore
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="密码加密盐",description="长度为：50")
	private String pwdSalt;
	/**
	* 真实姓名
	*/
	@KeyWords
	@Schema(title="真实姓名",description="长度为：50")
	private String realName;
	/**
	* 别名
	*/
	@KeyWords
	@Schema(title="别名",description="长度为：50")
	private String aliasName;
	/**
	* 头像
	*/
	@Schema(title="头像",description="长度为：300")
	private String avatar;
	/**
	* 生日，YYYY-MM-DD
	*/
	@Schema(title="生日，YYYY-MM-DD",description="长度为：10")
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	/**
	* 性别，字典SEX 1女，2男
	*/
	@Schema(title="性别，字典SEX 1女，2男",description="长度为：10")
	private Integer sex;
	/**
	* 邮箱
	*/
	@Schema(title="邮箱",description="长度为：200")
	private String email;
	/**
	* 联系qq
	*/
	@Schema(title="联系qq",description="长度为：50")
	private String qq;
	/**
	* 联系电话
	*/
	@KeyWords
	@Schema(title="联系电话",description="长度为：30")
	private String tel;
	/**
	* 密保问题
	*/
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="密保问题",description="长度为：200")
	private String securityQuestion;
	/**
	* MFA设备标识
	*/
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="MFA设备标识",description="长度为：100")
	private String sucurityMfa;
	/**
	* 上次登录时间
	*/
	@Schema(title="上次登录时间",description="yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;
	/**
	* 上次登录ip
	*/
	@Schema(title="上次登录ip",description="长度为：30")
	private String lastLoginIp;
	/**
	* 累计成功登陆次数
	*/
	@Schema(title="累计成功登陆次数",description="长度为：19")
	private Long totalLoginSuccess;
	/**
	* 累计失败登陆次数
	*/
	@Schema(title="累计失败登陆次数",description="长度为：19")
	private Long totalLoginFailue;
	/**
	* 用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定
	*/
	@Schema(title="用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定",description="长度为：10")
	private Integer status;
	/**
	* 审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过
	*/
	@Schema(title="审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过",description="长度为：10")
	private Integer auditSts;
	/**
	* 锁定时间，锁定时间后才能继续登录
	*/
	@Schema(title="锁定时间，锁定时间后才能继续登录",description="yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lockTime;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：400")
	private String descs;
	
}
