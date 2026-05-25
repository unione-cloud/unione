package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysAppInfo;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysAppInfo Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：应用管理",description="SysAppInfo")
@RequestMapping("/api/system/appInfo")	 //TreeFeignApi
public class SysAppInfoController implements PojoFeignApi<SysAppInfo>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	@PostMapping("/query")
	@Action(title = "查询应用:公开",type = ActionType.Query)
	@Operation(summary = "查询应用:公开",description = "查询应用")
	public Results<List<SysAppInfo>> query(@RequestBody Params<SysAppInfo> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		
		Results<List<SysAppInfo>> results = dataBaseDao.findPages(SqlBuilder.build(SysAppInfo.class,params)
			.where("category in ('component','service','platform') and category=? and status in (1,2,3) and status=? and trades=?")
			.dataPermis(PermisRule.ALL));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}

	@PostMapping("/load")
	@Action(title = "查询应用:列表",type = ActionType.Query,nolog = true)
	public Results<List<SysAppInfo>> load(@RequestBody Params<SysAppInfo> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		
		params.getBody().setTenantId(sessionService.getTenantId());
		Results<List<SysAppInfo>> results = dataBaseDao.findPages(SqlBuilder.build(params)
			.where("category=? and status in (1,2,3) and status=? and (isPlatform = 1 or tenantId=?)").dataPermis(PermisRule.ALL));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}
	
	@Override
	@Action(title = "查询应用:管理",type = ActionType.Query)
	public Results<List<SysAppInfo>> find(Params<SysAppInfo> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysAppInfo>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title = "保存应用",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) SysAppInfo entity) {
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "types","pc");
		BeanUtils.setDefaultValue(entity, "isPlatform",0);
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "isTmpl",0);

		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			SysAppInfo tmp = dataBaseDao.findById(SqlBuilder.build(SysAppInfo.class,entity.getId()));
			AssertUtil.service().notNull(tmp, "记录未找到");

			String[] fields = {"category","name","sn","isMp","url","welcome","versNo","versDesc","icon","picMax","picMid","picMix","ordered","trades","types","isPlatform","status","descs"};
			SqlBuilder<SysAppInfo> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置应用状态",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	@Operation(summary = "设置应用状态", description="APPSTATUS 1新建，2内测，3发布，4撤销")
	public Results<Void> setStatus(@RequestBody SysAppInfo entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3,4), "参数status取值范围[1,2,3,4]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<SysAppInfo>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysAppInfo> rows = dataBaseDao.findByIds(SqlBuilder.build(SysAppInfo.class,new ArrayList<>(ids)));
		return Results.success(rows);
	}


	@Override
	public Results<SysAppInfo> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysAppInfo tmp = dataBaseDao.findById(SqlBuilder.build(SysAppInfo.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		return Results.success(tmp);
	}
	

	@Override
	@Action(title = "删除应用",type = ActionType.Delete,roles = {UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysAppInfo.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		return results;
	}


}
