package com.unione.cloud.web.common;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.web.common.dto.CodeLvsnParam;

@FeignClient(
	name = "${unione.name.portal:unione-portal}",
	contextId = "CodeTreeApi",
	url = "${unione.cloud.ip:}${unione.cloud.portal:}",
	path = "/api/common/code/tree"
)
public interface CodeTreeApi {

	@PostMapping("/generate")
	public Results<String> generate(@RequestBody CodeLvsnParam param);
	
}
