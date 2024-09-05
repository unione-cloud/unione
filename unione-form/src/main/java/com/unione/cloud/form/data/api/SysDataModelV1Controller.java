package com.unione.cloud.form.data.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.form.data.storage.model.DataCommit;
import com.unione.cloud.form.data.storage.model.DataFind;
import com.unione.cloud.form.data.storage.model.DataResult;

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
	public DataResult<List<Map<String, Object>>> find(@PathVariable("psn") String psn, @RequestBody DataFind params){
		
		
		return DataResult.failure();
	}
	
	
	/**
	 * 数据保存接口
	 * @param params
	 * @return
	 */
	@PostMapping("/{psn}/save")
    @ApiOperation(value = "数据保存",notes = "有主键进行更新，主键为空则新增")
	public DataResult<Map<String, Object>> save(@PathVariable("psn") String psn, @RequestBody DataCommit commit){
		
		
		
		
		return DataResult.failure();
	}
	
	
	
	@PostMapping("/{psn}/detail")
	@ApiOperation(value = "数据详情",notes = "根据主键加载数据对象")
	public DataResult<Map<String, Object>> detail(@PathVariable("psn") String psn, @RequestBody Long id) {
		
		return DataResult.failure();
	}
	
	
	
	
	@PostMapping("/{psn}/loadByIds")
    @ApiOperation(value = "数据列表",notes = "根据主键集合加载数据列表")
	public DataResult<List<Map<String, Object>>> loadByIds(@PathVariable("psn") String psn, @RequestBody Set<Long> ids) {
		
		
		return DataResult.failure();
	}
	
	
	
	
	@PostMapping("/{psn}/delete")
    @ApiOperation(value = "删除数据",notes = "根据主键集合删除数据")
	public DataResult<Integer> delete(@PathVariable("psn") String psn, @RequestBody Set<Long> ids){
		
		
		
		return DataResult.failure();
	}
	
	
	
	
	
	
	

}
