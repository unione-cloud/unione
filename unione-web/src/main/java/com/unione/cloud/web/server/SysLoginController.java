package com.unione.cloud.web.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.web.model.dto.LoginParam;
import com.unione.cloud.web.model.dto.LoginResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "基础服务：登录服务")
@RequestMapping("/api/login")
public class SysLoginController {
	
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
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
		
		
		log.debug("退出->生成验证码图片控制器");
	}
	
	
	@PostMapping("/username")
	@ApiOperation(value="帐号密码登录",notes="")
	public LoginResult username(@RequestBody LoginParam param) {
		log.info("用户登录：帐号密码登录，usrename:{}",param.getUsername());
		
		
		
		
		return null;
	}
	
	
	
	

}
