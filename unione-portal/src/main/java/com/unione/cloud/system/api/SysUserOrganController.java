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
import com.unione.cloud.system.dto.UserOrganDto;
import com.unione.cloud.system.model.SysGroupMember;
import com.unione.cloud.system.model.SysOrgan;
import com.unione.cloud.system.model.SysUserOrgan;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UserOrganDto Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：用户机构",description="UserOrganDto")
@RequestMapping("/api/system/userOrgan")	 //TreeFeignApi
public class SysUserOrganController implements PojoFeignApi<UserOrganDto>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询用户机构",type = ActionType.Query)
	public Results<List<UserOrganDto>> find(Params<UserOrganDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<UserOrganDto>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存用户机构",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Long> save(@Validated(Validator.save.class) UserOrganDto entity) {
		// 参数处理
		SysOrgan organ=dataBaseDao.findById(SqlBuilder.build(SysOrgan.class,entity.getOrgId()));
		AssertUtil.service().notNull(organ, "机构不存在");
		
		int len = 0;
		if(entity.getId()==null) {
			entity.setStatus(1);
			entity.setTimeJoin(DateUtil.date());
			BeanUtils.setDefaultValue(entity,"ordered",0);
			if(ObjectUtil.isEmpty(entity.getUsers())){
				// 单个添加：
				AssertUtil.service().notNull(entity.getOrgId(), "属性机构id不能为空");
				AssertUtil.service().notNull(entity.getUserId(), "属性用户id不能为空");
				len = dataBaseDao.insert(entity);
			}else{
				// 批量添加：
				List<SysUserOrgan> members = entity.getUsers().stream().map(user->{
					SysUserOrgan member=new SysUserOrgan();
					BeanUtils.copyProperties(entity, member);
					member.setOrgId(user.getId());
					member.setUserId(user.getId());
					return member;
				}).collect(Collectors.toList());
				int lens[] = dataBaseDao.insertBatch(members);
				len = Arrays.stream(lens).sum();
			}
		}else {
			String[] fields = {"orgId","userId","timeJoin","timeLeave","status","ordered"};
			SqlBuilder<UserOrganDto> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}

	@PostMapping("/status")
	@Action(title="设置用户机构状态",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置状态", description="MENBERSTATUS 1正常，2离开")
	public Results<Void> setStatus(@RequestBody UserOrganDto entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2), "参数status取值范围[1,2]");
		
		String fields[]= {"status"};
		if(entity.getStatus()==2) {
			entity.setTimeLeave(DateUtil.date());
			fields = new String[] {"status","timeLeave"};
		}
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<UserOrganDto>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<UserOrganDto> rows = dataBaseDao.findByIds(SqlBuilder.build(UserOrganDto.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<UserOrganDto> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		UserOrganDto tmp = dataBaseDao.findById(SqlBuilder.build(UserOrganDto.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除用户机构",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(UserOrganDto.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
