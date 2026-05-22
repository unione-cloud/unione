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
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.feign.api.FeignSave;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.system.dto.UserInfoDto;
import com.unione.cloud.system.model.SysOrgan;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.service.OrganService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 SysUser Controller 服务
 * @作者	Jeking Yang
 * @日期	2023-8-31 0:00:34
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：用户信息 管理服务",description="SysUser")
@RequestMapping("/api/system/user")
public class SysUserController implements FeignSave<SysUser>,FeignDelete<SysUser>,FeignFind<UserInfoDto>,FeignFindById<SysUser>,FeignDetail<SysUser>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SecretService secretService;
	
	@Autowired
	private OrganService organService;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="查询用户",type = ActionType.Query)
	public Results<List<UserInfoDto>> find(Params<UserInfoDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<UserInfoDto>> results = dataBaseDao.findPages(SqlBuilder.build(params).notEq("id", sessionService.getUserId()));
		LogsUtil.add("分页数据查询，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		Set<Long> orgIds = results.getBody().stream().map(u->u.getOrgId()).filter(i->i!=null)
			.collect(Collectors.toSet());
		Map<Long, SysOrgan> orgMap=organService.loadOrgan(orgIds);
		
		// 数据渲染
		results.getBody().stream().forEach(row->{
			SysOrgan organ=orgMap.get(row.getOrgId());
			if(organ!=null) {
				row.setOrgName(organ.getName());
			}
		});
		
		return results;
	}


	@Override
	@Action(title="保存用户",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Long> save(@Validated(Validator.save.class) SysUser entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			AssertUtil.service().notNull(entity, new String[]{"pwdText"},"属性%s不能为空");
			entity.setPwdSalt(RandomUtil.randomString(16));
			entity.setPwdText(secretService.decrypt(entity.getPwdText()));
			String pwd = SmUtil.sm4(entity.getPwdSalt().getBytes()).encryptHex(entity.getPwdText());
			entity.setPwdText(pwd);
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"orgId","userType","username","realName","aliasName","avatar","birthday","sex","email","qq","tel","status","lockTime","descs"};
			len = dataBaseDao.updateById(SqlBuilder.build(entity).where("id=?").field(fields));
		}
		
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置用户状态",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置状态", description="USERSTATUS 1正常，2禁用，3注销，4锁定")
	public Results<Void> setStatus(@RequestBody SysUser entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3,4), "参数status取值范围[1,2,3,4]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<SysUser>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<SysUser> rows = dataBaseDao.findByIds(SqlBuilder.build(SysUser.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<SysUser> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		SysUser tmp = dataBaseDao.findById(SqlBuilder.build(SysUser.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除用户",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PCONFIG})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysUser.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		return results;
	}



}
