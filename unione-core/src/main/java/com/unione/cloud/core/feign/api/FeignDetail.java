package com.unione.cloud.core.feign.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignDetail<T> {

	/**
	 * Feign 详情接口
	 * @param sid
	 * @return
	 */
	@PostMapping("/detail")
	@ApiOperation(value = "详情")
	public Results<T> detail(@RequestBody Long sid);
	
	
	
}
