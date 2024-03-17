package com.unione.cloud.portal.security.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.secret.SecretService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "安全服务：密码加解密")
@RequestMapping("/api/security/secret")
public class SecretController {

	@Autowired
	private SecretService secretService;
	
	
	/**
	 * 数据加密
	 * @param data
	 * @return
	 */
	@PostMapping("/encrypt")
	@ApiOperation("数据加密")
	public Results<String> encrypt(@RequestBody String data) {
		return Results.success(secretService.encrypt(data));
	}
	
	/**
	 * 数据加密【批量】
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
	 * 数据解密
	 * @param data
	 * @return
	 */
	@PostMapping("/decrypt")
	@ApiOperation("数据解密")
	public Results<String> decrypt(@RequestBody String data) {
		return Results.success(secretService.decrypt(data));
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
