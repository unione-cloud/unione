package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.unione.cloud.system.dto.CodeLvsnDto;
import com.unione.cloud.system.model.SysCodeTree;
import com.unione.cloud.system.model.SysOrgan;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.service.CodeTreeService;
import com.unione.cloud.system.service.OrganService;
import com.unione.cloud.system.service.TenantService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysCodeTree Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2025-03-12 08:13:20
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：层级树",description="SysCodeTree")
@RequestMapping("/api/system/codeTree")	 //TreeFeignApi
public class SysCodeTreeController implements PojoFeignApi<SysCodeTree>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private CodeTreeService codeTreeService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private OrganService organService;
	
	
	@Override
	@Action(title="查询层级树",type = ActionType.Query,nolog = true)
	public Results<List<SysCodeTree>> find(Params<SysCodeTree> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysCodeTree>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@PostMapping("/lvsn")
	@Action(title="查询层级编码",type = ActionType.Query,nolog = true)
	public Results<List<CodeLvsnDto>> lvsn(@RequestBody Params<CodeLvsnDto> params) {
		AssertUtil.service()
			.notNull(params.getBody(),"请求参数body不能为空")
			.notNull(params.getBody().getTreeId(), "参数 treeId 不能为空");
				
		Results<List<CodeLvsnDto>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());

		if(results.getBody().size()>0){
			// 加载树信息
			SysCodeTree tree = codeTreeService.loadTree(results.getBody().get(0).getTreeSn());
			
			// 获取租户信息
			Set<Long> tids = results.getBody().stream().map(CodeLvsnDto::getTenantId).collect(Collectors.toSet());
			Map<Long,SysTenant> tmap = tenantService.loadTenant(tids);
			// 获取组织信息
			Set<Long> oids = results.getBody().stream().map(CodeLvsnDto::getOrgId).collect(Collectors.toSet());
			Map<Long,SysOrgan> omap = organService.loadOrgan(oids);

			results.getBody().forEach(item -> {
				codeTreeService.readLvsn(item);
				//设置树信息
				if(tree!=null){
					item.setAppName(tree.getAppName());
					item.setTreeTitle(tree.getTitle());
				}
				//设置租户信息
				SysTenant tenant = tmap.get(item.getTenantId());
				if(tenant!=null){
					item.setTenantName(tenant.getName());
				}
				//设置组织信息
				SysOrgan organ = omap.get(item.getOrgId());
				if(organ!=null){
					item.setOrgName(organ.getName());
				}
			});
		}
		
		return results;
	}


	@Override
	@Action(title="保存层级树",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysCodeTree entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"appName","title","sn","types","lvLen","status","delFlag","descs"};
			SqlBuilder<SysCodeTree> sqlBuilder=SqlBuilder.build(entity).field(fields);
		 	len = dataBaseDao.updateById(sqlBuilder);
		}
		return Results.build(len>0, entity.getId());
	}

	@Override
	@Action(title="加载层级树列表",type = ActionType.Query,nolog = true)
	public Results<List<SysCodeTree>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysCodeTree> rows = dataBaseDao.findByIds(SqlBuilder.build(SysCodeTree.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	@Action(title="加载层级树详情",type = ActionType.Query,nolog = true)
	public Results<SysCodeTree> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		SysCodeTree tmp = dataBaseDao.findById(SqlBuilder.build(SysCodeTree.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除层级树",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(SysCodeTree.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
