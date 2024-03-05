package com.unione.cloud.portal.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.SqlBuilder;
import com.unione.cloud.beetsql.Updater;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.portal.system.model.SysUser;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 SysUser Controller 服务
 * @作者	Jeking Yang
 * @日期	2023-8-31 0:00:34
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "系统管理：用户信息 管理服务",description="SysUser")
@RequestMapping("/api/user")
public class SysUserController implements PojoFeignApi<SysUser>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Override
	public Results<List<SysUser>> find(Params<SysUser> params) {
		log.debug("进入控制:查询系统用户列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询系统用户列表");
		
		SqlBuilder<SysUser> builder=SqlBuilder.build(params);
		Results<List<SysUser>> results = dataBaseDao.findPages(builder);
				
		LogsUtil.add("分页数据查询，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出控制:查询系统用户列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysUser entity) {
		log.debug("进入控制:新增系统用户信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增系统用户");
		// 参数处理
		dataBaseDao.insert(entity);
		
		LogsUtil.success(entity.getId());
		log.debug("退出控制:新增系统用户信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


	@Override
	public Results<Long> update(@Validated(Validator.update.class) SysUser entity) {
		log.debug("进入控制:修改系统用户信息方法，entity:{}",entity);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Modify, "修改系统用户",entity.getId());
		
		String[] fields = {"userType","username","pwdText","pwdSalt","realName","aliasName","portrait","birthday","sex","email","qq","tel","securityQuestion","sucurityMfa","lastLoginTime","lastLoginIp","status","lockTime","descs"};
		
		int len = dataBaseDao.updateById(Updater.build(entity).fields(fields));
		LogsUtil.add("保存数据,len:"+len);
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		LogsUtil.save(len>0, entity.getId());

		log.debug("退出控制:修改系统用户信息方法，entity:{},result:{}",entity,results.isSuccess());
		return results;
	}



	@Override
	public Results<List<SysUser>> findByIds(Set<Long> ids) {
		log.debug("进入控制:批量查询系统用户信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询系统用户");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 参数处理
		SysUser entity=new SysUser();
		entity.setIds(new ArrayList<>(ids));
		
		List<SysUser> rows= dataBaseDao.findByIds(entity);
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出控制:批量查询系统用户信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysUser> detail(Long sid) {
		log.debug("进入控制:查看系统用户详细信息方法，sid:{}",sid);
		LogsUtil.set(LogType.Query, "查看系统用户详细",sid);
		// 参数处理
		AssertUtil.service().notNull(sid,"参数sid不能为空");
		
		// 参数处理
		SysUser entity=new SysUser();
		entity.setId(sid);
		
		LogsUtil.add("查找记录");
		SysUser tmp = dataBaseDao.findById(entity);
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出控制:查看系统用户详细信息方法，sid:{},result:true",sid);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Set<Long> ids){
		log.debug("进入控制:删除系统用户信息方法，ids:{}",ids);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除系统用户");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 参数处理
		SysUser entity=new SysUser();
		entity.setIds(new ArrayList<>(ids));
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(entity.getIds()));
		int count = dataBaseDao.delete(entity);
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody((long)count);
		LogsUtil.save(count>0);

		log.debug("退出控制:删除系统用户信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}



}
