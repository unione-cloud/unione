package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.dto.ResTreeNodeDto;
import com.unione.cloud.system.model.SysAppInfo;
import com.unione.cloud.system.model.SysGroupPermis;
import com.unione.cloud.system.model.SysOrganPermis;
import com.unione.cloud.system.model.SysPostPermis;
import com.unione.cloud.system.model.SysResource;
import com.unione.cloud.system.model.SysRolePermis;
import com.unione.cloud.system.model.SysUserPermis;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysResource Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：系统资源",description="SysResource")
@RequestMapping("/api/system/resource")	 //TreeFeignApi
public class SysResourceController implements TreeFeignApi<SysResource>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="查询资源",type = ActionType.Query)
	public Results<List<SysResource>> find(Params<SysResource> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysResource>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存资源",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) SysResource entity) {
		// 参数处理
		BeanUtils.setDefaultValue(entity, "parentId",-1L);
		BeanUtils.setDefaultValue(entity, "isLeaf",1);
		BeanUtils.setDefaultValue(entity, "isHide",0);
		BeanUtils.setDefaultValue(entity, "isNeedPermis",1);
		BeanUtils.setDefaultValue(entity, "isPlatform",0);
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "ordered",0);

		LogsUtil.add("验证资源名称是否已存在,name:%s",entity.getName());
		SysResource tmp = dataBaseDao.findOne(SqlBuilder.build(entity).where("name=? and parentId=? and id!=?"));
		AssertUtil.service().isTrue(tmp==null, String.format("资源名称[%s]已存在", entity.getName()));

		if(!Objects.equals(-1L, entity.getParentId())) {
			SysResource parent = dataBaseDao.findById(SqlBuilder.build(SysResource.class,entity.getParentId()));
			AssertUtil.service().notNull(parent, "上级节点未找到");
			if(!Objects.equals(parent.getIsLeaf(), 0)) {
				parent.setIsLeaf(0);
				dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
			}
		}

		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"appId","parentId","name","title","alias","types","url","isIframe","isExternal","isHide","isLeaf","isNeedPermis","isPlatform","iconName","picMax","picMid","picMix","ordered","status","descs","configs"};
			SqlBuilder<SysResource> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		return Results.build(len>0, entity.getId());
	}


	@PostMapping("/status")
	@Action(title="设置资源状态",type = ActionType.Save,roles = {UserRoles.SYS3PCONFIG})
	@Operation(summary = "设置资源状态", description="USEORNOT 1使用，0停用")
	public Results<Void> setStatus(@RequestBody SysResource entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[1,0]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		return Results.build(len>0);
	}


	@Override
	public Results<List<SysResource>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		List<SysResource> rows = dataBaseDao.findByIds(SqlBuilder.build(SysResource.class,new ArrayList<>(ids)));
		
		return Results.success(rows);
	}


	@Override
	public Results<SysResource> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		SysResource tmp = dataBaseDao.findById(SqlBuilder.build(SysResource.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除资源",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysResource.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		return results;
	}


	@Override
	public Results<List<SysResource>> children(Long sid){
		 //参数处理
		AssertUtil.service().notNull(sid, "参数sid不能为空");
		// 执行查询
		SysResource params = new SysResource();
		params.setParentId(sid);
		List<SysResource> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}


	/**
	 * 加载资源树：同步
	 * @param params
	 * @return
	 */
	@PostMapping("/tree/{type}")
	@Operation(summary = "加载资源树：同步", description = "参数：类型type=permisOrgan,permisRole,permisUser,permisGroup,permisPost授权,view查看")
	public Results<List<ResTreeNodeDto>> tree(@PathVariable("type") String type,@RequestBody Params<Long> params){
		List<ResTreeNodeDto> nodes=new ArrayList<>();
		AssertUtil.service().notNull(type, "参数type不能为空")
			.notIn(type, Arrays.asList("permisOrgan","permisRole","permisUser","permisGroup","permisPost","view"), "参数type取值范围[permisOrgan,permisRole,permisUser,permisGroup,permisPost,view]");

		// 加载应用列表
		SqlBuilder<SysAppInfo> appBuilder = SqlBuilder.build(SysAppInfo.class)
			.where("status in (2,3) and isTmpl=0 and (isPlatform=1 or tenantId=?)")
			.params("status",1)
			.params("tenantId",sessionService.getTenantId());
		List<SysAppInfo> appList = dataBaseDao.findList(appBuilder);
			
		// 获得应用id集合
		Set<Long> appIds = appList.stream().map(SysAppInfo::getId).collect(Collectors.toSet());
		if(ObjectUtil.isEmpty(appIds)) {
			return Results.success(nodes);	
		}
		
		// 加载资源列表
		SqlBuilder<SysResource> resBuilder = SqlBuilder.build(SysResource.class)
			.where("status=? and (isPlatform=1 or tenantId=?) and appId in [appIds]")
			.params("status",1)
			.params("tenantId",sessionService.getTenantId())
			.params("appIds", appIds);
		List<SysResource> resList = dataBaseDao.findList(resBuilder);

		Map<Long,Boolean> hadResMap=new HashMap<>();
		// 加载机构资源权限
		if(ObjectUtil.equal(type, "permisOrgan") && params.getBody()!=null) {
			SqlBuilder<SysOrganPermis> permisBuilder = SqlBuilder.build(SysOrganPermis.class)
				.params("orgId", params.getBody());
			dataBaseDao.findList(permisBuilder).stream().forEach(row->{
				hadResMap.put(row.getResId(), ObjectUtil.equal(1, row.getEnDilivery()));
			});
		}
		// 加载角色资源权限
		if(ObjectUtil.equal(type, "permisRole") && params.getBody()!=null) {
			SqlBuilder<SysRolePermis> permisBuilder = SqlBuilder.build(SysRolePermis.class)
				.params("roleId", params.getBody());	
			dataBaseDao.findList(permisBuilder).stream().forEach(row->{
				hadResMap.put(row.getResId(), ObjectUtil.equal(1, row.getEnDilivery()));	
			});
		}
		// 加载用户资源权限
		if(ObjectUtil.equal(type, "permisUser") && params.getBody()!=null) {
			SqlBuilder<SysUserPermis> permisBuilder = SqlBuilder.build(SysUserPermis.class)
				.params("userId", params.getBody());
			dataBaseDao.findList(permisBuilder).stream().forEach(row->{
				hadResMap.put(row.getResId(), ObjectUtil.equal(1, row.getEnDilivery()));	
			});
		}
		// 加载分组资源权限
		if(ObjectUtil.equal(type, "permisGroup") && params.getBody()!=null) {
			SqlBuilder<SysGroupPermis> permisBuilder = SqlBuilder.build(SysGroupPermis.class)
				.params("groupId", params.getBody());	
			dataBaseDao.findList(permisBuilder).stream().forEach(row->{
				hadResMap.put(row.getResId(), ObjectUtil.equal(1, row.getEnDilivery()));	
			});
		}
		// 加载岗位资源权限
		if(ObjectUtil.equal(type, "permisPost") && params.getBody()!=null) {
			SqlBuilder<SysPostPermis> permisBuilder = SqlBuilder.build(SysPostPermis.class)
				.params("groupId", params.getBody());
			dataBaseDao.findList(permisBuilder).stream().forEach(row->{
				hadResMap.put(row.getResId(), ObjectUtil.equal(1, row.getEnDilivery()));	
			});
		}

		// 数据转换
		appList.stream().forEach(row->{
			ResTreeNodeDto node = new ResTreeNodeDto();
			node.setId(row.getId());
			node.setNtype("app");
			node.setPid(-1L);
			node.setTitle(row.getName());
			node.setIcon(row.getIcon());
			node.setAppId(row.getId());
			nodes.add(node);
			if(hadResMap.get(row.getId())!=null){
				node.setChecked(true);
				if(hadResMap.get(row.getId())){
					node.setEnDilivery(1);
				}else{
					node.setEnDilivery(0);
				}
			}
		});
		resList.stream().forEach(row->{
			ResTreeNodeDto node = new ResTreeNodeDto();
			BeanUtils.copyProperties(row,node);
			node.setNtype(row.getTypes());
			node.setPid(row.getParentId());
			node.setAppId(row.getAppId());
			if(ObjectUtil.equal(-1L, row.getParentId())){
				node.setPid(row.getAppId());
			}
			if(hadResMap.get(row.getId())!=null){
				node.setChecked(true);
				if(hadResMap.get(row.getId())){
					node.setEnDilivery(1);
				}else{
					node.setEnDilivery(0);
				}
			}
			nodes.add(node);
		});

		return Results.success(nodes);
	}


}
