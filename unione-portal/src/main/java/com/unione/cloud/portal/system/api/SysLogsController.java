package com.unione.cloud.portal.system.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignSave;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.web.logs.model.SysLogs;

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
@Api(tags = "基础服务：操作日志 管理服务",description="SysLogs")
@RequestMapping("/api/logs")
public class SysLogsController implements FeignSave<SysLogs>,FeignFind<SysLogs>,FeignDetail<SysLogs>{
	/**
	 * 数据访问对象
	 */
	@Autowired
	private DataBaseDao dataBaseDao;

	
	@Override
	public Results<List<SysLogs>> find(Params<SysLogs> params) {
		log.debug("进入:查询系统日志列表方法,params:{}",params);
		
		// 构造sql
		SqlBuilder<SysLogs> builder=SqlBuilder.build(params)
			.where("appCode=? AND actionId=? AND requestId=? AND userName like [%?%] AND "
					+ "title like [%?%] AND types=? AND targetId=? AND status=? AND "
					+ "startTime>[timeBegin] AND startTime<=[timeEnd]");
		// 执行查询
		Results<List<SysLogs>> results=dataBaseDao.findPages(builder);
		
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
	public Results<SysLogs> detail(Long id) {
		log.debug("进入:查看系统日志详细信息方法，id:{}",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysLogs tmp = dataBaseDao.findById(SysLogs.class,id);
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		log.debug("退出:查看系统日志详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	



}
