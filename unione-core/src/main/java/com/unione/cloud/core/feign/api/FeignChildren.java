package com.unione.cloud.core.feign.api;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;

public interface FeignChildren<T> {

	/**
	 * Feign 子级接口
	 * @param sid
	 * @return
	 */
	@PostMapping("/children")
    @Operation(summary = "子节点",description = "根据parentId加载子节点列表")
	public Results<List<T>> children(@RequestBody Long sid);
	
	
}
