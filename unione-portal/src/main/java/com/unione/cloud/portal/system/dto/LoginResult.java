package com.unione.cloud.portal.system.dto;

import com.unione.cloud.core.security.UserPrincipal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("认证结果")
public class LoginResult {
	
	@ApiModelProperty("令牌")
	private String token;
	
	@ApiModelProperty("凭证对象")
	private UserPrincipal principal;
	
	@ApiModelProperty("结果")
	private boolean success;
	
	@ApiModelProperty("消息")
	private String message;
	
	private LoginResult() {}
	
	
	/**
	 * 	登录失败
	 * @return
	 */
	public static LoginResult fail() {
		return fail("登录失败");
	}
	
	/**
	 * 	登录失败
	 * @param message
	 * @return
	 */
	public static LoginResult fail(String message) {
		LoginResult result=new LoginResult();
		result.setMessage(message);
		return result;
	}

	/**
	 *	 登录成功
	 * @param token
	 * @param principal
	 * @return
	 */
	public static LoginResult success(String token,UserPrincipal principal) {
		LoginResult result=new LoginResult();
		result.setSuccess(true);
		result.setToken(token);
		result.setPrincipal(principal);
		result.setMessage("登录成功");
		return result;
	}
	
	
	
}
