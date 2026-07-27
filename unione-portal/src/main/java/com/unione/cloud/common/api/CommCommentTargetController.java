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
import com.unione.cloud.common.model.CommCommentTarget;
import com.unione.cloud.common.service.CommentService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	CommCommentTarget Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-27 14:42:47
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "通用：评论目标，如，是否关闭评论，是否开启点赞，是否审核等等",description="CommCommentTarget")
@RequestMapping("/api/common/comment/target")	 //TreeFeignApi
public class CommCommentTargetController implements PojoFeignApi<CommCommentTarget>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private CommentService commentService;
	
	
	@Override
	@Action(title="查询评论目标",type = ActionType.Query)
	public Results<List<CommCommentTarget>> find(Params<CommCommentTarget> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<CommCommentTarget>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存评论目标",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) CommCommentTarget entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"auditFlag","closeFlag","likeEnable","fileEnable","fileCount","fileType"};
			SqlBuilder<CommCommentTarget> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
			if(len>0){
				commentService.clearTarget(entity.getTargetType());
			}
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载评论目标列表",type = ActionType.Query,nolog = true)
	public Results<List<CommCommentTarget>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<CommCommentTarget> rows = dataBaseDao.findByIds(SqlBuilder.build(CommCommentTarget.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载评论目标详情",type = ActionType.Query,nolog = true)
	public Results<CommCommentTarget> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		CommCommentTarget tmp = dataBaseDao.findById(SqlBuilder.build(CommCommentTarget.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除评论目标",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");

		List<CommCommentTarget> rows = dataBaseDao.findByIds(SqlBuilder.build(CommCommentTarget.class,new ArrayList<>(ids)));
		if(rows.isEmpty()){
			return Results.failure("记录未找到");
		}
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(CommCommentTarget.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);

		if(count>0){
			rows.stream().forEach(row->{
				commentService.clearTarget(row.getTargetType());
			});
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
