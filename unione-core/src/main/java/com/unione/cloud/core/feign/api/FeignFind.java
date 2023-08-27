package com.unione.cloud.core.feign.api;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignFind<T> {

	/**
	 * Feign 查询接口
	 * @param params
	 * @return
	 */
	@PostMapping("/find")
    @ApiOperation(value = "查询")
	public Results<List<T>> find(@RequestBody Params<T> params);
	
}
