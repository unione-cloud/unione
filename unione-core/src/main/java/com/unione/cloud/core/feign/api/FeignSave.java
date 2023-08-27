package com.unione.cloud.core.feign.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignSave<T> {

	/**
	 * Feign 保存接口
	 * @param entity
	 * @return
	 */
	@PostMapping("/save")
    @ApiOperation(value = "新增")
	public Results<Long> save(@RequestBody T entity);
	
}
