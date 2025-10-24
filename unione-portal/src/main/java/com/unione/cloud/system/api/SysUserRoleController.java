package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.unione.cloud.system.dto.UserRoleDto;
import com.unione.cloud.system.model.SysGroup;
import com.unione.cloud.system.model.SysGroupMember;
import com.unione.cloud.system.model.SysRole;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.model.SysUserRole;
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
	@Action(title="保存用户角色",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Long> save(@Validated(Validator.save.class) UserRoleDto entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			if(ObjectUtil.isEmpty(entity.getUsers())){
				if(ObjectUtil.isEmpty(entity.getRoles())){
					// 单个添加：
					BeanUtils.setDefaultValue(entity,"enDilivery",0);
					AssertUtil.service().notNull(entity.getRoleId(), "属性角色id不能为空");
					AssertUtil.service().notNull(entity.getUserId(), "属性用户id不能为空");
					SysRole role=dataBaseDao.findById(SqlBuilder.build(SysRole.class,entity.getRoleId()));
					AssertUtil.service().notNull(role, "角色不存在");
					SysUser user=dataBaseDao.findById(SqlBuilder.build(SysUser.class,entity.getUserId()));
					AssertUtil.service().notNull(user, "用户不存在");
					
					len = dataBaseDao.insert(entity);
				}else{
					// 批量添加角色：	
					AssertUtil.service().notNull(entity.getUserId(), "属性用户id不能为空");
					SysUser user=dataBaseDao.findById(SqlBuilder.build(SysUser.class,entity.getUserId()));
					AssertUtil.service().notNull(user, "用户不存在");

					SqlBuilder<SysUserRole> query=SqlBuilder.build(SysUserRole.class);
					query.where("userId", entity.getUserId());
					Map<Long,SysUserRole> uRoles=dataBaseDao.findList(query).stream()
						.collect(Collectors.toMap(SysUserRole::getRoleId, role->role));

					List<SysUserRole> adds = new ArrayList<>();
					List<SysUserRole> upds = new ArrayList<>();
					entity.getRoles().stream().forEach(role->{
						SysUserRole member=uRoles.remove(role.getId());
						if(ObjectUtil.isEmpty(member)){
							member=new SysUserRole();
							BeanUtils.copyProperties(entity, member);
							member.setRoleId(role.getId());
							BeanUtils.setDefaultValue(member,"enDilivery",0);
							adds.add(member);
						}else{
							if(ObjectUtil.isNotEmpty(role.getEnDilivery()) && 
								!ObjectUtil.equal(role.getEnDilivery(), member.getEnDilivery())){
								member.setEnDilivery(role.getEnDilivery());
								upds.add(member);
							}
						}
					});
					// 新增角色
					if(ObjectUtil.isNotEmpty(adds)){
						int lens[] = dataBaseDao.insertBatch(adds);
						len += Arrays.stream(lens).sum();
					}
					// 更新角色
					if(ObjectUtil.isNotEmpty(upds)){
						len += upds.stream().map(row->{
							SqlBuilder<SysUserRole> sqlBuilder=SqlBuilder.build(row).field("enDilivery");
							return dataBaseDao.updateById(sqlBuilder);
						}).reduce(0,Integer::sum);
					}
					// 删除角色
					if(!uRoles.isEmpty()){
						List<Long> ids = uRoles.values().stream().map(row->row.getId()).collect(Collectors.toList());
						SqlBuilder<SysUserRole> sqlBuilder=SqlBuilder.build(SysUserRole.class,ids);
						len += dataBaseDao.deleteById(sqlBuilder);
					}
				}
			}else{
				// 批量添加用户：
				AssertUtil.service().notNull(entity.getRoleId(), "属性角色id不能为空");
				SysRole role=dataBaseDao.findById(SqlBuilder.build(SysRole.class,entity.getRoleId()));
				AssertUtil.service().notNull(role, "角色不存在");

				SqlBuilder<SysUserRole> query=SqlBuilder.build(SysUserRole.class);
				query.where("roleId", entity.getRoleId());
				Map<Long,SysUserRole> uRoles=dataBaseDao.findList(query).stream()
					.collect(Collectors.toMap(SysUserRole::getUserId, row->row));

				List<SysUserRole> adds = new ArrayList<>();
				List<SysUserRole> upds = new ArrayList<>();
				entity.getUsers().stream().forEach(user->{
					SysUserRole member=uRoles.remove(user.getId());
					if(ObjectUtil.isEmpty(member)){
						member=new SysUserRole();
						BeanUtils.copyProperties(entity, member);
						member.setUserId(user.getId());
						BeanUtils.setDefaultValue(member,"enDilivery",0);
						adds.add(member);
					}else{
						if(ObjectUtil.isNotEmpty(user.getEnDilivery()) && 
							!ObjectUtil.equal(user.getEnDilivery(), member.getEnDilivery())){
							member.setEnDilivery(user.getEnDilivery());
							upds.add(member);
						}
					}
				});
				// 新增用户
				if(ObjectUtil.isNotEmpty(adds)){
					int lens[] = dataBaseDao.insertBatch(adds);
					len += Arrays.stream(lens).sum();
				}
				// 更新角色
				if(ObjectUtil.isNotEmpty(upds)){
					len += upds.stream().map(row->{
						SqlBuilder<SysUserRole> sqlBuilder=SqlBuilder.build(row).field("enDilivery");
						return dataBaseDao.updateById(sqlBuilder);
					}).reduce(0,Integer::sum);
				}
			}
		}else {
			String[] fields = {"enDilivery"};
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
