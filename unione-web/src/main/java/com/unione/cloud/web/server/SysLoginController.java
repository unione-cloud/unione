package com.unione.cloud.web.server;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.web.model.dto.LoginParam;
import com.unione.cloud.web.model.dto.LoginResult;
import com.unione.cloud.web.service.CaptchaService;
import com.unione.cloud.web.service.LoginService;
import com.unione.cloud.web.util.LogsUtil;
import com.unione.cloud.web.util.LogsUtil.LogType;

import cn.hutool.captcha.AbstractCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "基础服务：登录服务")
@RequestMapping("/api")
public class SysLoginController {
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private CaptchaService captchaService;
	
	@Autowired
	private LoginService loginService;
	
	
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
	
	
	
	

}
