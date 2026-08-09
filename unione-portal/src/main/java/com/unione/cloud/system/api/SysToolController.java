package com.unione.cloud.system.api;

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
import com.unione.cloud.system.model.SysTool;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysTool Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2026-08-09 08:43:28
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：常用工具",description="SysTool")
@RequestMapping("/api/system/tool")	 //TreeFeignApi
public class SysToolController implements PojoFeignApi<SysTool>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询常用工具",type = ActionType.Query)
	public Results<List<SysTool>> find(Params<SysTool> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysTool>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存常用工具",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysTool entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"orgId","userId","sysId","resId","gname","title","url","isGlobal","isDefualt","isPrivate","types","answerWay","icon","picMax","picMid","picMix","ordered","status","descs","configs","delFlag"};
			SqlBuilder<SysTool> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载常用工具列表",type = ActionType.Query,nolog = true)
	public Results<List<SysTool>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysTool> rows = dataBaseDao.findByIds(SqlBuilder.build(SysTool.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载常用工具详情",type = ActionType.Query,nolog = true)
	public Results<SysTool> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysTool tmp = dataBaseDao.findById(SqlBuilder.build(SysTool.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除常用工具",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysTool.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载常用工具子级",type = ActionType.Query,nolog = true)
//	public Results<List<SysTool>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		SysTool params = new SysTool();
//		params.setParentId(pid);
//		List<SysTool> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
