package com.unione.cloud.core.feign.api;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignFindById<T> {

	@PostMapping("/findByIds")
	@ApiOperation(value = "查询列表", notes = "通过sid查询列表")
	public Results<List<T>> findByIds(@RequestBody Set<Long> ids);
	
}
