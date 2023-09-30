package com.unione.cloud.web.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户注册")
public class UserRegister {

	@ApiModelProperty(value="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",notes="字符长度为：10")
	private Integer userType;
	
	@ApiModelProperty(value="登录帐号",notes="字符长度为：250")
	private String username;
	
	@ApiModelProperty(value="用户密码",notes="字符长度为：250")
	private String pwdText;
	
	@ApiModelProperty(value="真实姓名",notes="字符长度为：50")
	private String realName;
	
	@ApiModelProperty(value="别名",notes="字符长度为：50")
	private String aliasName;
	
	@ApiModelProperty(value="头像",notes="字符长度为：500")
	private String portrait;
	
	@ApiModelProperty(value="联系电话",notes="字符长度为：30")
	private String tel;
	
	@ApiModelProperty("验证码")
	private String captcha;
	
}
