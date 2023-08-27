package com.unione.cloud.core.feign.api;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.ApiOperation;

public interface FeignDelete<T> {

	/**
	 * Feign 删除接口
	 * @param params
	 * @return
	 */
	@PostMapping("/delete")
	@ApiOperation(value = "删除", notes = "批量删除")
	public Results<Long> delete(@RequestBody Params<List<Long>> params);
	
	
}
