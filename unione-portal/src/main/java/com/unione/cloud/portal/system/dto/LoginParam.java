package com.unione.cloud.portal.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Schema("认证参数")
public class LoginParam {

	@Schema(title="帐号")
	private String username;
	
	@Schema(title="手机号")
	private String userphone;
	
	@Schema(title="密码")
	private String password;
	
	@Schema(title="验证码")
	private String captcha;
	
}
