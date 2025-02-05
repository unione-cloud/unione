package com.unione.cloud.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="用户注册")
public class UserRegister {

	@Schema(title="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",description="字符长度为：10")
	private Integer userType;
	
	@Schema(title="登录帐号",description="字符长度为：250")
	private String username;
	
	@Schema(title="用户密码",description="字符长度为：250")
	private String pwdText;
	
	@Schema(title="真实姓名",description="字符长度为：50")
	private String realName;
	
	@Schema(title="别名",description="字符长度为：50")
	private String aliasName;
	
	@Schema(title="头像",description="字符长度为：500")
	private String portrait;
	
	@Schema(title="联系电话",description="字符长度为：30")
	private String tel;
	
	@Schema(title="验证码")
	private String captcha;
	
}
