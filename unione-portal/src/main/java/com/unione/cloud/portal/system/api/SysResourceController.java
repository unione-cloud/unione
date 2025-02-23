package com.unione.cloud.portal.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Tag;
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
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.portal.system.model.SysResource;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysResource Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：系统资源",description="SysResource")
@RequestMapping("/api/system/resource")	 //TreeFeignApi
public class SysResourceController implements TreeFeignApi<SysResource>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询资源",type = ActionType.Query)
	public Results<List<SysResource>> find(Params<SysResource> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysResource>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存资源",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysResource entity) {
		// 参数处理
		BeanUtils.setDefaultValue(entity, "parentId",-1L);
		if(!Objects.equals(-1L, entity.getParentId())) {
			SysResource parent = dataBaseDao.findOne(SqlBuilder.build(entity).where("id=?"));
			AssertUtil.service().notNull(parent, "上级节点未找到");
			if(!Objects.equals(parent.getIsLeaf(), 0)) {
				parent.setIsLeaf(0);
				dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
			}
		}

		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"appId","parentId","name","title","alias","types","url","isIframe","isExternal","isHide","isLeaf","isNeedPermis","isPlatform","icon","picMax","picMid","picMix","ordered","status","descs","configs"};
			SqlBuilder<SysResource> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置资源状态",type = ActionType.Save,roles = {UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置资源状态", description="USEORNOT 1使用，0停用")
	public Results<Void> setStatus(@RequestBody SysResource entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[1,0]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<SysResource>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<SysResource> rows = dataBaseDao.findByIds(SqlBuilder.build(SysResource.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<SysResource> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		SysResource tmp = dataBaseDao.findById(SqlBuilder.build(SysResource.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除资源",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysResource.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		return results;
	}


	@Override
	public Results<List<SysResource>> children(Long sid){
		 //参数处理
		AssertUtil.service().notNull(sid, "参数sid不能为空");
		// 执行查询
		SysResource params = new SysResource();
		params.setParentId(sid);
		List<SysResource> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}

}
