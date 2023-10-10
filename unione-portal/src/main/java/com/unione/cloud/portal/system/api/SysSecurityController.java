package com.unione.cloud.portal.system.api;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.portal.system.dto.LoginParam;
import com.unione.cloud.portal.system.dto.LoginResult;
import com.unione.cloud.portal.system.dto.UserRegister;
import com.unione.cloud.portal.system.service.CaptchaService;
import com.unione.cloud.portal.system.service.LoginService;
import com.unione.cloud.portal.system.service.RegisterService;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.captcha.AbstractCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "基础服务：安全服务")
@RequestMapping("/security")
public class SysSecurityController {
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private CaptchaService captchaService;
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private RegisterService registerService;
	
	
	@Autowired
	private SessionService sessionService;	
	
	@Autowired
	private TokenService tokenService;
	
	/**
	 * 生成验证码图片
	 */
	@GetMapping("/captcha")
	@ApiOperation(value="生成验证码图片",notes="生成验证码并返回验证码图片")
	public void captcha(){
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
	
	
	
	@PostMapping("/login")
	@ApiOperation(value="用户登录",notes="")
	public LoginResult login(@RequestBody LoginParam param) {
		log.info("用户登录，usrename:{}",param.getUsername());
		LogsUtil.set(LogType.Login, "用户登录");
		LogsUtil.setCreator(param.getUsername());
		
		// 执行登录
		LoginResult result = loginService.doLogin(param);
		
		LogsUtil.save(result.isSuccess());
		return result;
	}
	
	@PostMapping("/logout")
	@ApiOperation(value="用户注销",notes="")
	public Results<Void> logout(){
		log.info("用户注销，usrename:{}",sessionService.getUsername());
		LogsUtil.set(LogType.Logout, "用户注销");
		
		tokenService.clean4auth(sessionService.getToken());
		
		LogsUtil.success();
		return Results.success();
	}
	
	
	@GetMapping("/isAuthed")
	@ApiOperation(value="令牌验证",notes="验证是否登录，token是否有效")
	public LoginResult isAuthed() {
		UserPrincipal principal=sessionService.getPrincipal();
		if(principal!=null) {
			return LoginResult.success(sessionService.getToken(),principal);
		}
		return LoginResult.fail("当前账号未认证");
	}
	
	
	@PostMapping("/register")
	@ApiOperation(value = "用户注册",notes = "需要开启注册功能并设置默认分配角色等信息")
	public Results<Void> register(@RequestBody UserRegister param){
		log.info("用户注册：usrename:{}",param.getUsername());
		LogsUtil.set(LogType.Register, "用户注册");
		LogsUtil.setCreator(param.getUsername());
		
		Results<Void> result=registerService.doRegister(param);
		
		LogsUtil.save(result.isSuccess());
		return result;
	}
	
	
	
	

}
