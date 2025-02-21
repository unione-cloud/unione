package com.unione.cloud.core.feign.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;

public interface FeignSave<T> {

	/**
	 * Feign 保存接口
	 * @param entity
	 * @return
	 */
	@PostMapping("/save")
	@Operation(summary = "保存",description = "新增/更新")
	public Results<Long> save(@RequestBody T entity);
	
}
