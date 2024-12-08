package com.unione.cloud.form.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.service.DataDefineService;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.security.UserFormRoles;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataDefine Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "数据管理：数据定义管理",description="SysDataModel")
@RequestMapping("/api/data/define")	 //TreeFeignApi
public class SysDataDefineController implements FeignDelete<SysDataDefine>,FeignFind<SysDataDefine>,FeignFindById<SysDataDefine>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DataDefineService dataDefineService;	
	
	
	@Override
	public Results<List<SysDataDefine>> find(Params<SysDataDefine> params) {
		log.debug("进入:查询数据定义管理列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询数据定义管理列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysDataDefine>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询数据定义管理列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}

	
	@PostMapping(value="/imp/from/db")
	@ApiOperation(value="从数据库导入",notes = "从数据库中选中表格导入，自动解析生成数据定义")
	public Results<String> impFromDb(@RequestParam(value="appId",required = false) Long appId,
			@RequestParam(value="dsId") Long dsId,
			@RequestParam(value="tables") List<String> tables,
			@RequestParam(value="force", required = false) boolean force){
		log.debug("进入：从数据库导入数据定义方法,ds id:{} tables:{}",dsId,tables);
		LogsUtil.set(LogType.Insert, "从数据库导入数据定义");		
		
		Results<String> result = dataDefineService.impFromDb(appId,dsId, tables,force);		
		
		LogsUtil.save(result.isSuccess());
		return result;
	}
	
	
	@PostMapping(value="/save")
	@ApiOperation(value="保存数据定义")
	public Results<DataDefine> save(@Validated(Validator.save.class) @RequestBody DataDefine dataDefine) {
		log.debug("进入:新增数据定义管理信息.dataDefine:{}",dataDefine);
		AssertUtil.service().isTrue(sessionService.hasRole(UserFormRoles.FORM_ADMIN,
				UserFormRoles.FORM_CONFIG,
				UserFormRoles.FORM_DEV), "当前帐号无权限");
		
		Results<DataDefine> result = dataDefineService.save(dataDefine);
		
		LogsUtil.save(result.isSuccess());
		return result;
	}


	@Override
	public Results<List<SysDataDefine>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询数据定义管理信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询数据定义管理");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysDataDefine> rows = dataBaseDao.findByIds(SqlBuilder.build(SysDataDefine.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询数据定义管理信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@PostMapping(value="/load/{sn}")
	@ApiOperation(value="加载数据定义")
	public Results<SysDataDefine> load(@PathVariable("sn") String sn) {
		log.debug("进入:加载数据定义方法，sn:{}",sn);
		LogsUtil.set(LogType.Query, "加载数据定义");
		// 参数处理
		AssertUtil.service().notNull(sn,"参数sn不能为空");
		LogsUtil.setExtData(sn);
		
		LogsUtil.add("查找记录");
		SysDataDefine param=SysDataDefine.builder().sn(sn).build();
		SysDataDefine tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		AssertUtil.service().notNull(tmp, "页面信息未找到","404");
		LogsUtil.setTarget(tmp.getId());
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:加载数据定义方法，sn:{},result:true",sn);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Set<Long> ids){
		log.debug("进入:删除数据定义管理信息方法，ids:{}",ids);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除数据定义管理");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(SysDataDefine.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody((long)count);
		LogsUtil.save(count>0);

		log.debug("退出:删除数据定义管理信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


}
