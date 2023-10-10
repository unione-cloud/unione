package com.unione.cloud.web.logs;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.web.logs.model.SysLogs;

@FeignClient(
	name = "${unione.cloud.portal:unione-portal}",
	contextId = "SysLogs",
	url = "${unione.cloud.ip:}${unione.cloud.portal:}",
	path = "/api/logs"
)
public interface LogsApi {

	@PostMapping("/save")
	public Results<Long> save(@RequestBody SysLogs entity);
	
}
