package com.unione.cloud.ums.api;

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
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.ums.model.UmsTmpl;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UmsTmpl Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "统一消息:模版",description="根据模版编码，通知方式自动发送各种消息")
@RequestMapping("/api/ums/tmpl")	 //TreeFeignApi
public class UmsTmplController implements PojoFeignApi<UmsTmpl>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询消息模版",type = ActionType.Query)
	public Results<List<UmsTmpl>> find(Params<UmsTmpl> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		params.getBody().setDelFlag(0);			
		Results<List<UmsTmpl>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存消息模版",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) UmsTmpl entity) {
		// 参数处理
		int len = 0;
		BeanUtils.setDefaultValue(entity, "sn",RandomUtil.randomString(10));
		BeanUtils.setDefaultValue(entity, "ways","site");
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "delFlag",0);
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "usel",1);

		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"types","title","bodyHtml","bodySms","vars","usel","ways","status","ordered"};
			SqlBuilder<UmsTmpl> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@PostMapping("/status")
	@Action(title="设置模版状态",type = ActionType.Save,roles = {UserRoles.SYSOPSUSER})
	@Operation(summary = "设置模版状态", description="USEORNOT 1 使用，0停用")
	public Results<Void> setStatus(@RequestBody UmsTmpl entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载消息模版列表",type = ActionType.Query,nolog = true)
	public Results<List<UmsTmpl>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<UmsTmpl> rows = dataBaseDao.findByIds(SqlBuilder.build(UmsTmpl.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载消息模版详情",type = ActionType.Query,nolog = true)
	public Results<UmsTmpl> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		UmsTmpl tmp = dataBaseDao.findById(SqlBuilder.build(UmsTmpl.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除消息模版",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(UmsTmpl.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
