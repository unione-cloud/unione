package com.unione.cloud.portal.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.unione.cloud.portal.system.dto.UserRoleDto;
import com.unione.cloud.portal.system.model.SysGroup;
import com.unione.cloud.portal.system.model.SysGroupMember;
import com.unione.cloud.portal.system.model.SysRole;
import com.unione.cloud.portal.system.model.SysUserRole;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UserRoleDto Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：用户角色",description="UserRoleDto")
@RequestMapping("/api/system/userRole")	 //TreeFeignApi
public class SysUserRoleController implements PojoFeignApi<UserRoleDto>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询用户角色",type = ActionType.Query)
	public Results<List<UserRoleDto>> find(Params<UserRoleDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<UserRoleDto>> results = dataBaseDao.findPages(params);
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存用户角色",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Long> save(@Validated(Validator.save.class) UserRoleDto entity) {
		// 参数处理
		SysRole role=dataBaseDao.findById(SqlBuilder.build(SysRole.class,entity.getRoleId()));
		AssertUtil.service().notNull(role, "角色不存在");

		int len = 0;
		if(entity.getId()==null) {
			BeanUtils.setDefaultValue(entity,"enDilivery",0);
			if(ObjectUtil.isEmpty(entity.getUsers())){
				// 单个添加：
				AssertUtil.service().notNull(entity.getRoleId(), "属性角色id不能为空");
				AssertUtil.service().notNull(entity.getUserId(), "属性用户id不能为空");
				len = dataBaseDao.insert(entity);
			}else{
				// 批量添加：
				List<SysUserRole> members = entity.getUsers().stream().map(user->{
					SysUserRole member=new SysUserRole();
					BeanUtils.copyProperties(entity, member);
					member.setUserId(user.getId());
					return member;
				}).collect(Collectors.toList());
				int lens[] = dataBaseDao.insertBatch(members);
				len = Arrays.stream(lens).sum();
			}
		}else {
			String[] fields = {"userId","roleId","enDilivery"};
			SqlBuilder<UserRoleDto> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}




	@Override
	public Results<List<UserRoleDto>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<UserRoleDto> rows = dataBaseDao.findByIds(SqlBuilder.build(UserRoleDto.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<UserRoleDto> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		UserRoleDto tmp = dataBaseDao.findById(SqlBuilder.build(UserRoleDto.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除用户角色",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(UserRoleDto.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
