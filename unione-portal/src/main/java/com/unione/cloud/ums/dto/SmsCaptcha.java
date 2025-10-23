package com.unione.cloud.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name="Sms Captcha模型")
public class SmsCaptcha {
	
	@Schema(title="sms网关名称",description="如果为空则使用默认sms服务网关")
	private String gateway;
	
	@Schema(title="场景编码",required=true,description="")
	private String scene;
	
	@Schema(title="模板编码",description="如果该字段不为空则使用模板消息")
	private String tmpl;

	@Schema(title="手机号")
	private String tel;
	
}

