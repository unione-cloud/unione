package com.unione.cloud.base.api;

import static org.mockito.ArgumentMatchers.endsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.base.dto.DictShowDto;
import com.unione.cloud.base.model.BaseDict;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
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
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.util.DictUtil;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	BaseDict Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:06:12
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "基础：字典管理",description="BaseDict")
@RequestMapping("/api/base/dict")	 //TreeFeignApi
public class BaseDictController implements TreeFeignApi<BaseDict>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	
	@Operation(description="加载字典")
	@GetMapping("/load/{name}")
	public Results<List<BaseDict>> load(@PathVariable("name") String name){
		log.debug("进入:加载基础字典信息方法,name:{}",name);
		List<BaseDict> list = dataBaseDao.findList(SqlBuilder.build(BaseDict.builder()
				.dictName(name).status(1).build()).sort(Sort.build("ordered", "asc"))
				.dataPermis(PermisRule.ALL));
		
		list.stream().forEach(row->{
			row.setCreated(null);
			row.setCreatedBy(null);
			row.setLastUpdated(null);
			row.setLastUpdatedBy(null);
			row.setAppId(null);
			row.setAppName(null);
			row.setStatus(null);
		});
		
		return Results.success(list);
	}
	
	@PostMapping("/list")
	@Operation(summary="加载字典列表",description="根据关键字搜索字典列表")
	@Action(title="加载字典列表",type = ActionType.Query)
	public Results<List<BaseDict>> list(@RequestBody Params<BaseDict> params) {
		if(params.getBody()==null){
			params.setBody(BaseDict.builder().build());
		}
		params.getBody().setParentId(-1L);
		params.getBody().setStatus(1);
		Results<List<BaseDict>> results = dataBaseDao.findPages(SqlBuilder.build(params).dataPermis(PermisRule.ALL));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		return results;
	}
	
	@Override
	@Action(title="查询字典",type = ActionType.Query)
	public Results<List<BaseDict>> find(Params<BaseDict> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");

		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			params.getBody().setTenantId(sessionService.getTenantId());
		}
		Results<List<BaseDict>> results = dataBaseDao.findPages(SqlBuilder.build(params).dataPermis(PermisRule.ALL)
			.where("(isGlobal = 1 or isGlobal = 0 and tenantId=?) and dictType=? and status=?"));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		return results;
	}


	@Override
	@Action(title="保存字典",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	public Results<Long> save(@Validated(Validator.save.class) BaseDict entity) {
		// 验证字典名称是否已存在
		BaseDict parent=null;
		if(Objects.equals(-1L, entity.getParentId())) {
			parent = dataBaseDao.findOne(SqlBuilder.build(entity).where("dictName=? and parentId = -1"));
			if(parent!=null) {
				BeanUtils.copy(parent, entity, "appId","appName","dictName","dictType");
				entity.setParentId(parent.getId());
			}
			entity.setDictKey(entity.getDictName());
		}else {
			parent=dataBaseDao.findById(BaseDict.class, entity.getParentId());
			AssertUtil.service().notNull(parent, "父级节点未找到");
			BeanUtils.copy(parent, entity, "appId","appName","dictName","dictType");
		}
		
		// 参数处理
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "status",1);
		BeanUtils.setDefaultValue(entity, "isLeaf",1);
		BeanUtils.setDefaultValue(entity, "isGlobal", 0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			entity.setIsGlobal(0);
		}

		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
			LogsUtil.add("新增数据,len:"+len);
			if(parent!=null && Objects.equals(1, parent.getIsLeaf())) {
				parent.setIsLeaf(0);
				len = dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
				LogsUtil.add("设置父节点isLeaf属性,nid:%s,len:%s",parent.getId(),len);
			}
			AssertUtil.service().isTrue(len>0, "保存失败");
			DictUtil.clear(entity.getDictName());
		}else {
			return update(entity);
		}
		
		return Results.success(entity.getId());
	}


	
	private Results<Long> update(@Validated(Validator.update.class) BaseDict entity) {
		Results<Long> results = new Results<>();
		AssertUtil.service().notNull(entity.getId(), "参数id不能为空");
		int len = 0;

		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			BaseDict tmp = dataBaseDao.findById(SqlBuilder.build(BaseDict.class,entity.getId()).dataPermis(PermisRule.ALL));
			AssertUtil.service().notNull(tmp, "记录未找到或无权限")
				.isTrue(ObjectUtil.equal(tmp.getIsGlobal(), 0), "当前帐号无权修改全局数据")
				.isTrue(ObjectUtil.equal(tmp.getTenantId(), sessionService.getTenantId()), "记录未找到");
		}

		if(Objects.equals(-1L, entity.getParentId())) {
			LogsUtil.add("加载字典信息,id:%s",entity.getId());
			BaseDict tmp=dataBaseDao.findById(BaseDict.class, entity.getId());
			AssertUtil.service().notNull(tmp, "记录未找到");
			
			LogsUtil.add("更新字典信息");
			entity.setIsLeaf(0);
			entity.setDictKey(entity.getDictName());
			BeanUtils.setDefaultValue(entity, "ordered",0);
			BeanUtils.setDefaultValue(entity, "status",1);
			BeanUtils.setDefaultValue(entity, "dictShow","{\"type\":\"text\"}");
			String[] fields = {"appId","appName","dictType","dictValue","dictShow","ordered","isLeaf","isGlobal","status"};
			SqlBuilder<BaseDict> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
			LogsUtil.add("保存数据,len:"+len);
			
			LogsUtil.add("判断字典信息，验证是否需要同步更新字典项");
			List<String> fieldList=Arrays.asList("appName","dictType","status").stream()
				.filter(field->!Objects.equals(BeanUtils.getFieldValue(entity, field), BeanUtils.getFieldValue(tmp, field)))
				.collect(Collectors.toList());
			if(!fieldList.isEmpty()) {
				int len2 = dataBaseDao.update(SqlBuilder.build(entity).where("dictName=?").field(fieldList.toArray(new String[0])));
				LogsUtil.add("同步更新字典项,fields:%s,len:%s",fieldList,len2);
			}
			if(!Objects.equals(entity.getDictShow(), tmp.getDictShow())) {
				DictShowDto show=JsonUtil.toBean(DictShowDto.class, entity.getDictShow());
				AtomicInteger count=new AtomicInteger();
				dataBaseDao.findList(SqlBuilder.build(entity).where("dictName=?"))
					.stream().filter(dict->!Objects.equals(-1L, dict.getParentId()))
					.forEach(dict->{
						if(!StringUtils.isEmpty(dict.getDictShow())) {
							DictShowDto dictShow=JsonUtil.toBean(DictShowDto.class, dict.getDictShow());
							dictShow.setType(show.getType());
							dict.setDictShow(JsonUtil.toJson(dictShow));
						}else {
							dict.setDictShow(entity.getDictShow());
						}
						int len2 = dataBaseDao.updateById(SqlBuilder.build(dict).field("dictShow"));
						if(len2>0) {
							count.addAndGet(1);
						}
					});
				LogsUtil.add("同步更新字典项,fields:dictShow,len:%s",count.get());
			}
		}else {
			// 修改字典项信息
			String[] fields = {"dictKey","dictValue","dictShow","ordered","isGlobal","status"};
			SqlBuilder<BaseDict> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
			LogsUtil.add("保存数据,len:"+len);
		}

		DictUtil.clear(entity.getDictName());
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		return results;
	}


	@PostMapping("/status")
	@Action(title="设置字典状态",type = ActionType.Save,roles = {UserRoles.FORMDEV})
	@Operation(summary = "设置字典状态", description="USEORNOT 1 使用，0停用")
	public Results<Void> setStatus(@RequestBody BaseDict entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");

		BaseDict tmp = null;
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			tmp = dataBaseDao.findById(SqlBuilder.build(BaseDict.class,entity.getId()).dataPermis(PermisRule.ALL));
			AssertUtil.service().notNull(tmp, "记录未找到或无权限")
				.isTrue(ObjectUtil.equal(tmp.getIsGlobal(), 0), "当前帐号无权修改全局数据")
				.isTrue(ObjectUtil.equal(tmp.getTenantId(), sessionService.getTenantId()), "记录未找到");
		}
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		if(len>0){
			tmp = tmp!=null?tmp:dataBaseDao.findById(SqlBuilder.build(BaseDict.class, entity.getId()));
			if(tmp!=null){
				DictUtil.clear(tmp.getDictName());
			}
		}
		
		return Results.build(len>0);
	}
	

	@Override
	public Results<List<BaseDict>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<BaseDict> rows = dataBaseDao.findByIds(SqlBuilder.build(BaseDict.class,new ArrayList<>(ids)));
		return Results.success(rows);
	}


	@Override
	public Results<BaseDict> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		BaseDict tmp = dataBaseDao.findById(SqlBuilder.build(BaseDict.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除字典",type = ActionType.Delete,roles = {UserRoles.FORMDEV})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");

		List<BaseDict> dicts=dataBaseDao.findByIds(SqlBuilder.build(BaseDict.class,ids).dataPermis(PermisRule.ALL));
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			for(BaseDict row:dicts){
				if(ObjectUtil.equal(row.getIsGlobal(), 1)){
					return Results.error("当前帐号无权删除全局数据");
				}
				if(!ObjectUtil.equal(row.getTenantId(), sessionService.getTenantId())){
					return Results.error("记录未找到");
				}
			}
		}
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(BaseDict.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);

		if(count>0){
			dicts.forEach(dict->{
				DictUtil.clear(dict.getDictName());
			});
		}
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		return results;
	}


	@Override
	public Results<List<BaseDict>> children(Long id){
		 //参数处理
		AssertUtil.service().notNull(id, "参数id不能为空");
		
		// 执行查询
		BaseDict params = new BaseDict();
		params.setParentId(id);
		LogsUtil.add("parentId:%s",id);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
			params.setTenantId(sessionService.getTenantId());
		}
		List<BaseDict> rows = dataBaseDao.findList(SqlBuilder.build(params).dataPermis(PermisRule.ALL)
			.where("(isGlobal = 1 or isGlobal = 0 and tenantId=?) and parentId=?"));
		
		return Results.success(rows);
	}

}
