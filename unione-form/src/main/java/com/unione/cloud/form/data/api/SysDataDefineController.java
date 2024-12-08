package com.unione.cloud.form.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.security.UserFormRoles;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysDataDefine Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "数据管理：数据定义管理",description="SysDataModel")
@RequestMapping("/api/data/define")	 //TreeFeignApi
public class SysDataDefineController implements FeignDelete<SysDataDefine>,FeignFind<SysDataDefine>,FeignFindById<SysDataDefine>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	@Value("${form.page.default.appid:1000}")
	private Long DEFAULT_APP_ID;
	
	
	@Override
	public Results<List<SysDataDefine>> find(Params<SysDataDefine> params) {
		log.debug("进入:查询数据定义管理列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询数据定义管理列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysDataDefine>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询数据定义管理列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	
	@PostMapping(value="/save")
	@ApiOperation(value="保存数据定义")
	public Results<DataDefine> save(@Validated(Validator.save.class) @RequestBody DataDefine dataDefine) {
		log.debug("进入:新增数据定义管理信息.dataDefine:{}",dataDefine);
		LogsUtil.set(LogType.Insert, "新增数据定义管理");
		AssertUtil.service().isTrue(sessionService.hasRole(UserFormRoles.FORM_ADMIN,
				UserFormRoles.FORM_CONFIG,
				UserFormRoles.FORM_DEV), "当前帐号无权限");
		if("new_".equals(dataDefine.getSn())) {
			dataDefine.setSn(null);
		}
		
		SysDataDefine tmp = null;
		if(!StringUtils.isEmpty(dataDefine.getSn())) {
			SysDataDefine param=SysDataDefine.builder().sn(dataDefine.getSn()).build();
			param.setTenantId(sessionService.getTenantId());
			tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		}
		
		if(tmp!=null) {
			dataDefine.setId(tmp.getId());
			// 更新
			String[] fields = {"dirId","dsId","title","name","isCustom","category","sqlFind","sqlInsert","sqlUpdate","sqlDelete","url","syncFlag","fields","settings","ordered","status","descs"};
			SqlBuilder<SysDataDefine> sqlBuilder=SqlBuilder.build((SysDataDefine)dataDefine).field(fields);
			int len = dataBaseDao.updateById(sqlBuilder);
			AssertUtil.service().isTrue(len>0, "数据定义保存失败");
		}else {
			// 新增
			// 参数处理
			if(StringUtils.isEmpty(dataDefine.getSn())) {
				dataDefine.setSn(RandomUtil.randomString(20));
			}
			BeanUtils.setDefaultValue(dataDefine, "appId", DEFAULT_APP_ID);
			BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
			BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
			BeanUtils.setDefaultValue(dataDefine, "status",1);
			BeanUtils.setDefaultValue(dataDefine, "ordered",0);
			BeanUtils.setDefaultValue(dataDefine, "configs","{}");
			dataDefine.setVers(1);
			
			int len = dataBaseDao.insert(dataDefine);
			AssertUtil.service().isTrue(len>0, "数据定义保存失败");
		}
		
		LogsUtil.success(dataDefine.getId());
		log.debug("退出:新增数据定义管理信息.entity:{},result:true",dataDefine);
		return Results.success(dataDefine);
	}


	@Override
	public Results<List<SysDataDefine>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询数据定义管理信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询数据定义管理");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysDataDefine> rows = dataBaseDao.findByIds(SqlBuilder.build(SysDataDefine.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询数据定义管理信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@PostMapping(value="/load/{sn}")
	@ApiOperation(value="加载数据定义")
	public Results<SysDataDefine> load(@PathVariable("sn") String sn) {
		log.debug("进入:加载数据定义方法，sn:{}",sn);
		LogsUtil.set(LogType.Query, "加载数据定义");
		// 参数处理
		AssertUtil.service().notNull(sn,"参数sn不能为空");
		LogsUtil.setExtData(sn);
		
		LogsUtil.add("查找记录");
		SysDataDefine param=SysDataDefine.builder().sn(sn).build();
		SysDataDefine tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		AssertUtil.service().notNull(tmp, "页面信息未找到","404");
		LogsUtil.setTarget(tmp.getId());
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:加载数据定义方法，sn:{},result:true",sn);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Set<Long> ids){
		log.debug("进入:删除数据定义管理信息方法，ids:{}",ids);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除数据定义管理");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(SysDataDefine.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody((long)count);
		LogsUtil.save(count>0);

		log.debug("退出:删除数据定义管理信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


}
