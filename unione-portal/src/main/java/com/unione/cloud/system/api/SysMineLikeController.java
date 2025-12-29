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
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysMineLike;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysMineLike Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-12-29 08:45:26
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：我的收藏",description="SysMineLike")
@RequestMapping("/api/system/mineLike")	 //TreeFeignApi
public class SysMineLikeController implements PojoFeignApi<SysMineLike>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="查询我的收藏",type = ActionType.Query)
	public Results<List<SysMineLike>> find(Params<SysMineLike> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		params.getBody().setDelFlag(0);
		Results<List<SysMineLike>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存我的收藏",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysMineLike entity) {

		BeanUtils.setDefaultValue(entity,"ordered",0);
		if(entity.getId()==null){
			SysMineLike tmp=dataBaseDao.findOne(SqlBuilder.build(SysMineLike.class)
				.where("userId=? and targetId=?")
			.where("userId", sessionService.getUserId())
			.where("targetId", entity.getTargetId()));
			if(tmp!=null){
				entity.setId(tmp.getId());
			}
		}

		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			entity.setDelFlag(0);
			String[] fields = {"ordered","delFlag","descs","configs"};
			SqlBuilder<SysMineLike> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载我的收藏列表",type = ActionType.Query,nolog = true)
	public Results<List<SysMineLike>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysMineLike> rows = dataBaseDao.findByIds(SqlBuilder.build(SysMineLike.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载我的收藏详情",type = ActionType.Query,nolog = true)
	public Results<SysMineLike> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysMineLike tmp = dataBaseDao.findById(SqlBuilder.build(SysMineLike.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除我的收藏",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysMineLike.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}



}
