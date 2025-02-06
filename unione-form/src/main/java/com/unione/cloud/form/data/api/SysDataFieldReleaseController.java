package com.unione.cloud.form.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.form.data.model.SysDataFieldRelease;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataFieldRelease Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-08 20:17:01
 * @版本	1.0.0
 **/
@Slf4j
@RestController("sysDataFieldReleaseController")
@Tag(name = "系统管理：数据字段发布",description="SysDataFieldRelease")
@RequestMapping("/api/data/field/release")	 //TreeFeignApi
public class SysDataFieldReleaseController implements FeignFind<SysDataFieldRelease>,FeignFindById<SysDataFieldRelease>,FeignDetail<SysDataFieldRelease>{
	
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
	

}
