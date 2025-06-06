package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.dto.SysConfigNodeDto;
import com.unione.cloud.system.model.SysConfigDefine;
import com.unione.cloud.system.service.CodeTreeService;
import com.unione.cloud.system.service.SysConfigService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysConfigDefine Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-04 08:29:01
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：统一配置",description="SysConfigDefine")
@RequestMapping("/api/system/configDefine")	 //TreeFeignApi
public class SysConfigDefineController implements TreeFeignApi<SysConfigDefine>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private CodeTreeService codeTreeService;

	@Autowired
	private SysConfigService sysConfigService;


	@PostMapping("/tree/{name}/{type}")
	@Action(title="加载统一配置",type = ActionType.Query)
	@Operation(summary = "加载统一配置", description = "参数：name=配置名称,name=root加载全部，type=-1全部,0全局，1租户级，2机构级，3用户级")
	public Results<List<SysConfigNodeDto>> tree(@PathVariable("name") String name,@PathVariable("type") Integer type){
		// 参数处理
		AssertUtil.service().isTrue(type>=-1 && type<=3, "参数type错误,有效值[-1,3]");
		List<SysConfigNodeDto> list = sysConfigService.loadList(name, type).stream().map(row->{
				SysConfigNodeDto dto = new SysConfigNodeDto();
				dto.setId(row.getId());
				dto.setPid(row.getParentId());
				dto.setTitle(row.getTitle());
				dto.setNtype(String.valueOf(row.getTypes()));
				dto.setSn(row.getSn());
				dto.setValueDefault(row.getValueDefault());
				dto.setValueUsed(row.getValueUsed());
				dto.setDescs(row.getDescs());
				return dto;
			}).collect(Collectors.toList());

		return Results.success(list).setTotal(list.size());
	} 

	@PostMapping("/setValue")
	@Action(title="设置配置value",type = ActionType.Save)
	@Operation(summary = "设置配置value", description = "参数name：配置名称，value：配置值")
	public Results<Void> setValue(String sn,String value){
		sysConfigService.setValue(sn, value);
		return Results.success();
	}
	
	
	@Override
	@Action(title="查询统一配置",type = ActionType.Query)
	public Results<List<SysConfigDefine>> find(Params<SysConfigDefine> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		params.getBody().setDelFlag(0);
		Results<List<SysConfigDefine>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存统一配置",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysConfigDefine entity) {
		// 参数处理
		BeanUtils.setDefaultValue(entity, "parentId",-1L);
		BeanUtils.setDefaultValue(entity, "types",0);
		BeanUtils.setDefaultValue(entity, "level",0);
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "status",0);
		BeanUtils.setDefaultValue(entity, "isAuth",0);
		BeanUtils.setDefaultValue(entity, "delFlag",0);
		SysConfigDefine parent = null;
		if(!Objects.equals(-1L, entity.getParentId())) {
			parent = dataBaseDao.findById(SqlBuilder.build(SysConfigDefine.class,entity.getParentId()));
			AssertUtil.service().notNull(parent, "上级节点未找到");
			entity.setAppId(parent.getAppId());
			entity.setAppName(parent.getAppName());
			entity.setLevel(parent.getLevel()+1);
			entity.setLvsn(codeTreeService.generate("SYSCONFIG",parent.getLvsn(),entity.getLevel()));
			entity.setSn(parent.getSn()+"."+entity.getName());
		}else{
			entity.setLvsn(codeTreeService.generate("SYSCONFIG"));
			entity.setSn(entity.getName());
		}

		LogsUtil.add("验证配置名称是否已存在,name:%s",entity.getName());
		SysConfigDefine tmp = dataBaseDao.findOne(SqlBuilder.build(entity).where("name=? and parentId=? and id!=?"));
		AssertUtil.service().isTrue(tmp==null, String.format("配置名称[%s]已存在", entity.getName()));

		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"title","name","sn","types","valueDefault","valueUsed","ordered","status","descs"};
			SqlBuilder<SysConfigDefine> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@PostMapping("/status")
	@Action(title="设置配置状态",type = ActionType.Save,roles = {UserRoles.SYSOPSUSER})
	@Operation(summary = "设置配置状态", description="USEORNOT 1 使用，0停用")
	public Results<Void> setStatus(@RequestBody SysConfigDefine entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}

	@Override
	@Action(title="加载统一配置列表",type = ActionType.Query,nolog = true)
	public Results<List<SysConfigDefine>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysConfigDefine> rows = dataBaseDao.findByIds(SqlBuilder.build(SysConfigDefine.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载统一配置详情",type = ActionType.Query,nolog = true)
	public Results<SysConfigDefine> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysConfigDefine tmp = dataBaseDao.findById(SqlBuilder.build(SysConfigDefine.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除统一配置",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");

		long unckChildCount = dataBaseDao.findList(SqlBuilder.build(SysConfigDefine.class)
			.where("delFlag=0 and parentId in [pids]")
			.params("pids", ids))
			.stream()
			.filter(r->!ids.contains(r.getId()))
			.count();
		AssertUtil.service().isTrue(unckChildCount==0, "请先删除子节点");	
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(SysConfigDefine.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


	@Override
 @Action(title="加载统一配置子级",type = ActionType.Query,nolog = true)
	public Results<List<SysConfigDefine>> children(Long pid){
		 //参数处理
		AssertUtil.service().notNull(pid, "参数pid不能为空");
		
		// 执行查询
		SysConfigDefine params = new SysConfigDefine();
		params.setParentId(pid);
		params.setDelFlag(0);
		List<SysConfigDefine> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}

}
