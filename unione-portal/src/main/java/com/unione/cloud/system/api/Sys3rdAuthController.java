package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
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
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.dto.ThirdAuthDto;
import com.unione.cloud.system.model.Sys3rdAuth;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.service.ThirdAuthService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	Sys3rdAuth Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-03 09:39:18
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：第三方系统认证",description="所有请求外部系统认证管理，前端接口动态加载认证信息（后端加密，前端解密）")
@RequestMapping("/api/system/3rdAuth")	 //TreeFeignApi
public class Sys3rdAuthController implements PojoFeignApi<Sys3rdAuth>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ThirdAuthService thirdAuthService;


	@GetMapping("/list/{scene}")
	@Operation(summary="加载第三方系统认证列表",description = "根据场景加载可用列表，排序")
	@Action(title="加载第三方系统认证列表",type = ActionType.Query)
	public Results<List<Sys3rdAuth>> list(@PathVariable("scene") String scene) {
		AssertUtil.service().notNull(scene,"场景不能为空");

		List<Sys3rdAuth> list = dataBaseDao.findList(SqlBuilder.build(Sys3rdAuth.class)
			.where("scene = ? and status = 1 and delFlag = 0 and (tenantId = ? or isGlobal = 1)")
			.where("scene",scene)
			.where("tenantId",sessionService.getTenantId())
			.dataPermis(PermisRule.ALL)
			.sort(Sort.build("ordered", "asc"))
		);
		
		return Results.success(list);
	}


	@GetMapping("/load/{sn}")
	@Operation(summary="加载第三方系统认证",description = "根据编码加载认证信息,后端完成授权签名、认证接入等操作")
	@Action(title="加载第三方系统认证",type = ActionType.Query)
	public Results<ThirdAuthDto> load(@PathVariable("sn") String sn){
		AssertUtil.service().notNull(sn,"编码不能为空");

		ThirdAuthDto dto = thirdAuthService.load(sn);

		return Results.success(dto);
	}
	
	
	@Override
	@Action(title="查询第三方系统认证",type = ActionType.Query,roles={UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	public Results<List<Sys3rdAuth>> find(Params<Sys3rdAuth> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<Sys3rdAuth>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存第三方系统认证",type = ActionType.Save,roles={UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	public Results<Long> save(@Validated(Validator.save.class) Sys3rdAuth entity) {
		// 参数处理
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "delFlag",0);

		// 验证编码是否已存在
		long count = dataBaseDao.count(SqlBuilder.build(Sys3rdAuth.class)
			.where("id!=? and sn=?")
			.where("sn",entity.getSn())
		    .where("id",entity.getId()));
		AssertUtil.service().isTrue(count==0, "编码已暂用");

		if(!ObjectUtil.isEmpty(entity.getDataJson())){
			ThirdAuthDto dto = JsonUtil.toBean(ThirdAuthDto.class,entity.getDataJson());
			dto.setUrl(entity.getUrl());
			entity.setDataJson(JsonUtil.toJson(dto));
		}

		int len = 0;
		if(entity.getId()==null) {
			entity.setIsGlobal(0);
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"scene","title","sn","icon","picMax","picMid","picMix","url","dataJson","scriptTxt","docLink","descs","status","ordered"};
			SqlBuilder<Sys3rdAuth> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	
	@PostMapping("/status")
	@Action(title="设置第三方系统认证状态",type = ActionType.Save,roles = {UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置状态", description="TENANTSTATUS 1试用，2开通，3关闭")
	public Results<Void> setStatus(@RequestBody Sys3rdAuth entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,0), "参数status取值范围[1,0]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载第三方系统认证列表",type = ActionType.Query,nolog = true,roles={UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	public Results<List<Sys3rdAuth>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<Sys3rdAuth> rows = dataBaseDao.findByIds(SqlBuilder.build(Sys3rdAuth.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载第三方系统认证详情",type = ActionType.Query,nolog = true,roles={UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	public Results<Sys3rdAuth> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		Sys3rdAuth tmp = dataBaseDao.findById(SqlBuilder.build(Sys3rdAuth.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除第三方系统认证",type = ActionType.Delete,roles={UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(Sys3rdAuth.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
