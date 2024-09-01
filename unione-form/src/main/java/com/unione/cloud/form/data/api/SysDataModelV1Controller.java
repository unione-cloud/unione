package com.unione.cloud.form.data.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataModelV1 Controller 服务
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "系统管理：数据模型接口V1",description="SysDataModelV1")
@RequestMapping("/api/data/model/v1")	 //TreeFeignApi
public class SysDataModelV1Controller{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	
	/**
	 * 数据查询接口
	 * @param params
	 * @return
	 */
	@PostMapping("/{psn}/find")
    @ApiOperation(value = "数据查询")
	public Results<List<Map<String, Object>>> find(@PathVariable("psn") String psn, @RequestBody Params<Map<String, Object>> params){
		
		return Results.failure();
	}
	
	
	/**
	 * 数据保存接口
	 * @param params
	 * @return
	 */
	@PostMapping("/{psn}/save")
    @ApiOperation(value = "数据查询")
	public Results<List<Map<String, Object>>> save(@PathVariable("psn") String psn, @RequestBody Map<String, Object> params){
		
		return Results.failure();
	}
	
	
	

}
