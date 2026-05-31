package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.base.model.BaseDict;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.TreeNodeDto;
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
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.dto.SystemAppDto;
import com.unione.cloud.system.dto.SystemInfoDto;
import com.unione.cloud.system.model.SysResource;
import com.unione.cloud.system.model.SysSystem;
import com.unione.cloud.system.service.SystemService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysSystem Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2026-04-26 21:20:41
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：系统信息",description="SysSystem")
@RequestMapping("/api/system/sys")	 //TreeFeignApi
public class SysSystemController implements PojoFeignApi<SysSystem>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SystemService systemService;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="系统信息",type = ActionType.Query)
	public Results<List<SysSystem>> find(Params<SysSystem> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			params.getBody().setTenantId(sessionService.getTenantId());
		}
		Results<List<SysSystem>> results = dataBaseDao.findPages(SqlBuilder.build(params).dataPermis(PermisRule.ALL)
			.where("(isGlobal = 1 or isGlobal = 0 and tenantId=?) and types=? and status=? and delFlag = 0"));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存系统信息",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) SysSystem entity) {
		// 参数处理
		int len = 0;
		BeanUtils.setDefaultValue(entity, "delFlag",0);
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "types","pc");
		BeanUtils.setDefaultValue(entity, "isGlobal", 0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			entity.setIsGlobal(0);
		}

		SystemInfoDto info = SystemInfoDto.from(entity);
		entity.setConfigs(JsonUtil.toJson(info.getConfigs()));
		entity.setAppList(JsonUtil.toJson(info.getApps()));


		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
				SysSystem tmp = dataBaseDao.findById(SqlBuilder.build(SysSystem.class,entity.getId()).dataPermis(PermisRule.ALL));
				AssertUtil.service().notNull(tmp, "记录未找到或无权限")
					.isTrue(ObjectUtil.equal(tmp.getIsGlobal(), 0), "当前帐号无权修改全局数据")
					.isTrue(ObjectUtil.equal(tmp.getTenantId(), sessionService.getTenantId()), "记录未找到");
			}
			String[] fields = {"name","alias","types","ctx","logoLarge","logoSmall","themeName","secret","footer","configs","appList","navList","versNo","versDesc","ordered","isGlobal","status","descs"};
			SqlBuilder<SysSystem> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		if(len>0){
			systemService.clear(entity.getCtx());
		}
		return Results.build(len>0, entity.getId());
	}

	@PostMapping("/status")
	@Action(title="设置系统状态",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	@Operation(summary = "设置系统状态", description="SYSSTATUS 1新建，2内测，3发布，4撤销")
	public Results<Void> setStatus(@RequestBody SysSystem entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3,4), "参数status取值范围[1,2,3,4]");
		
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			SysSystem tmp = dataBaseDao.findById(SqlBuilder.build(SysSystem.class,entity.getId()).dataPermis(PermisRule.ALL));
			AssertUtil.service().notNull(tmp, "记录未找到或无权限")
				.isTrue(ObjectUtil.equal(tmp.getIsGlobal(), 0), "当前帐号无权修改全局数据")
				.isTrue(ObjectUtil.equal(tmp.getTenantId(), sessionService.getTenantId()), "记录未找到");
		}

		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载系统信息列表",type = ActionType.Query,nolog = true)
	public Results<List<SysSystem>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysSystem> rows = dataBaseDao.findByIds(SqlBuilder.build(SysSystem.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载系统信息详情",type = ActionType.Query,nolog = true)
	public Results<SysSystem> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysSystem tmp = dataBaseDao.findById(SqlBuilder.build(SysSystem.class,id).dataPermis(PermisRule.ALL));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除系统信息",type = ActionType.Delete,roles = {UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");

		List<SysSystem> rows = dataBaseDao.findByIds(SqlBuilder.build(SysSystem.class,new ArrayList<>(ids)).dataPermis(PermisRule.ALL));
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			for(SysSystem row:rows){
				if(ObjectUtil.equal(row.getIsGlobal(), 1)){
					return Results.error("当前帐号无权删除全局数据");
				}
				if(!ObjectUtil.equal(row.getTenantId(), sessionService.getTenantId())){
					return Results.error("记录未找到");
				}
			}
		}
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JsonUtil.toJson(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(SysSystem.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);

		if(count>0){
			rows.stream().forEach(row->{
				systemService.clear(row.getCtx());
			});
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


	@PostMapping("/res/tree")
	@Operation(summary = "加载系统资源树")
	@Action(title="加载系统资源树",type = ActionType.Query,roles = {UserRoles.FORMDEV})
	public Results<List<TreeNodeDto>> resTree(@RequestBody Params<SysResource> params){
		AssertUtil.service().notNull(params.getBody(), "请求参数body不能为空")
			.notNull(params.getBody().getSysId(),"参数body.sysId不能为空");

		SysSystem tmp = new SysSystem();
		tmp.setId(params.getBody().getSysId());
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			tmp.setTenantId(sessionService.getTenantId());
		}
		tmp = dataBaseDao.findById(SqlBuilder.build(SysSystem.class,tmp).dataPermis(PermisRule.ALL)
			.where("(isGlobal = 1 or isGlobal = 0 and tenantId=?) and id=? and delFlag = 0"));
		AssertUtil.service().notNull(tmp, "记录未找到");

		List<TreeNodeDto> treeNodes = new ArrayList<>();
		SystemInfoDto system=SystemInfoDto.from(tmp);
		if(!ObjectUtil.isEmpty(system.getApps())){
			Set<Long> appIds = system.getApps().stream().map(SystemAppDto::getId).collect(Collectors.toSet());
			if(!appIds.isEmpty()){
				system.getApps().stream().forEach(app->{
					TreeNodeDto node = new TreeNodeDto();
					node.setNtype("app");
					node.setPid(-1L);
					node.setId(app.getId());
					node.setTitle(app.getName());
					treeNodes.add(node);
				});
				List<SysResource> appResources = dataBaseDao.findList(SqlBuilder.build(SysResource.class,appIds)
					.where("appId in [query.ids]")
					.sort(Sort.build("ordered", "asc"))
					.dataPermis(PermisRule.ALL));
				appResources.stream().forEach(res->{
					TreeNodeDto node = new TreeNodeDto();
					node.setNtype("res");
					node.setPid(res.getParentId());
					if(ObjectUtil.equal(res.getParentId(), -1L)){
						node.setPid(res.getAppId());
					}
					node.setId(res.getId());
					node.setTitle(res.getTitle());
					node.setIconName(res.getIconName());
					node.setData(res);
					treeNodes.add(node);
				});
			}
		}

		List<SysResource> resources = dataBaseDao.findList(SqlBuilder.build(params)
			.where("appId is null and sysId = ?")
			.sort(Sort.build("ordered", "asc"))
			.dataPermis(PermisRule.ALL));
		resources.stream().forEach(res->{
			TreeNodeDto node = new TreeNodeDto();
			node.setNtype("res");
			node.setPid(params.getBody().getParentId());
			node.setId(res.getId());
			node.setTitle(res.getTitle());
			node.setIconName(res.getIconName());
			node.setData(res);
			treeNodes.add(node);
		});

		return Results.success(treeNodes);
	}

	@PostMapping("/res/delete")
	@Operation(summary = "删除系统资源")
	@Action(title="删除系统资源",type = ActionType.Delete,roles = {UserRoles.FORMDEV})
	public Results<Void> resDelete( @RequestBody Set<Long> ids){
		systemService.deletePage(ids);
		return Results.success();
	}


}
