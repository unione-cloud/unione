package com.unione.cloud.form.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.data.dto.DataConvertOption;
import com.unione.cloud.form.data.dto.DataConvertRequest;
import com.unione.cloud.form.data.model.SysDataConvertor;
import com.unione.cloud.form.data.service.DataConvertorService;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataConvertor Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:03:41
 * @版本	1.0.0
 **/
@Slf4j
@RefreshScope
@RestController
@Tag(name = "系统管理：数据转换器",description="SysDataConvertor")
@RequestMapping("/api/data/convertor")	 //TreeFeignApi
public class SysDataConvertorController implements PojoFeignApi<SysDataConvertor>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private DataConvertorService dataConvertorService;
	
	@Value("${unione.form.page.default.appid:1000}")
	private Long DEFAULT_APP_ID;
	
	
	@PostMapping("/load/{id}")
	@Operation(description ="加载转换数据")
	public Results<List<DataConvertOption>> load(@PathVariable("id") Long id, @RequestBody Params<DataConvertRequest> params){
		return dataConvertorService.load(id,params);
	}
	
	
	@Override
	public Results<List<SysDataConvertor>> find(Params<SysDataConvertor> params) {
		log.debug("进入:查询数据转换器列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询数据转换器列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysDataConvertor>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询数据转换器列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysDataConvertor entity) {
		log.debug("进入:新增数据转换器信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增数据转换器");
		
		entity.setDelFlag(0);
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "appId",DEFAULT_APP_ID);
		
		// 参数处理
		dataBaseDao.insert(entity);
		
		LogsUtil.success(entity.getId());
		log.debug("退出:新增数据转换器信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


//	@Override
//	public Results<Long> update(@Validated(Validator.update.class) SysDataConvertor entity) {
//		log.debug("进入:修改数据转换器信息方法，entity:{}",entity);
//		Results<Long> results = new Results<>();
//		LogsUtil.set(LogType.Update, "修改数据转换器",entity.getId());
//		
//		String[] fields = {"appId","dsId","title","types","dictName","options","search","url","tableName","tableField","tableWhere","tableOrder","idField","pidField","valueField","labelField","showLevel","status","isAsync","isPaging"};
//		SqlBuilder<SysDataConvertor> sqlBuilder=SqlBuilder.build(entity).field(fields);
//		int len = dataBaseDao.updateById(sqlBuilder);
//		LogsUtil.add("保存数据,len:"+len);
//		
//		results.setBody(entity.getId());
//		results.setSuccess(len>0);
//		results.setMessage(len>0?"操作成功":"操作失败");
//		LogsUtil.save(len>0, entity.getId());
//
//		log.debug("退出:修改数据转换器信息方法，entity:{},result:{}",entity,results.isSuccess());
//		return results;
//	}



	@Override
	public Results<List<SysDataConvertor>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询数据转换器信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询数据转换器");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysDataConvertor> rows = dataBaseDao.findByIds(SqlBuilder.build(SysDataConvertor.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询数据转换器信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysDataConvertor> detail(Long id) {
		log.debug("进入:查看数据转换器详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看数据转换器详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysDataConvertor tmp = dataBaseDao.findById(SqlBuilder.build(SysDataConvertor.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看数据转换器详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除数据转换器信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除数据转换器");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysDataConvertor.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除数据转换器信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}



}
