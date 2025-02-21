package com.unione.cloud.core.feign.api;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;

public interface FeignFindById<T> {

	@PostMapping("/findByIds")
	@Operation(summary = "加载列表",description = "根据id集合加载列表")
	public Results<List<T>> findByIds(@RequestBody Set<Long> ids);
	
}
