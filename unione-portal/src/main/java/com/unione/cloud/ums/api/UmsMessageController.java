package com.unione.cloud.ums.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.ums.dto.UmsMessageDto;
import com.unione.cloud.ums.model.UmsMessage;
import com.unione.cloud.ums.service.UmsMessageService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	UmsMessage Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "统一消息：消息",description="UmsMessage")
@RequestMapping("/api/ums/message")	 //TreeFeignApi
public class UmsMessageController implements FeignDelete<UmsMessage>,FeignFind<UmsMessage>,FeignFindById<UmsMessage>,FeignDetail<UmsMessage>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UmsMessageService umsMessageService;
	
	
	@Override
	@Action(title="查询统一消息",type = ActionType.Query)
	public Results<List<UmsMessage>> find(Params<UmsMessage> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		params.getBody().setDelFlag(0);		
		params.getBody().setUserId(sessionService.getUserId());
		Results<List<UmsMessage>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@PostMapping("/save")
	@Operation(summary = "保存",description = "新增/更新")
	@Action(title="保存统一消息",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) UmsMessageDto entity) {
		Results<Long> results=umsMessageService.save(entity);
		if(results.isSuccess()){
			umsMessageService.send(entity.getId());
		}
		return results;
	}

	@Override
	@Action(title="加载统一消息列表",type = ActionType.Query,nolog = true)
	public Results<List<UmsMessage>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<UmsMessage> rows = dataBaseDao.findByIds(SqlBuilder.build(UmsMessage.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载统一消息详情",type = ActionType.Query,nolog = true)
	public Results<UmsMessage> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		UmsMessage tmp = dataBaseDao.findById(SqlBuilder.build(UmsMessage.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除统一消息",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(UmsMessage.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}



}
