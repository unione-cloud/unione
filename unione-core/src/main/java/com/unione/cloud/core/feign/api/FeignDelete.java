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
	@Operation(summary = "删除",description = "根据id删除数据")
	public Results<Integer> delete(@RequestBody Set<Long> ids);
	
	
}
