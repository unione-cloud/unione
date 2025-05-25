package com.unione.cloud.system.dto;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.UserPrincipal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="认证结果")
public class LoginResult extends Results<Void>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2586507538432233973L;

	@Schema(title="令牌")
	private String token;
	
	@Schema(title="凭证对象")
	private UserPrincipal principal;
	
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
		result.setCode(500);
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
		result.setCode(200);
		result.setSuccess(true);
		result.setToken(token);
		result.setPrincipal(principal);
		result.setMessage("登录成功");
		return result;
	}
	
	
	
}
