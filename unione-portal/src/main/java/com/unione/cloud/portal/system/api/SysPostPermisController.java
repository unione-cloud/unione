package com.unione.cloud.portal.system.api;

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
import com.unione.cloud.portal.system.model.SysPostPermis;
import com.unione.cloud.web.logs.LogsUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysPostPermis Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：岗位权限",description="SysPostPermis")
@RequestMapping("/api/system/postPermis")	 //TreeFeignApi
public class SysPostPermisController implements PojoFeignApi<SysPostPermis>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询岗位权限",type = ActionType.Query)
	public Results<List<SysPostPermis>> find(Params<SysPostPermis> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysPostPermis>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存岗位权限",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Long> save(@Validated(Validator.save.class) SysPostPermis entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"postId","appId","resId","resType","enDilivery"};
			SqlBuilder<SysPostPermis> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}




	@Override
	public Results<List<SysPostPermis>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<SysPostPermis> rows = dataBaseDao.findByIds(SqlBuilder.build(SysPostPermis.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<SysPostPermis> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		SysPostPermis tmp = dataBaseDao.findById(SqlBuilder.build(SysPostPermis.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除岗位权限",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysPostPermis.class,ids));
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
