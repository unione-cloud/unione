package com.unione.cloud.portal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.system.dto.SystemInfoDto;
import com.unione.cloud.system.service.SystemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "统一门户: 系统入口")
@RequestMapping("/api")
public class EntryController {

	@Autowired
	private SystemService systemService;
	
	@GetMapping("/entry")
	@Action(title="进入系统",type = ActionType.Query)
	@Operation(summary = "进入系统", description = "加载系统信息")
	public Results<SystemInfoDto> entry(){
		SystemInfoDto systemInfoDto = systemService.load();
		return Results.success(systemInfoDto);
	}
	
}
