package com.unione.cloud.form.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.form.data.model.SysDataFieldRelease;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataFieldRelease Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "SysDataFieldRelease sys_data_field_release管理：系统管理：数据字段发布",description="SysDataFieldRelease")
@RequestMapping("/api/data/sysDataFieldRelease")	 //TreeFeignApi
public class SysDataFieldReleaseController implements PojoFeignApi<SysDataFieldRelease>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysDataFieldRelease>> find(Params<SysDataFieldRelease> params) {
		log.debug("进入:查询sys_data_field_release列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询sys_data_field_release列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysDataFieldRelease>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询sys_data_field_release列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysDataFieldRelease entity) {
		log.debug("进入:新增sys_data_field_release信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增sys_data_field_release");
		// 参数处理
		dataBaseDao.insert(entity);
		
		LogsUtil.success(entity.getId());
		log.debug("退出:新增sys_data_field_release信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


	@Override
	public Results<Long> update(@Validated(Validator.update.class) SysDataFieldRelease entity) {
		log.debug("进入:修改sys_data_field_release信息方法，entity:{}",entity);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Modify, "修改sys_data_field_release",entity.getId());
		
		String[] fields = {"modelId","title","name","dataType","dataFormat","dataLen","dataPrec","isPk","isFk","fkType","fkTableId","fkTableName","fkFieldId","fkFieldName","fkLabelName","fkRefWay","isNull","isUnique","widgetName","isQuery","queryDefault","queryType","stsField","configs","needAuth","syncEnable","syncFlag","ordered","status","delFlag","descs"};
		SqlBuilder<SysDataFieldRelease> sqlBuilder=SqlBuilder.build(entity).field(fields);
		int len = dataBaseDao.updateById(sqlBuilder);
		LogsUtil.add("保存数据,len:"+len);
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		LogsUtil.save(len>0, entity.getId());

		log.debug("退出:修改sys_data_field_release信息方法，entity:{},result:{}",entity,results.isSuccess());
		return results;
	}



	@Override
	public Results<List<SysDataFieldRelease>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询sys_data_field_release信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询sys_data_field_release");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysDataFieldRelease> rows = dataBaseDao.findByIds(SqlBuilder.build(SysDataFieldRelease.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询sys_data_field_release信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysDataFieldRelease> detail(Long id) {
		log.debug("进入:查看sys_data_field_release详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看sys_data_field_release详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysDataFieldRelease tmp = dataBaseDao.findById(SqlBuilder.build(SysDataFieldRelease.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看sys_data_field_release详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Set<Long> ids){
		log.debug("进入:删除sys_data_field_release信息方法，ids:{}",ids);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除sys_data_field_release");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(SysDataFieldRelease.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody((long)count);
		LogsUtil.save(count>0);

		log.debug("退出:删除sys_data_field_release信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


//	@Override
//	public Results<List<SysDataFieldRelease>> children(Long sid){
//		log.debug("进入:加载下级sys_data_field_release信息,sid:{}",sid);
//		LogsUtil.set(LogType.Query, "加载下级sys_data_field_release信息",sid);
//		 //参数处理
//		AssertUtil.service().notNull(sid, "参数sid不能为空");
//		
//		// 执行查询
//		SysDataFieldRelease params = new SysDataFieldRelease();
//		params.setParentId(sid);
//		LogsUtil.add("parentId:%s",sid);
//	
//		List<SysDataFieldRelease> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		LogsUtil.add("下级sys_data_field_release记录数量:"+rows.size());
//		
//		LogsUtil.success();
//		log.debug("退出:加载下级sys_data_field_release信息,sid:{},result:true",sid);
//		return Results.success(rows);
//	}

}
