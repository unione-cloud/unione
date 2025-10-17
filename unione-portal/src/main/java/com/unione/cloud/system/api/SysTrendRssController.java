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
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysTrendRss;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysTrendRss Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 14:46:39
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：动态订阅",description="SysTrendRss")
@RequestMapping("/api/system/trendRss")	 //TreeFeignApi
public class SysTrendRssController implements PojoFeignApi<SysTrendRss>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询动态订阅",type = ActionType.Query)
	public Results<List<SysTrendRss>> find(Params<SysTrendRss> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysTrendRss>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存动态订阅",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysTrendRss entity) {
		AssertUtil.service().isTrue(!ObjectUtil.isEmpty(entity.getTel()) || !ObjectUtil.isEmpty(entity.getEmail()), "电话和邮箱不不能都为空");
		// 参数处理
		int len = 0;

		SysTrendRss tmp = dataBaseDao.findOne(SqlBuilder.build(entity).where("tel=? and email=?"));
		if(tmp!=null){
			entity.setId(tmp.getId());
			BeanUtils.setDefaultValue(entity, "name", tmp.getName());
			BeanUtils.setDefaultValue(entity, "email", tmp.getEmail());
			BeanUtils.setDefaultValue(entity, "tel", tmp.getTel());
			BeanUtils.setDefaultValue(entity, "descs", tmp.getDescs());
		}

		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"name","email","tel","subject","descs"};
			SqlBuilder<SysTrendRss> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载动态订阅列表",type = ActionType.Query,nolog = true)
	public Results<List<SysTrendRss>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysTrendRss> rows = dataBaseDao.findByIds(SqlBuilder.build(SysTrendRss.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载动态订阅详情",type = ActionType.Query,nolog = true)
	public Results<SysTrendRss> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysTrendRss tmp = dataBaseDao.findById(SqlBuilder.build(SysTrendRss.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除动态订阅",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(SysTrendRss.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载动态订阅子级",type = ActionType.Query,nolog = true)
//	public Results<List<SysTrendRss>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		SysTrendRss params = new SysTrendRss();
//		params.setParentId(pid);
//		List<SysTrendRss> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
