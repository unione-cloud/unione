package com.unione.cloud.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name="Vali Captcha模型")
public class ValiCaptcha {
	
	@Schema(title="场景编码",required=true,description="")
	private String scene;

	@Schema(title="手机号",required=true)
	private String tel;
	
	@Schema(title="短信验证码",required=true)
	private String captcha;
	
}

