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
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.portal.system.model.SysGroupMember;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysGroupMember Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 21:18:02
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：分组成员",description="SysGroupMember")
@RequestMapping("/api/system/groupMember")	 //TreeFeignApi
public class SysGroupMemberController implements PojoFeignApi<SysGroupMember>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysGroupMember>> find(Params<SysGroupMember> params) {
		log.debug("进入:查询分组成员列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询分组成员列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysGroupMember>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询分组成员列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysGroupMember entity) {
		log.debug("进入:保存分组成员.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "保存分组成员");
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"groupId","mbType","mbId","orgId","orgName","name","sn","timeJoin","timeLeave","status","ordered","descs"};
			SqlBuilder<SysGroupMember> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		LogsUtil.save(len>0, entity.getId());
		log.debug("退出:保存分组成员.entity:{},result:true",entity);
		return Results.build(len>0, entity.getId());
	}



	@Override
	public Results<List<SysGroupMember>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询分组成员信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询分组成员");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysGroupMember> rows = dataBaseDao.findByIds(SqlBuilder.build(SysGroupMember.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询分组成员信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysGroupMember> detail(Long id) {
		log.debug("进入:查看分组成员详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看分组成员详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysGroupMember tmp = dataBaseDao.findById(SqlBuilder.build(SysGroupMember.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看分组成员详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除分组成员信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除分组成员");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysGroupMember.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除分组成员信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}



}
