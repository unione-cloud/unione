package com.unione.cloud.form.page.api;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.page.dto.PageDefine;
import com.unione.cloud.form.page.dto.PageDefine.FormPageDefine;
import com.unione.cloud.form.page.dto.PageDefine.ListPageDefine;
import com.unione.cloud.form.page.model.SysPageDefine;
import com.unione.cloud.form.page.service.PageDefineService;
import com.unione.cloud.form.security.UserFormRoles;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysPageDefine Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:44:00
 * @版本	1.0.0
 **/
@Slf4j
@RefreshScope
@RestController
@Tag(name = "页面管理：页面定义",description="SysPageDefine")
@RequestMapping("/api/page/define")	 //TreeFeignApi
public class SysPageDefineController implements FeignDelete<SysPageDefine>,FeignFind<SysPageDefine>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private PageDefineService pageDefineService;
	
	
	@Override
	public Results<List<SysPageDefine>> find(Params<SysPageDefine> params) {
		log.debug("进入:查询页面定义列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询页面定义列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空")
			.isTrue(sessionService.hasRole(UserFormRoles.FORM_ADMIN,
					UserFormRoles.FORM_CONFIG,
					UserFormRoles.FORM_DEV), "当前帐号无权限");
				
		Results<List<SysPageDefine>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询页面定义列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}

	@PostMapping(value="/save")
	@Operation(description="保存页面定义")
	public Results<SysPageDefine> save(@Validated(Validator.save.class) @RequestBody PageDefine entity) {
		return pageDefineService.saveDefine(entity);
	}
	

	@PostMapping(value="/load/{sn}")
	@Operation(description="加载页面信息")
	public Results<SysPageDefine> load(@PathVariable("sn") String sn) {
		log.debug("进入:加载页面信息方法，sn:{}",sn);
		LogsUtil.set(LogType.Query, "加载页面信息");
		// 参数处理
		AssertUtil.service().notNull(sn,"参数sn不能为空");
		LogsUtil.setExtData(sn);
		
		LogsUtil.add("查找记录");
		SysPageDefine param=SysPageDefine.builder().sn(sn).build();
		SysPageDefine tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		AssertUtil.service().notNull(tmp, "页面信息未找到","404");
		LogsUtil.setTarget(tmp.getId());
		
		if("unione-page-form".equals(tmp.getComponent())) {
			FormPageDefine pageDefine=new FormPageDefine();
			BeanUtils.copy(tmp, pageDefine);
			tmp=pageDefine;
		}else if("unione-page-list".equals(tmp.getComponent())) {
			ListPageDefine pageDefine=new ListPageDefine();
			BeanUtils.copy(tmp, pageDefine);
			tmp=pageDefine;
		}
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:加载页面信息方法，sn:{},result:true",sn);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除页面定义信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除页面定义");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysPageDefine.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除页面定义信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


}
