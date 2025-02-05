package com.unione.cloud.portal.system.api;

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
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.portal.system.model.SysGroup;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysGroup Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：分组管理，分组对应的行政区划信息直接存储在数据权限：行政区划权限表中。一个分组可以有多个行政区划，但都是当前用户所",description="SysGroup")
@RequestMapping("/api/system/group")	 //TreeFeignApi
public class SysGroupController implements TreeFeignApi<SysGroup>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysGroup>> find(Params<SysGroup> params) {
		log.debug("进入:查询分组列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询分组列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysGroup>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询分组列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysGroup entity) {
		log.debug("进入:保存分组.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "保存分组");
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"parentId","name","sn","level","types","iconFont","iconPic","descs","isLeaf","ordered","status"};
			SqlBuilder<SysGroup> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		LogsUtil.save(len>0, entity.getId());
		log.debug("退出:保存分组.entity:{},result:true",entity);
		return Results.build(len>0, entity.getId());
	}



	@Override
	public Results<List<SysGroup>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询分组信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询分组");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysGroup> rows = dataBaseDao.findByIds(SqlBuilder.build(SysGroup.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询分组信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysGroup> detail(Long id) {
		log.debug("进入:查看分组详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看分组详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysGroup tmp = dataBaseDao.findById(SqlBuilder.build(SysGroup.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看分组详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除分组信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除分组");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysGroup.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除分组信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


	@Override
	public Results<List<SysGroup>> children(Long sid){
		log.debug("进入:加载下级分组信息,sid:{}",sid);
		LogsUtil.set(LogType.Query, "加载下级分组信息",sid);
		 //参数处理
		AssertUtil.service().notNull(sid, "参数sid不能为空");
		
		// 执行查询
		SysGroup params = new SysGroup();
		params.setParentId(sid);
		LogsUtil.add("parentId:%s",sid);
	
		List<SysGroup> rows = dataBaseDao.findList(SqlBuilder.build(params));
		LogsUtil.add("下级分组记录数量:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:加载下级分组信息,sid:{},result:true",sid);
		return Results.success(rows);
	}

}
