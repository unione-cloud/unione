package com.unione.cloud.security.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.security.service.CaptchaService;
import com.unione.cloud.security.service.LoginService;
import com.unione.cloud.system.dto.LoginParam;
import com.unione.cloud.system.dto.LoginResult;
import com.unione.cloud.system.dto.UserRegister;
import com.unione.cloud.system.service.RegisterService;
import com.unione.cloud.ums.dto.SmsCaptcha;
import com.unione.cloud.ums.service.UmsSmsService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.captcha.AbstractCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "安全服务：安全认证")
@RequestMapping("/api/security")
public class SecurityController {
	
	@Autowired
	private HttpServletResponse response;

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private UmsSmsService umsSmsService;
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private RegisterService registerService;
	
	
	@Autowired
	private SessionService sessionService;	
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private SecretService secretService;

	/**
	 * 发送短信验证码
	 */
	@PostMapping("/captcha/sms")
	@Operation(summary="发送短信验证码",description="")
	public Results<Long> captchaSms(@RequestBody SmsCaptcha captcha){
		log.debug("进入->发送短信验证码方法");
		AssertUtil.service()
			.notNull(captcha, new String[] {"tel","scene"},"请求参数%s不能为空")
			.notIn(captcha.getScene(), Arrays.asList("login","register"), "场景编码只能是login或register");
		
		if(umsSmsService.enableCaptcha(captcha.getScene(), captcha.getTel())){
			return Results.success();
		}

		Results<Long> results = umsSmsService.sendCaptcha(captcha);
		
		log.debug("退出->发送短信验证码方法");
		return results;
	}
	
	/**
	 * 生成验证码图片
	 */
	@GetMapping("/captcha/image")
	@Operation(summary="生成验证码图片",description="生成验证码并返回验证码图片")
	public void captchaImage(){
		log.debug("进入->生成验证码图片控制器");
		response.setContentType("image/jpeg");
		// 不缓存此内容
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expire", 0);
		
		try {
			AbstractCaptcha captcha=captchaService.create();
			captcha.write(response.getOutputStream());
		} catch (Exception e) {
			log.error("验证码生成失败",e);
		}
		
		log.debug("退出->生成验证码图片控制器");
	}
	
	
	
	@PostMapping("/login/uname")
	@Action(title="用户登录:帐号密码",type = ActionType.Login)
	@Operation(summary="用户登录:帐号密码",description="")
	public LoginResult login(@RequestBody LoginParam param) {
		log.info("用户登录，usrename:{}",param.getUsername());
		LogsUtil.setUsername(param.getUsername());
		AssertUtil.service()
			.notNull(param, new String[] {"username","password"},"请求参数%s不能为空");
		// 密码解密处理
		param.setPassword(secretService.decrypt(param.getPassword()));
		
		// 执行登录
		LoginResult result = loginService.doLoginByUname(param);
		
		return result;
	}

	@PostMapping("/login/sms")
	@Action(title="用户登录:短信验证码",type = ActionType.Login)
	@Operation(summary="用户登录:短信验证码",description="")
	public LoginResult loginBySms(@RequestBody LoginParam param) {
		log.info("用户登录，usrename:{}",param.getUsername());
		LogsUtil.setUsername(param.getUserphone());
		AssertUtil.service()
			.notNull(param, new String[] {"userphone","captcha"},"请求参数%s不能为空");

		// 执行登录
		LoginResult result = loginService.doLoginBySms(param);
		
		return result;
	}


	@PostMapping("/oauth/{type}")
	@Action(title="用户登录:OAuth2.0",type = ActionType.Login)
	@Operation(summary="用户登录:OAuth2.0",description="")
	public LoginResult oauth(@PathVariable("type") String type,@RequestParam("code") String code) {
		log.info("用户登录，type:{},code:{}",type,code);
		
		// 执行登录
		LoginResult result = loginService.doLoginByOAuth(type, code);
		
		return result;
	}

	/**
	 * 登录回调
	 */
	@Action(title="登录回调",type = ActionType.Login)
	@Operation(summary="登录回调",description="")
	@RequestMapping(value="/notice/{type}",method = {RequestMethod.POST,RequestMethod.GET})
	public void notice(@PathVariable("type") String type){
		// 获取请求参数
		String code = request.getParameter("code");

		// 待实现
		// TODO

	}
	
	@PostMapping("/logout")
	@Action(title="用户注销",type = ActionType.Logout)
	@Operation(summary="用户注销",description="")
	public Results<Void> logout(){
		log.info("用户注销，usrename:{}",sessionService.getUsername());
		
		tokenService.clean4auth(sessionService.getToken());
		
		return Results.success();
	}
	
	
	@GetMapping("/isAuthed")
	@Operation(summary="令牌验证",description="验证是否登录，token是否有效")
	public LoginResult isAuthed() {
		UserPrincipal principal=sessionService.getPrincipal();
		if(principal!=null) {
			return LoginResult.success(sessionService.getToken(),principal);
		}
		return LoginResult.fail("当前账号未认证");
	}
	
	
	@PostMapping("/register")
	@Action(title="用户注册",type = ActionType.Register)
	@Operation(summary= "用户注册",description = "需要开启注册功能并设置默认分配角色等信息")
	public Results<Void> register(@RequestBody UserRegister param){
		log.info("用户注册：usrename:{}",param.getUsername());
		LogsUtil.setUsername(param.getUsername());
		
		Results<Void> result=registerService.doRegister(param);
		
		return result;
	}
	
	
	
	

}
