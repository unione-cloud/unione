package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysAppInfo;
import com.unione.cloud.system.model.SysChangeLog;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysChangeLog Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 14:46:39
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：更新日志",description="SysChangeLog")
@RequestMapping("/api/system/sysChangeLog")	 //TreeFeignApi
public class SysChangeLogController implements PojoFeignApi<SysChangeLog>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询更新日志",type = ActionType.Query)
	public Results<List<SysChangeLog>> find(Params<SysChangeLog> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysChangeLog>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存更新日志",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) SysChangeLog entity) {
		// 参数处理
		int len = 0;
		BeanUtils.setDefaultValue(entity, "releaseTime", DateUtil.date());
		if(entity.getId()==null) {
			entity.setDelFlag(0);
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"versNo","versType","versDesc","changeType","changeTxt","upgradeTips","releaseTime","resLink"};
			SqlBuilder<SysChangeLog> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}

		// 同步版本信息到应用
		if(len>0){
			SysAppInfo appInfo=new SysAppInfo();
			appInfo.setId(entity.getAppId());
			appInfo.setVersDesc(entity.getVersDesc());
			appInfo.setVersNo(entity.getVersNo());
			String[] fields = {"versNo","versDesc"};
			SqlBuilder<SysAppInfo> sqlBuilder=SqlBuilder.build(appInfo).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}

		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载更新日志列表",type = ActionType.Query,nolog = true)
	public Results<List<SysChangeLog>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysChangeLog> rows = dataBaseDao.findByIds(SqlBuilder.build(SysChangeLog.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载更新日志详情",type = ActionType.Query,nolog = true)
	public Results<SysChangeLog> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysChangeLog tmp = dataBaseDao.findById(SqlBuilder.build(SysChangeLog.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除更新日志",type = ActionType.Delete,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysChangeLog.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载更新日志子级",type = ActionType.Query,nolog = true)
//	public Results<List<SysChangeLog>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		SysChangeLog params = new SysChangeLog();
//		params.setParentId(pid);
//		List<SysChangeLog> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
