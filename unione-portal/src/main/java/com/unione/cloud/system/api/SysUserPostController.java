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
import com.unione.cloud.system.dto.UserPostDto;
import com.unione.cloud.system.model.SysGroup;
import com.unione.cloud.system.model.SysGroupMember;
import com.unione.cloud.system.model.SysPost;
import com.unione.cloud.system.model.SysUserPost;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UserPostDto Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:38
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：用户岗位",description="UserPostDto")
@RequestMapping("/api/system/userPost")	 //TreeFeignApi
public class SysUserPostController implements PojoFeignApi<UserPostDto>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询用户岗位",type = ActionType.Query)
	public Results<List<UserPostDto>> find(Params<UserPostDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<UserPostDto>> results = dataBaseDao.findPages(params);
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存用户岗位",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Long> save(@Validated(Validator.save.class) UserPostDto entity) {
		// 参数处理
		SysPost post=dataBaseDao.findById(SqlBuilder.build(SysPost.class,entity.getPostId()));
		AssertUtil.service().notNull(post, "岗位不存在");

		int len = 0;
		if(entity.getId()==null) {
			entity.setStatus(1);
			entity.setTimeJoin(DateUtil.date());
			BeanUtils.setDefaultValue(entity,"ordered",0);
			if(ObjectUtil.isEmpty(entity.getUsers())){
				// 单个添加：
				AssertUtil.service().notNull(entity.getOrgId(), "属性机构id不能为空");
				AssertUtil.service().notNull(entity.getOrgName(), "属性机构名称不能为空");
				AssertUtil.service().notNull(entity.getUserId(), "属性用户id不能为空");
				AssertUtil.service().notNull(entity.getName(), "属性成员名称不能为空");
				len = dataBaseDao.insert(entity);
			}else{
				// 批量添加：
				List<SysUserPost> members = entity.getUsers().stream().map(user->{
					SysUserPost member=new SysUserPost();
					BeanUtils.copyProperties(entity, member);
					member.setOrgId(user.getId());
					member.setOrgName(user.getOrgName());
					member.setUserId(user.getId());
					member.setName(user.getTitle());
					return member;
				}).collect(Collectors.toList());
				int lens[] = dataBaseDao.insertBatch(members);
				len = Arrays.stream(lens).sum();
			}
		}else {
			String[] fields = {"postId","userId","userOrgId","userOrgName","name","timeJoin","timeLeave","status","ordered","descs"};
			SqlBuilder<UserPostDto> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置用户岗位状态",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置状态", description="MENBERSTATUS 1正常，2离开")
	public Results<Void> setStatus(@RequestBody UserPostDto entity){
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
	public Results<List<UserPostDto>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<UserPostDto> rows = dataBaseDao.findByIds(SqlBuilder.build(UserPostDto.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<UserPostDto> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		UserPostDto tmp = dataBaseDao.findById(SqlBuilder.build(UserPostDto.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除用户岗位",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(UserPostDto.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
