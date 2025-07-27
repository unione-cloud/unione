package com.unione.cloud.ums.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
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
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.ums.dto.UmsMessageDto;
import com.unione.cloud.ums.dto.UmsMessageMine;
import com.unione.cloud.ums.dto.UmsMessageSend;
import com.unione.cloud.ums.model.UmsCategory;
import com.unione.cloud.ums.model.UmsMessage;
import com.unione.cloud.ums.model.UmsMessageStatus;
import com.unione.cloud.ums.model.UmsMessageTarget;
import com.unione.cloud.ums.model.UmsMessageWay;
import com.unione.cloud.ums.service.UmsMessageService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
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
public class UmsMessageController implements FeignDelete<UmsMessage>,FeignFind<UmsMessageDto>,FeignFindById<UmsMessage>,FeignDetail<UmsMessage>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UmsMessageService umsMessageService;

	@PostMapping("/send")
	@Operation(summary = "发送消息",description = "")
	@Action(title="发送统一消息",type = ActionType.Save)
	public Results<Long> send(@RequestBody @Validated(Validator.save.class) UmsMessageSend entity) {
		Results<Long> results=umsMessageService.save(entity);
		if(results.isSuccess()){
			umsMessageService.send(entity.getId());
		}
		return results;
	}
	
	@PostMapping("/mine")
	@Operation(summary = "我的消息",description = "")
	public Results<List<UmsMessageMine>> mine(@RequestBody Params<UmsMessageMine> params) {
		Results<List<UmsMessageMine>> results=umsMessageService.mine(params);
		if(results.getBody()!=null){
			Set<Long> cids = results.getBody().stream().map(r->r.getCategoryId()).filter(i->i!=null).collect(Collectors.toSet());
			if(!cids.isEmpty()){
				Map<String,UmsCategory> map = dataBaseDao.findMap(SqlBuilder.build(UmsCategory.class, cids), "id");
				results.getBody().stream().filter(r->r.getCategoryId()!=null).forEach(r->{
					UmsCategory category=map.get(r.getCategoryId().toString());
					if(category!=null){
						r.setCategoryName(category.getTitle());
					}
				});
			}
		}
		return results;
	}

	@PostMapping("/viwe")
	@Operation(summary = "查看消息",description = "获取消息详情，并更新消息状态，参数id不能为空")
	public Results<UmsMessageMine> viwe(@RequestBody UmsMessageMine mine) {

		UmsMessageMine tmp = dataBaseDao.findOne("loadMine",SqlBuilder.build(UmsMessageMine.class,mine.getId()));
		AssertUtil.service().notNull(tmp, "记录未找到");
		if(tmp.getMineId()==null){
			// 保存消息状态
			UmsMessageStatus status=new UmsMessageStatus();
			status.setMessageId(tmp.getId());
			status.setUserId(sessionService.getUserId());
			status.setDelFlag(0);
			status.setViewSts(1);
			status.setViewTime(DateUtil.date());
			dataBaseDao.insert(status);
			tmp.setMineId(status.getId());
			tmp.setViewSts(1);
			tmp.setViewTime(status.getViewTime());
		}

		return Results.success(tmp);
	}


	@PostMapping("/confirm")
	@Operation(summary = "确认消息",description = "参数id不能为空")
	public Results<Void> confirm(@RequestBody UmsMessageStatus entity) {
		AssertUtil.service().isTrue(!(entity.getId()==null&&entity.getMessageId()==null), "参数id和messageId不能都为空");

		BeanUtils.setDefaultValue(entity, "confirmStatus",1);
		if(entity.getId()==null && entity.getMessageId()!=null){
			// 加载消息对象
			UmsMessageMine tmp = dataBaseDao.findOne("loadMine",SqlBuilder.build(UmsMessageMine.class,entity.getMessageId()));
			AssertUtil.service().notNull(tmp, "记录未找到");

			// 保存消息状态
			if(tmp.getMineId()==null){
				// 保存消息状态
				UmsMessageStatus status=new UmsMessageStatus();
				status.setMessageId(tmp.getId());
				status.setUserId(sessionService.getUserId());
				status.setDelFlag(0);
				status.setViewSts(1);
				status.setViewTime(DateUtil.date());
				status.setConfirmDate(DateUtil.date());
				status.setConfirmResult(entity.getConfirmResult());
				status.setConfirmStatus(entity.getConfirmStatus());
				status.setReplyInfo(entity.getReplyInfo());
				int len = dataBaseDao.insert(status);
				return Results.build(len>0);
			}else{
				entity.setId(tmp.getMineId());
			}
		}
		AssertUtil.service().notNull(entity.getId(), "参数id不能为空");
		UmsMessageStatus status=new UmsMessageStatus();
		status.setId(entity.getId());
		status.setUserId(sessionService.getUserId());
		status=dataBaseDao.findOne(SqlBuilder.build(status));
		AssertUtil.service().notNull(status, "记录未找到");

		status.setViewSts(1);
		status.setConfirmDate(DateUtil.date());
		status.setConfirmResult(entity.getConfirmResult());
		status.setConfirmStatus(entity.getConfirmStatus());
		status.setReplyInfo(entity.getReplyInfo());
		int len = dataBaseDao.updateById(SqlBuilder.build(status).field("viewSts","confirmDate","confirmResult","confirmStatus","replyInfo"));

		return Results.build(len>0);
	}

	@PostMapping("/remove")
	@Operation(summary = "删除我的消息",description = "参数ids不能为空")
	@Action(title="删除我的消息",type = ActionType.Delete)
	public Results<Integer> remove(@RequestBody Set<Long> ids){
		Results<Integer> results = new Results<>();
		AssertUtil.service().notEmpty(ids, "参数ids不能为空");
		AtomicInteger total=new AtomicInteger();

		Params<UmsMessageMine> params=new Params<>();
        params.setBody(new UmsMessageMine());
        params.setIds(new ArrayList<>(ids));
        params.setPage(1);
        params.setPageSize(2000);
        List<UmsMessageMine> list=dataBaseDao.findPageList("loadMine",params);

		Set<Long> mineIds = list.stream().map(r->r.getMineId()).filter(r->r!=null).collect(Collectors.toSet());
		// 删除我的消息
		if(ObjectUtil.isNotEmpty(mineIds)){
			LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(mineIds));
			int len = dataBaseDao.deleteLogicById(SqlBuilder.build(UmsMessageStatus.class,mineIds));
			LogsUtil.add("成功删除我的:"+len);
			total.addAndGet(len);
		}

		// 根据消息id删除
		Set<Long> msgIds=list.stream().filter(r->r.getMineId()==null).map(UmsMessageMine::getId).collect(Collectors.toSet());
		if(ObjectUtil.isNotEmpty(msgIds)){
			List<UmsMessageStatus> stsList = msgIds.stream().map(id->{
 				UmsMessageStatus sts=new UmsMessageStatus();
                sts.setMessageId(id);
                sts.setUserId(sessionService.getUserId());
                sts.setDelFlag(1);
                sts.setViewSts(0);
                return sts;
            }).collect(Collectors.toList());
			 if(!stsList.isEmpty()){
                int t[] = dataBaseDao.insertBatch(stsList);
                Arrays.asList(t).stream().filter(l->(Integer)l>0).forEach(i->total.addAndGet((Integer)i));
            }
		}
		
		results.setSuccess(total.get()>0);
		results.setMessage(total.get()>0?"操作成功":"操作失败");
		results.setBody(total.get());
		return results;
	}


	@PostMapping("/clear")
	@Action(title="标记所有消息已读",type = ActionType.Other)
	@Operation(summary = "标记所有消息已读",description = "将所有未读消息设置成已读")
	public Results<Integer> clear(){
		return umsMessageService.clear();
	}

	
	@Override
	@Action(title="查询统一消息",type = ActionType.Query)
	public Results<List<UmsMessageDto>> find(Params<UmsMessageDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
		params.getBody().setDelFlag(0);		
		params.getBody().setUserId(sessionService.getUserId());
		Results<List<UmsMessageDto>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());

		if(results.getBody()!=null){

			// 加载通知类别
			Set<Long> cids = results.getBody().stream().map(r->r.getCategoryId()).filter(i->i!=null).collect(Collectors.toSet());
			if(!cids.isEmpty()){
				Map<String,UmsCategory> map = dataBaseDao.findMap(SqlBuilder.build(UmsCategory.class, cids), "id");
				results.getBody().stream().filter(r->r.getCategoryId()!=null).forEach(r->{
					UmsCategory category=map.get(r.getCategoryId().toString());
					if(category!=null){
						r.setCategoryName(category.getTitle());
					}
				});
			}

			// 加载通知目标，通知方式
			Set<Long> mids=results.getBody().stream().map(r->r.getId()).filter(i->i!=null).collect(Collectors.toSet());
			if(!mids.isEmpty()){
				List<UmsMessageTarget> targetList = dataBaseDao.findList(SqlBuilder.build(UmsMessageTarget.class,mids).where("messageId in [ids]"));
				Map<Long,List<UmsMessageTarget>> targetMap = targetList.stream().collect(Collectors.groupingBy(UmsMessageTarget::getMessageId));
				
				List<UmsMessageWay> wayList=dataBaseDao.findList(SqlBuilder.build(UmsMessageWay.class,mids).where("messageId in [ids]"));
				Map<Long,List<UmsMessageWay>> wayMap = wayList.stream().collect(Collectors.groupingBy(UmsMessageWay::getMessageId));
				
				results.getBody().stream().forEach(r->{
					r.setTargets(targetMap.get(r.getId()));
					r.setWays(wayMap.get(r.getId()).stream().map(w->w.getWay()).collect(Collectors.toList()));
				});
			}
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
		AssertUtil.service().notNull(tmp, "记录未找到").notEq(tmp.getTenantId(), sessionService.getTenantId(), "记录未找到");
		
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
