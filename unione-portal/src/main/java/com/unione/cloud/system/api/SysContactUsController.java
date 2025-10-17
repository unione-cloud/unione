package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.system.model.SysAppInfo;
import com.unione.cloud.system.model.SysContactUs;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysContactUs Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 15:10:27
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：联系我们",description="SysContactUs")
@RequestMapping("/api/system/contactUs")	 //TreeFeignApi
public class SysContactUsController implements PojoFeignApi<SysContactUs>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="查询联系我们",type = ActionType.Query)
	public Results<List<SysContactUs>> find(Params<SysContactUs> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysContactUs>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存联系我们",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysContactUs entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			entity.setRegSts(0);
			entity.setTrailSts(1);
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"company","name","post","email","tel","subject","message","regSts","regTime","trailSts","trailUid","trailUname","trailInfos","delFlag","descs"};
			SqlBuilder<SysContactUs> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置跟进状态",type = ActionType.Save,roles = {UserRoles.SYSOPSUSER,UserRoles.SUPPERADMIN})
	@Operation(summary = "设置跟进状态", description="CUSTRAILSTS 1：待跟进，2：跟进中，3：已处理")
	public Results<Void> setStatus(@RequestBody SysContactUs entity){
		AssertUtil.service().notNull(entity, new String[] {"id","trailSts"},"属性%s不能为空")
			.notIn(entity.getTrailSts(), Arrays.asList(1,2,3), "参数trailSts取值范围[1,2,3]");
		
		List<String> fields=new ArrayList<>(Arrays.asList("trailSts"));
		if(entity.getTrailSts()==2 || entity.getTrailSts()==3) {
			entity.setTrailUid(sessionService.getUserId());
			entity.setTrailUname(sessionService.getRealname());
			fields.add("trailUid");
			fields.add("trailUname");
		}
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields.toArray(new String[0])));
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载联系我们列表",type = ActionType.Query,nolog = true)
	public Results<List<SysContactUs>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysContactUs> rows = dataBaseDao.findByIds(SqlBuilder.build(SysContactUs.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载联系我们详情",type = ActionType.Query,nolog = true)
	public Results<SysContactUs> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysContactUs tmp = dataBaseDao.findById(SqlBuilder.build(SysContactUs.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除联系我们",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(SysContactUs.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


//	@Override
//  @Action(title="加载联系我们子级",type = ActionType.Query,nolog = true)
//	public Results<List<SysContactUs>> children(Long pid){
//		 //参数处理
//		AssertUtil.service().notNull(pid, "参数pid不能为空");
//		
//		// 执行查询
//		SysContactUs params = new SysContactUs();
//		params.setParentId(pid);
//		List<SysContactUs> rows = dataBaseDao.findList(SqlBuilder.build(params));
//		
//		return Results.success(rows);
//	}

}
