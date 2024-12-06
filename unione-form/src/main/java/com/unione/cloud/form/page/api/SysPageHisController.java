package com.unione.cloud.form.page.api;

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
import com.unione.cloud.form.page.model.SysPageHis;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysPageHis Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:44:00
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "页面管理：页面历史",description="SysPageHis")
@RequestMapping("/api/page/his")	 //TreeFeignApi
public class SysPageHisController implements FeignFind<SysPageHis>,FeignFindById<SysPageHis>,FeignDetail<SysPageHis> {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysPageHis>> find(Params<SysPageHis> params) {
		log.debug("进入:查询页面历史列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询页面历史列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysPageHis>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询页面历史列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}



	@Override
	public Results<List<SysPageHis>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询页面历史信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询页面历史");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysPageHis> rows = dataBaseDao.findByIds(SqlBuilder.build(SysPageHis.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询页面历史信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysPageHis> detail(Long id) {
		log.debug("进入:查看页面历史详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看页面历史详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysPageHis tmp = dataBaseDao.findById(SqlBuilder.build(SysPageHis.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看页面历史详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	



}
