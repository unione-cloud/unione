package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.LinkEntity;
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
import com.unione.cloud.system.model.SysOnlineDoc;
import com.unione.cloud.system.model.SysOnlineDocItem;
import com.unione.cloud.system.service.OnlineDocService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysOnlineDocItem Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-18 18:43:24
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：在线文档内容",description="SysOnlineDocItem")
@RequestMapping("/api/system/onlineDoc/item")	 //TreeFeignApi
public class SysOnlineDocItemController implements TreeFeignApi<SysOnlineDocItem>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private OnlineDocService onlineDocService;
	
	@PostMapping("/load")
	@Operation(summary="加载在线文档内容",description="根据文档ID加载文档内容")
	@Action(title="加载在线文档内容",type = ActionType.Query)
	public Results<List<SysOnlineDocItem>> load(@RequestBody Params<SysOnlineDocItem> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空")
			.notNull(params.getBody().getDocId(),"文档ID不能为空");
		params.getBody().setDelFlag(0);
				
		Results<List<SysOnlineDocItem>> results = dataBaseDao.findPages(SqlBuilder.build(params)
			.field("id,parentId,title,icon,picMax,picMid,picMix,ordered,isLeaf")
			.link(LinkEntity.build("docId", SysOnlineDoc.class).in("status", Arrays.asList(new Integer[]{3,4}))));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}

	@PostMapping("/view")
	@Operation(summary="加载在线文档内容",description="根据文档ID加载文档内容")
	@Action(title="加载在线文档内容",type = ActionType.Query)
	public Results<String> view(@RequestBody Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		String content = onlineDocService.loadDocContent(id);
		return Results.success(content);
	}
	
	@Override
	@Action(title="查询在线文档内容",type = ActionType.Query)
	public Results<List<SysOnlineDocItem>> find(Params<SysOnlineDocItem> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		params.getBody().setDelFlag(0);
				
		Results<List<SysOnlineDocItem>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存在线文档内容",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) SysOnlineDocItem entity) {
		SysOnlineDoc doc = dataBaseDao.findById(SqlBuilder.build(SysOnlineDoc.class,entity.getDocId()));
			AssertUtil.service().notNull(doc, "文档记录未找到")
				.isTrue(doc.getStatus()!=4, "已归档文档不能修改");
				
		// 参数处理
		int len = 0;
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "parentId",-1L);
		BeanUtils.setDefaultValue(entity, "isLeaf",1);
		if(entity.getId()==null) {
			if(entity.getParentId()!=-1){
				SysOnlineDocItem parent = dataBaseDao.findById(SqlBuilder.build(SysOnlineDocItem.class,entity.getParentId()));
				AssertUtil.service().notNull(parent, "上级记录未找到");
				if(parent.getIsLeaf()==1){
					parent.setIsLeaf(0);
					dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
				}
			}

			entity.setDelFlag(0);
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"title","iconName","picMax","picMid","picMix","contents","ordered","descs"};
			SqlBuilder<SysOnlineDocItem> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载在线文档内容列表",type = ActionType.Query,nolog = true)
	public Results<List<SysOnlineDocItem>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysOnlineDocItem> rows = dataBaseDao.findByIds(SqlBuilder.build(SysOnlineDocItem.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载在线文档内容详情",type = ActionType.Query,nolog = true)
	public Results<SysOnlineDocItem> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysOnlineDocItem tmp = dataBaseDao.findById(SqlBuilder.build(SysOnlineDocItem.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除在线文档内容",type = ActionType.Delete,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysOnlineDocItem.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


	@Override
 @Action(title="加载在线文档内容子级",type = ActionType.Query,nolog = true)
	public Results<List<SysOnlineDocItem>> children(Long pid){
		 //参数处理
		AssertUtil.service().notNull(pid, "参数pid不能为空");
		
		// 执行查询
		SysOnlineDocItem params = new SysOnlineDocItem();
		params.setParentId(pid);
		List<SysOnlineDocItem> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}

}
