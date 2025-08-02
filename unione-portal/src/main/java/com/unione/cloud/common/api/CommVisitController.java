package com.unione.cloud.common.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.common.dto.CommVisitStatDto;
import com.unione.cloud.common.dto.VisitEntry;
import com.unione.cloud.common.service.VisitService;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	访问登记 服务
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "通用：访问登记服务")
@RequestMapping("/api/common/visit")
public class CommVisitController {
	
	@Autowired
	private VisitService visitService;

	@PostMapping("/entry")
	@Operation(summary = "访问",description = "记录页面访问信息")
	public Results<Void> entry(@RequestBody @Validated(Validator.save.class) VisitEntry entry) {
		log.debug("进入：页面访问登记接口");
		return visitService.entry(entry);
	}

	
	@PostMapping("/stat")
	@Operation(summary = "统计",description = "加载统计信息，参数：开始时间：timeBegin,截止时间：timeEnd,统计维度：dimensions")
	public Results<List<CommVisitStatDto>> stat(@RequestBody CommVisitStatDto dto) {
		return visitService.stat(dto);
	}


}
