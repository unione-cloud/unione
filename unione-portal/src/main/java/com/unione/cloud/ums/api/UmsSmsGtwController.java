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
import com.unione.cloud.ums.model.UmsSmsGtw;
import com.unione.cloud.ums.model.UmsTmpl;
import com.unione.cloud.ums.service.UmsSmsService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UmsSmsGtw Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-22 12:52:46
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "统一消息:短信网关",description="UmsSmsGtw")
@RequestMapping("/api/ums/smsGtw")	 //TreeFeignApi
public class UmsSmsGtwController implements PojoFeignApi<UmsSmsGtw>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private UmsSmsService smsService;
	
	
	@Override
	@Action(title="查询短信网关",type = ActionType.Query)
	public Results<List<UmsSmsGtw>> find(Params<UmsSmsGtw> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<UmsSmsGtw>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存短信网关",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SYSOPSUSER})
	public Results<Long> save(@Validated(Validator.save.class) UmsSmsGtw entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			entity.setStatus(1);
			entity.setDelFlag(0);
			len = dataBaseDao.insert(entity);
		}else {
			UmsSmsGtw gtw = dataBaseDao.findById(SqlBuilder.build(UmsSmsGtw.class,entity.getId()));
			AssertUtil.service().notNull(gtw, "记录未找到");

			String[] fields = {"title","ip","ports","url","authInfo","authApi","authScript","sendApi","sendScript","receiveApi","receiveScript","receiveCron","receiptApi","receiptScript","receiptCron","descs","status","ordered"};
			SqlBuilder<UmsSmsGtw> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
			if(len>0){
				smsService.delGtw(gtw.getSn());
			}
		}
		return Results.build(len>0, entity.getId());
	}
	
	@PostMapping("/status")
	@Action(title="设置网关状态",type = ActionType.Save,roles = {UserRoles.TENANTADMIN,UserRoles.SYSOPSUSER})
	@Operation(summary = "设置网关状态", description="USEORNOT 1 使用，0停用")
	public Results<Void> setStatus(@RequestBody UmsSmsGtw entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");
		
		UmsSmsGtw gtw = dataBaseDao.findById(SqlBuilder.build(UmsSmsGtw.class,entity.getId()));
		AssertUtil.service().notNull(gtw, "记录未找到");

		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		if(len>0){
			smsService.delGtw(gtw.getSn());
		}
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载短信网关列表",type = ActionType.Query,nolog = true)
	public Results<List<UmsSmsGtw>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<UmsSmsGtw> rows = dataBaseDao.findByIds(SqlBuilder.build(UmsSmsGtw.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载短信网关详情",type = ActionType.Query,nolog = true)
	public Results<UmsSmsGtw> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		UmsSmsGtw tmp = dataBaseDao.findById(SqlBuilder.build(UmsSmsGtw.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除短信网关",type = ActionType.Delete,roles = {UserRoles.TENANTADMIN,UserRoles.SYSOPSUSER})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");

		List<UmsSmsGtw> rows = dataBaseDao.findByIds(SqlBuilder.build(UmsSmsGtw.class,new ArrayList<>(ids)));
		AssertUtil.service().isTrue(!rows.isEmpty(), "记录未找到");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(UmsSmsGtw.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);

		// 删除缓存
		if(count>0){
			rows.forEach(item->smsService.delGtw(item.getSn()));
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
