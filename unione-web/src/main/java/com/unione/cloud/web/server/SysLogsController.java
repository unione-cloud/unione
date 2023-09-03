package com.unione.cloud.web.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Finder;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignSave;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.web.model.SysLogs;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 SysLogs Controller 服务
 * 
 * @作者	Jeking Yang
 * @日期	2023年8月29日 下午11:29:27
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "系统管理：操作日志 管理服务",description="SysLogs")
@RequestMapping("/api/logs")
public class SysLogsController implements FeignSave<SysLogs>,FeignDelete<SysLogs>,FeignFind<SysLogs>,FeignDetail<SysLogs>{
	/**
	 * 数据访问对象
	 */
	@Autowired
	private DataBaseDao dataBaseDao;

	/**
	 * 用户会话对象
	 */
	@Autowired
	private SessionService sessionService;

	
	@Override
	public Results<List<SysLogs>> find(Params<SysLogs> params) {
		log.debug("进入:查询系统日志列表方法,params:{}",params);
		// 参数处理
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				params.getBody().setOrgId(sessionService.getOrgId());
			}
		}
		
		// 构造sql
		SqlBuilder<SysLogs> builder=SqlBuilder.build(params)
			.field("id,name,sex,age")
			.where("name=? and age>? and realname like ? and time>#{timeBegin} and time<=#{timeEnd}");
		// 执行查询
		Results<List<SysLogs>> results=dataBaseDao.findListByPage(builder);
		
		log.debug("退出:查询系统日志列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysLogs entity) {
		log.debug("进入:新增系统日志信息.entity:{}",entity);
		
		dataBaseDao.insert(entity);
		
		log.debug("退出:新增系统日志信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


	@Override
	public Results<SysLogs> detail(Long sid) {
		log.debug("进入:查看系统日志详细信息方法，sid:{}",sid);
		// 参数处理
		AssertUtil.service().notNull(sid,"参数sid不能为空");
		
		// 参数处理
		SysLogs entity=new SysLogs();
		entity.setId(sid);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
			entity.setTenantId(sessionService.getTenantId());
			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		SysLogs tmp = dataBaseDao.findById(entity);
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		log.debug("退出:查看系统日志详细信息方法，sid:{},result:true",sid);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Params<List<Long>> params){
		log.debug("进入:删除系统日志信息方法，params:{}",params);
		// 参数处理
		AssertUtil.service().isTrue(params.getBody()!=null && !params.getBody().isEmpty(), "参数body不能为空");
		
		// 参数处理
		SysLogs entity=new SysLogs();
		entity.setIds(params.getBody());
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
			entity.setTenantId(sessionService.getTenantId());
			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		// 执行删除
		int count = dataBaseDao.deleteLogicById(entity);

		log.debug("退出:删除系统日志信息方法，params:{}",params);
		return Results.build(count>0, (long)count);
	}



}
