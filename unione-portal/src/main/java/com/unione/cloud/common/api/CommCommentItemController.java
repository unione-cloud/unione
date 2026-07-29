package com.unione.cloud.common.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.CommentTargetEntity;
import com.unione.cloud.common.model.CommCommentItem;
import com.unione.cloud.common.model.CommCommentSetting;
import com.unione.cloud.common.model.CommCommentTarget;
import com.unione.cloud.common.service.CommentService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.service.CodeTreeService;
import com.unione.cloud.system.service.UserService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	CommCommentItem Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-27 13:51:29
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "通用：评论明细，通用评论",description="CommCommentItem")
@RequestMapping("/api/common/comment/item")	 //TreeFeignApi
public class CommCommentItemController implements TreeFeignApi<CommCommentItem>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private CommentService commentService;

	@Autowired
	private UserService userService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private CodeTreeService codeTreeService;
	
	
	@Override
	@Action(title="查询评论明细",type = ActionType.Query)
	public Results<List<CommCommentItem>> find(Params<CommCommentItem> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		
		params.getBody().setDelFlag(0);
		Results<List<CommCommentItem>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());

		// 加载用户信息
		Set<Long> uids = results.getBody().stream().map(CommCommentItem::getUserId).collect(Collectors.toSet());
		Map<Long,SysUser> userMap = userService.loadUser(uids);

		//数据渲染
		results.getBody().stream().forEach(item->{
			SysUser user = userMap.get(item.getUserId());
			if(user!=null) {
				item.putExtra("userName", user.getAliasName());
				if(ObjectUtil.isEmpty(user.getAliasName())){
					item.putExtra("userName", user.getUsername());
				}
				item.putExtra("userAvatar", user.getAvatar());
			}
		});
		
		return results;
	}


	@PostMapping("/load")
	@Action(title="加载评论列表",type = ActionType.Query)
	public Results<CommentTargetEntity> load(@RequestBody Params<CommCommentItem> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空")
			.notNull(params.getBody().getTargetId(), "目标ID不能为空")
			.notNull(params.getBody().getTargetType(), "目标类型不能为空");

		CommCommentTarget target = commentService.loadTarget(params.getBody().getTargetType());
		CommCommentSetting setting = commentService.loadSetting(params.getBody().getTargetId());
		if(setting==null){
			setting=BeanUtils.copyProperties(target, CommCommentSetting.class);
		}
		CommentTargetEntity entity=new CommentTargetEntity();
		
		// 参数处理
		params.getBody().setDelFlag(0);
		params.getBody().setStatus(1);
		if(params.getBody().getParentId()==null || ObjectUtil.equal(target.getAsyncFlag(), 1)){
			params.getBody().setParentId(-1L);
		}

		// 加载评论数据
		Results<List<CommCommentItem>> results = dataBaseDao.findPages(SqlBuilder.build(params)
			.sort(Sort.build("isTop", "desc"),Sort.build("ordered", "desc")));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());

		List<CommCommentItem> list=new ArrayList<>(results.getBody());

		// 如果是同步加载
		if(ObjectUtil.equal(target.getAsyncFlag(), 1) && !ObjectUtil.isEmpty(results.getBody())){
			// 获取层级编码集合
			List<String> lvsnList = results.getBody().stream().map(CommCommentItem::getLvsn).collect(Collectors.toList());

			// 加载子集列表
			List<CommCommentItem> children = dataBaseDao.findList(SqlBuilder.build(CommCommentItem.class)
				.where("delFlag = 0 and (status = 1 or status = 2 and userId = ?) and targetId = ? and (forEach(lvsns,lvsn like [?%],or))")
				.where("targetId", params.getBody().getTargetId())
				.where("userId", sessionService.getUserId())
				.where("lvsns", lvsnList)
				.sort(Sort.build("isTop", "desc"),Sort.build("ordered", "desc")));
			if(!children.isEmpty()){
				list.addAll(children);
	
				// 构建树结构
				Map<Long,CommCommentItem> roots=results.getBody().stream().collect(Collectors.toMap(CommCommentItem::getId, Function.identity()));
				Map<Long,CommCommentItem> map = new HashMap<>();
				children.stream().forEach(row->{
					if(roots.get(row.getId())==null){
						map.put(row.getId(), row);
					}
				});
				children.stream().forEach(item->{
					if(roots.get(item.getId())!=null){
						return;
					}
					CommCommentItem parent=map.get(item.getParentId());
					if(parent==null){
						CommCommentItem root=roots.get(item.getParentId());
						if(root!=null){
							if(root.getChildren()==null){
								root.setChildren(new ArrayList<>());
							}
							root.getChildren().add(item);
						}
					}else{
						if(parent.getChildren()==null){
							parent.setChildren(new ArrayList<>());
						}
						parent.getChildren().add(item);
					}
				});
			}
		}

		// 渲染用户信息
		Set<Long> uids = list.stream().map(CommCommentItem::getUserId).collect(Collectors.toSet());
		Map<Long,SysUser> userMap = userService.loadUser(uids);
		list.stream().forEach(item->{
			SysUser user = userMap.get(item.getUserId());
			if(user!=null) {
				item.putExtra("userName", user.getAliasName());
				if(ObjectUtil.isEmpty(user.getAliasName())){
					item.putExtra("userName", user.getUsername());
				}
				item.putExtra("userAvatar", user.getAvatar());
			}
		});

		entity.setItems(results.getBody());
		entity.setSetting(setting);
		return Results.success(entity).setPage(results.getPage()).setPageSize(params.getPageSize());
	}


	@Override
	@Action(title="保存评论明细",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) CommCommentItem entity) {
		// 参数处理
		CommCommentSetting setting = commentService.loadSetting(entity.getTargetId());
		CommCommentTarget target=commentService.loadTarget(entity.getTargetType());
		if(setting==null){
			setting=BeanUtils.copyProperties(target, CommCommentSetting.class);
		}
		AssertUtil.service().isEq(setting.getCloseFlag(), 1, "评论已关闭");
		BeanUtils.setDefaultValue(entity, "parentId",-1L);
		entity.setStatus(1);
		entity.setDelFlag(0);
		entity.setLikeCount(0);
		entity.setIsTop(0);
		entity.setOrdered(0);
		if(ObjectUtil.equal(target.getAuditFlag(), 1)){
			entity.setStatus(2);
		}

		// 加载上级评论
		CommCommentItem parent = null;
		if(!ObjectUtil.equal(entity.getParentId(), -1L)){
			parent = commentService.loadItem(entity.getParentId());
			AssertUtil.service().notNull(parent, "上级评论不存在");
		}
		if(parent!=null && setting.getRefLevel()!=null&&setting.getRefLevel()>0){
			AssertUtil.service().isTrue((parent.getLevel()+1)<setting.getRefLevel(), "引用层级错误");
		}

		int len = 0;
		if(entity.getId()==null) {
			if(parent==null){
				String lvsn = codeTreeService.generate("COMMENTITEM");
				entity.setLvsn(lvsn);
				entity.setLevel(0);
			}else{
				String lvsn = codeTreeService.generate("COMMENTITEM",parent.getLvsn(),parent.getLevel()+1);
				entity.setLvsn(lvsn);
				entity.setLevel(parent.getLevel()+1);
			}
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"contents","files"};
			entity.setUserId(sessionService.getUserId());
			SqlBuilder<CommCommentItem> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
			if(len>0){
				commentService.clearItem(entity.getId());
			}
		}
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/incLike")
	@Action(title="点赞评论",type = ActionType.Other)
	public Results<Void> incLike(@RequestBody Long id){
		// 点赞评论
		commentService.incLike(id);
		return Results.success();
	}


	@PostMapping("/status")
	@Action(title="设置评论状态",type = ActionType.Save,roles = {UserRoles.FORMDEV,UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置状态", description="COMMENTSTS 1正常，2待审，3拒绝")
	public Results<Void> setStatus(@RequestBody CommCommentItem entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3), "参数status取值范围[1,2,3]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}


	@Override
	@Action(title="加载评论明细列表",type = ActionType.Query,nolog = true)
	public Results<List<CommCommentItem>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<CommCommentItem> rows = dataBaseDao.findByIds(SqlBuilder.build(CommCommentItem.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载评论明细详情",type = ActionType.Query,nolog = true)
	public Results<CommCommentItem> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		CommCommentItem tmp = dataBaseDao.findById(SqlBuilder.build(CommCommentItem.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除评论明细",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(CommCommentItem.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		if(count>0){
			ids.stream().forEach(id->{
				commentService.clearItem(id);
			});
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


	@Override
 	@Action(title="加载评论明细子级",type = ActionType.Query,nolog = true)
	public Results<List<CommCommentItem>> children(Long pid){
		 //参数处理
		AssertUtil.service().notNull(pid, "参数pid不能为空");
		
		// 执行查询
		CommCommentItem params = new CommCommentItem();
		params.setParentId(pid);
		params.setDelFlag(0);
		params.setStatus(1);
		List<CommCommentItem> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}

}
