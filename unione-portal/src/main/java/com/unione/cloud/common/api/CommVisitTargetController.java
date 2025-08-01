package com.unione.cloud.common.api;

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
import com.unione.cloud.common.model.CommVisitTarget;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	CommVisitTarget Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "通用：访问登记-目标",description="CommVisitTarget")
@RequestMapping("/api/common/visitTarget")	
public class CommVisitTargetController implements PojoFeignApi<CommVisitTarget>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询访问登记-目标",type = ActionType.Query)
	public Results<List<CommVisitTarget>> find(Params<CommVisitTarget> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<CommVisitTarget>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存访问登记-目标",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) CommVisitTarget entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"appId","parentId","orgId","userId","targetType","targetTitle","targetId","targetUrl","cover","ordered","descs","status"};
			SqlBuilder<CommVisitTarget> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载访问登记-目标列表",type = ActionType.Query,nolog = true)
	public Results<List<CommVisitTarget>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<CommVisitTarget> rows = dataBaseDao.findByIds(SqlBuilder.build(CommVisitTarget.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载访问登记-目标详情",type = ActionType.Query,nolog = true)
	public Results<CommVisitTarget> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		CommVisitTarget tmp = dataBaseDao.findById(SqlBuilder.build(CommVisitTarget.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除访问登记-目标",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(CommVisitTarget.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载访问登记-目标子级",type = ActionType.Query,nolog = true)
//	public Results<List<CommVisitTarget>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		CommVisitTarget params = new CommVisitTarget();
//		params.setParentId(pid);
//		List<CommVisitTarget> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
