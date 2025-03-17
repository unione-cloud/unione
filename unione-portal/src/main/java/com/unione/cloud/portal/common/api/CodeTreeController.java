package com.unione.cloud.portal.common.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.portal.system.dto.CodeLvsnParam;
import com.unione.cloud.portal.system.service.CodeTreeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "公共服务：层级树")
@RequestMapping("/api/code/tree")	
public class CodeTreeController {

	@Autowired
	private CodeTreeService codeTreeService;
	

	@PostMapping("/generate")
	@Action(title="生成层级编码",type = ActionType.Query,nolog = true)
	public Results<String> generate(@RequestBody CodeLvsnParam param){
		String lvsn = codeTreeService.generate(param.getSn(), param.getParent(), param.getLv());
		return Results.success(lvsn);
	}

}
