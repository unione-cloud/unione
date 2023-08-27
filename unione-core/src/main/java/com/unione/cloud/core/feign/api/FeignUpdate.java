package com.unione.cloud.core.feign.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignUpdate<T> {
	
	/**
	 * Feign 修改接口
	 * @param entity
	 * @return
	 */
	@PostMapping("/update")
    @ApiOperation(value = "修改")
	public Results<Long> update(@RequestBody T entity);
	
}
