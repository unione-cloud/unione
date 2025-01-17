package com.unione.cloud.core.feign.api;

import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;

public interface FeignDelete<T> {

	/**
	 * Feign 删除接口
	 * @param params
	 * @return
	 */
	@PostMapping("/delete")
	@Operation(description = "删除")
	public Results<Integer> delete(@RequestBody Set<Long> ids);
	
	
}
