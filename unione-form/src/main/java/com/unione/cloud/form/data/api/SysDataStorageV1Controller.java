package com.unione.cloud.form.data.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.form.cache.DataDefineCache;
import com.unione.cloud.form.data.storage.DataStorageService;
import com.unione.cloud.form.data.storage.model.DataCommit;
import com.unione.cloud.form.data.storage.model.DataDelete;
import com.unione.cloud.form.data.storage.model.DataFind;
import com.unione.cloud.form.data.storage.model.DataLoad;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.data.storage.model.DataResult;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 SysDataStorage V1 Controller 服务
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "系统管理：数据存储接口V1",description="SysDataModelV1")
@RequestMapping("/api/data/storage/v1")
public class SysDataStorageV1Controller{
	
	
	@Autowired
	private DataDefineCache dataModelCache;
	
	
	@Autowired
	private DataStorageService dataStorageService;
	
	
	@PostMapping("/find")
    @ApiOperation(value = "数据查询")
	public DataResult<List<Map<String, Object>>> find(@RequestBody DataFind dataFind){
		log.debug("进入：数据存储/数据查询接口,dsn:{},find:{}",dataFind.getDsn(),dataFind);
		LogsUtil.set(LogType.Query, "数据存储/数据查询");
		AssertUtil.service().notNull(dataFind, new String[] {"dsn"},"参数%s不能为空");	
		
		DataDefine dataModel=dataModelCache.load(dataFind.getDsn());
		LogsUtil.setTarget(dataModel.getId(),dataModel.getTitle());
		LogsUtil.setExtData(JSONUtil.toJsonStr(dataFind));
		
		// 加载列表数据
		DataResult<List<Map<String, Object>>> result = dataStorageService.findListPage(dataModel, dataFind);
		
		// 加载外键数据
		dataStorageService.loadFkeyEntrys(dataModel, result.getBody(),dataFind.getFields().toArray(new String[0]));
		
		LogsUtil.success();
		log.debug("退出：数据模型数据查询接口,dsn:{},find:{}",dataFind.getDsn(),dataFind);
		return result;
	}
	
	
	@PostMapping("/save")
    @ApiOperation(value = "数据保存",notes = "有主键进行更新，主键为空则新增")
	public DataResult<Map<String, Object>> save(@RequestBody DataCommit dataCommit){
		log.debug("进入：数据存储/数据保存接口,dsn:{},commit:{}",dataCommit.getDsn(),dataCommit);
		AssertUtil.service().notNull(dataCommit, new String[] {"dsn"},"参数%s不能为空");	
		
		DataDefine dataModel=dataModelCache.load(dataCommit.getDsn());
		LogsUtil.setTarget(dataModel.getId(),dataModel.getTitle());
		LogsUtil.setExtData(JSONUtil.toJsonStr(dataCommit));
		
		int len = -1;
		if(dataCommit.getId()!=null) {
			LogsUtil.set(LogType.Update, "数据存储/数据保存");
			len = dataStorageService.updateById(dataModel, dataCommit);
		}else {
			LogsUtil.set(LogType.Insert, "数据存储/数据保存");
			len = dataStorageService.insert(dataModel, dataCommit);
		}
		
		LogsUtil.save(len>0);
		return DataResult.build(len>0);
	}
	
	
	
	@PostMapping("/detail")
	@ApiOperation(value = "数据详情",notes = "根据主键加载数据对象")
	public DataResult<Map<String, Object>> detail(@RequestBody DataLoad dataLoad) {
		log.debug("进入：数据存储/数据详情接口,dsn:{},load:{}",dataLoad.getDsn(),dataLoad);
		LogsUtil.set(LogType.Query, "数据存储/数据详情");
		AssertUtil.service().notNull(dataLoad, new String[] {"dsn","id"},"参数%s不能为空");	
		
		DataDefine dataModel=dataModelCache.load(dataLoad.getDsn());
		LogsUtil.setTarget(dataModel.getId(),dataModel.getTitle());
		LogsUtil.setExtData(JSONUtil.toJsonStr(dataLoad));
		
		// 加载数据
		Map<String, Object> row = dataStorageService.findById(dataModel, dataLoad);
		
		//加载外键
		dataStorageService.loadFkeyEntrys(dataModel, Arrays.asList(row), dataLoad.getFields().toArray(new String[0]));
		
		LogsUtil.save(row!=null);
		return DataResult.build(row!=null, row);
	}
	
	
	
	
	@PostMapping("/loadByIds")
    @ApiOperation(value = "数据列表",notes = "根据主键集合加载数据列表")
	public DataResult<List<Map<String, Object>>> loadByIds(@RequestBody DataLoad dataLoad) {
		log.debug("进入：数据存储/数据详情接口,dsn:{},load:{}",dataLoad.getDsn(),dataLoad);
		LogsUtil.set(LogType.Query, "数据存储/数据列表");
		AssertUtil.service().notNull(dataLoad, new String[] {"dsn","ids"},"参数%s不能为空")
			.notEmpty(dataLoad.getIds(),"参数ids不能为空");
		
		
		DataDefine dataModel=dataModelCache.load(dataLoad.getDsn());
		LogsUtil.setTarget(dataModel.getId(),dataModel.getTitle());
		LogsUtil.setExtData(JSONUtil.toJsonStr(dataLoad));
		
		// 加载数据
		List<Map<String, Object>> rows = dataStorageService.findByIds(dataModel, dataLoad);
		
		//加载外键
		dataStorageService.loadFkeyEntrys(dataModel, rows, dataLoad.getFields().toArray(new String[0]));
		
		LogsUtil.success();
		return DataResult.success(rows);
	}
	
	
	
	@PostMapping("/delete")
    @ApiOperation(value = "删除数据",notes = "根据主键集合删除数据")
	public DataResult<Integer> delete(@RequestBody DataDelete dataDelete){
		log.debug("进入：数据存储/数据保存接口,dsn:{},dataDelete:{}",dataDelete.getDsn(),dataDelete);
		LogsUtil.set(LogType.Delete, "数据存储/数据删除");
		AssertUtil.service().notNull(dataDelete, new String[] {"dsn","ids"},"参数%s不能为空")
		.notEmpty(dataDelete.getIds(),"参数ids不能为空");
		
		DataDefine dataModel=dataModelCache.load(dataDelete.getDsn());
		LogsUtil.setTarget(dataModel.getId(),dataModel.getTitle());
		LogsUtil.setExtData(JSONUtil.toJsonStr(dataDelete.getIds()));
		
		int len = dataStorageService.deleteByIds(dataModel, dataDelete.getIds());
		
		LogsUtil.save(len>0);
		return DataResult.build(len>0);
	}
	
	
	
	
	
	
	

}
