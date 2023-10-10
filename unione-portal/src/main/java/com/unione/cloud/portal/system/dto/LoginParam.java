package com.unione.cloud.portal.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("认证参数")
public class LoginParam {

	@ApiModelProperty("帐号")
	private String username;
	
	@ApiModelProperty("密码")
	private String password;
	
	@ApiModelProperty("验证码")
	private String captcha;
	
}
