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
import com.unione.cloud.form.data.model.SysDataDir;
import com.unione.cloud.form.data.model.SysDataPermis;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataPermis Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "SysDataPermis 数据权限管理：系统管理：数据权限",description="SysDataPermis")
@RequestMapping("/api/data/sysDataPermis")	 //TreeFeignApi
public class SysDataPermisController implements PojoFeignApi<SysDataPermis>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysDataPermis>> find(Params<SysDataPermis> params) {
		log.debug("进入:查询数据权限列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询数据权限列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysDataPermis>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询数据权限列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysDataPermis entity) {
		log.debug("进入:保存数据权限信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增数据权限");
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"modelId","name","needAuth","express","configs","descs","status","delFlag"};
			SqlBuilder<SysDataPermis> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		LogsUtil.save(len>0, entity.getId());
		log.debug("退出:保存数据权限信息.entity:{},result:true",entity);
		return Results.build(len>0, entity.getId());
	}




	@Override
	public Results<List<SysDataPermis>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询数据权限信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询数据权限");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysDataPermis> rows = dataBaseDao.findByIds(SqlBuilder.build(SysDataPermis.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询数据权限信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysDataPermis> detail(Long id) {
		log.debug("进入:查看数据权限详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看数据权限详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysDataPermis tmp = dataBaseDao.findById(SqlBuilder.build(SysDataPermis.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看数据权限详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除数据权限信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除数据权限");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysDataPermis.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除数据权限信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


}
