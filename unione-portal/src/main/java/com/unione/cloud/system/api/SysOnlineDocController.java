package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.TreeNodeDto;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.dto.OnlineDocDto;
import com.unione.cloud.system.model.SysOnlineDoc;
import com.unione.cloud.system.model.SysOnlineDocItem;
import com.unione.cloud.system.service.OnlineDocService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysOnlineDoc Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-18 18:43:24
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：在线文档",description="SysOnlineDoc")
@RequestMapping("/api/system/onlineDoc")	 //TreeFeignApi
public class SysOnlineDocController implements PojoFeignApi<SysOnlineDoc>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private OnlineDocService onlineDocService;
	

	@PostMapping("/load")
	@Operation(summary="加载在线文档",description="appId不能为空")
	@Action(title="加载在线文档",type = ActionType.Query)
	public Results<List<SysOnlineDoc>> load(@RequestBody Params<SysOnlineDoc> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空")
			.notNull(params.getBody().getAppId(),"appId不能为空");
		if(params.getPageSize()>10){
			params.setPageSize(10);
		}
		
		Results<List<SysOnlineDoc>> results = dataBaseDao.findPages(SqlBuilder.build(params)
			.field("id,title,versNo,icon,picMax,picMid,picMix,profile,ordered")
			.where("appId=? and delFlag=0 and status in (3,4)"));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}

	@PostMapping("/view")
	@Operation(summary="查看在线文档",description="appId不能为空")
	@Action(title="查看在线文档",type = ActionType.Query)
	public Results<OnlineDocDto> view(@RequestBody SysOnlineDoc params) {
		AssertUtil.service().isTrue(params.getAppId()!=null || params.getId()!=null,"appId和docId不能都为空");
		OnlineDocDto doc=null;
		if(params.getId()!=null){
			doc=onlineDocService.loadDocByDocId(params.getId());
		}else{
			doc=onlineDocService.loadDocByAppId(params.getAppId());
		}
		AssertUtil.service().notNull(doc, "记录未找到");
		return Results.success(doc);
	}
		
	
	@Override
	@Action(title="查询在线文档",type = ActionType.Query)
	public Results<List<SysOnlineDoc>> find(Params<SysOnlineDoc> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		params.getBody().setDelFlag(0);
		
		Results<List<SysOnlineDoc>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存在线文档",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) SysOnlineDoc entity) {
		// 参数处理
		int len = 0;
		BeanUtils.setDefaultValue(entity, "ordered",0);
		if(entity.getId()==null) {
			entity.setDelFlag(0);
			entity.setStatus(1);
			len = dataBaseDao.insert(entity);
		}else {
			SysOnlineDoc doc = dataBaseDao.findById(SqlBuilder.build(SysOnlineDoc.class,entity.getId()));
			AssertUtil.service().notNull(doc, "记录未找到")
				.isTrue(doc.getStatus()!=4, "已归档文档不能修改");

			String[] fields = {"title","versNo","iconName","picMax","picMid","picMix","profile","ordered","descs"};
			SqlBuilder<SysOnlineDoc> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
			if(len>0){
				onlineDocService.refreshDoc(doc);
			}
		}
		return Results.build(len>0, entity.getId());
	}

	@PostMapping("/status")
	@Action(title="设置应用状态",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	@Operation(summary = "设置应用状态", description="1：编制中，2：内审中，3：已发布，4：已归档")
	public Results<Void> setStatus(@RequestBody SysOnlineDoc entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(1,2,3,4), "参数status取值范围[1,2,3,4]");
		
		SysOnlineDoc tmp = dataBaseDao.findById(SqlBuilder.build(SysOnlineDoc.class,entity.getId()).field("id,appId,status"));
		AssertUtil.service().notNull(tmp, "记录未找到");

		List<String> fields=new ArrayList<>();
		fields.add("status");
		if(entity.getStatus()==3){
			fields.add("releaseTime");
			entity.setReleaseTime(DateUtil.date());
		}else if(entity.getStatus()==4){
			fields.add("archiveTime");
			entity.setArchiveTime(DateUtil.date());
		}
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields.toArray(new String[0])));
		if(len>0 && (entity.getStatus()==3 || entity.getStatus()==4)){
			onlineDocService.refreshDoc(tmp);
		}
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载在线文档列表",type = ActionType.Query,nolog = true)
	public Results<List<SysOnlineDoc>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysOnlineDoc> rows = dataBaseDao.findByIds(SqlBuilder.build(SysOnlineDoc.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载在线文档详情",type = ActionType.Query,nolog = true)
	public Results<SysOnlineDoc> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysOnlineDoc tmp = dataBaseDao.findById(SqlBuilder.build(SysOnlineDoc.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除在线文档",type = ActionType.Delete,roles = {UserRoles.TENANTADMIN,UserRoles.SUPPERADMIN,UserRoles.ONLINEDEV,UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysOnlineDoc.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载在线文档子级",type = ActionType.Query,nolog = true)
//	public Results<List<SysOnlineDoc>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		SysOnlineDoc params = new SysOnlineDoc();
//		params.setParentId(pid);
//		List<SysOnlineDoc> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
