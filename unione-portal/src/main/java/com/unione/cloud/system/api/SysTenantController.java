package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.unione.cloud.system.dto.TenantInfoDto;
import com.unione.cloud.system.dto.UserRoleDto;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.service.TenantService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysTenant Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：租户信息",description="SysTenant")
@RequestMapping("/api/system/tenant")	 //TreeFeignApi
public class SysTenantController implements PojoFeignApi<TenantInfoDto>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private TenantService tenantService;
	
	
	@Override
	@Action(title="查询租户",type = ActionType.Query)
	public Results<List<TenantInfoDto>> find(Params<TenantInfoDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<TenantInfoDto>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());

		Set<Long> adminIds = results.getBody().stream().filter(r->r.getAdminId()!=null).map(r->r.getAdminId()).collect(Collectors.toSet());
		if(!adminIds.isEmpty()) {
			List<UserRoleDto> list=dataBaseDao.findList("loadUserRoleList",SqlBuilder.build(UserRoleDto.class,adminIds));
			Map<Long, List<String>> map = list.stream().collect(Collectors.groupingBy(UserRoleDto::getUserId, Collectors.mapping(UserRoleDto::getRoleSn, Collectors.toList())));
			results.getBody().forEach(r->{
				if(map.get(r.getAdminId())==null) {
					return;
				}
				r.setRoleList(map.get(r.getAdminId()).stream().collect(Collectors.joining(",")));
			});
		}
		
		return results;
	}


	@Override
	@Action(title="保存租户",type = ActionType.Save,roles = {UserRoles.SYSOPSUSER})
	public Results<Long> save(@Validated(Validator.save.class) TenantInfoDto entity) {
		return tenantService.save(entity);
	}


	@PostMapping("/status")
	@Action(title="设置租户状态",type = ActionType.Save,roles = {UserRoles.SYSOPSUSER})
	@Operation(summary = "设置状态", description="TENANTSTATUS 1试用，2开通，3关闭")
	public Results<Void> setStatus(@RequestBody SysTenant entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3), "参数status取值范围[1,2,3]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		tenantService.clear(entity.getId());
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<TenantInfoDto>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<TenantInfoDto> rows = dataBaseDao.findByIds(SqlBuilder.build(TenantInfoDto.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<TenantInfoDto> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		TenantInfoDto tmp = dataBaseDao.findById(SqlBuilder.build(TenantInfoDto.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除租户",type = ActionType.Delete,roles = {UserRoles.SYSOPSUSER})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysTenant.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);

		if(count>0){
			ids.stream().forEach(id->{
				tenantService.clear(id);
			});
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
