package com.unione.cloud.portal.security.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RefreshScope
@RestController
@Api(tags = "安全服务：密码加解密")
@RequestMapping("/api/security/secret")
public class SecretController {

	@Autowired
	private SecretService secretService;
	
	@Autowired
	private SessionService sessionService;
	
	/**
	 * 	解密权限配置，允许的用户列表
	 */
	@Value("${security.secret.decrypt.user.names:}")
	private List<String> ALLOW_USER_NAMES=new ArrayList<>();
	/**
	 * 	解密权限配置，允许的角色列表
	 */
	@Value("${security.secret.decrypt.role.codes:}")
	private List<String> ALLOW_ROLE_CODES=new ArrayList<>();
	
	/**
	 * 	数据加密
	 * @param data
	 * @return
	 */
	@PostMapping("/encrypt")
	@ApiOperation("数据加密")
	public Results<String> encrypt(@RequestBody String data) {
		return Results.success(secretService.encrypt(data));
	}
	
	/**
	 * 	数据加密【批量】
	 * @param data
	 * @return
	 */
	@PostMapping("/encrypts")
	@ApiOperation(value="数据加密【批量】",notes = "批量加密")
	public Results<Map<String, String>> encrypts(@RequestBody List<String> data) {
		Map<String, String> map=new HashMap<>();
		data.stream().forEach(str->{
			map.put(str, secretService.encrypt(str));
		});
		return Results.success(map);
	}
	
	
	/**
	 * 	数据解密
	 * @param data
	 * @return
	 */
	@PostMapping("/decrypt")
	@ApiOperation("数据解密")
	public Results<String> decrypt(@RequestBody String data) {
		log.info("进入：数据解密方法,user id:{},name:{},data:{}",sessionService.getUserId(),sessionService.getUsername(),data);
		LogsUtil.set(LogType.Sensitive, "数据解密");
		LogsUtil.add("数据解密,user id:%s,name:%s,data:%s",sessionService.getUserId(),sessionService.getUsername(),data);
		
		// 权限验证
		if(!ALLOW_USER_NAMES.contains(sessionService.getUsername()) && 
			CollectionUtil.intersection(ALLOW_ROLE_CODES, sessionService.getUserRoles()).isEmpty()){
			LogsUtil.failure("401", "当前帐号无权限");
			return Results.error("当前帐号无权限");
		}
		
		// 数据解密
		Results<String> resule =Results.success(secretService.decrypt(data)); 
		
		LogsUtil.save(resule.isSuccess());
		return resule;
	}
	
	
	
	/**
	 * 哈希计算
	 * @param data
	 * @return
	 */
	@PostMapping("/hash")
	@ApiOperation("哈希计算")
	public Results<String> hash(@RequestBody String data) {
		return Results.success(secretService.hash(data));
	}
	
	
	
}
