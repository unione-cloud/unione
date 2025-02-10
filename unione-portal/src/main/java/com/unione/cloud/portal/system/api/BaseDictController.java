package com.unione.cloud.portal.system.api;

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

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.audit.Action;
import com.unione.cloud.core.audit.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.portal.system.dto.DictShowDto;
import com.unione.cloud.portal.system.model.BaseDict;
import com.unione.cloud.web.logs.LogsUtil;

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
@Tag(name = "系统管理：基础字典",description="BaseDict")
@RequestMapping("/api/system/dict")	 //TreeFeignApi
public class BaseDictController implements TreeFeignApi<BaseDict>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Operation(description="加载字典")
	@GetMapping("/load/{name}")
	public Results<List<BaseDict>> load(@PathVariable("name") String name){
		log.debug("进入:加载基础字典信息方法,name:{}",name);
		List<BaseDict> list = dataBaseDao.findList(SqlBuilder.build(BaseDict.builder()
				.dictName(name).status(1).build()).sort(Sort.build("ordered", "asc")));
		
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
	
	
	@Override
	@Action(title="查询字典",type = ActionType.Query)
	public Results<List<BaseDict>> find(Params<BaseDict> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<BaseDict>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		return results;
	}


	@Override
	@Action(title="保存字典",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) BaseDict entity) {
		// 验证字典名称是否已存在
		BaseDict parent=null;
		if(Objects.equals(-1L, entity.getParentId())) {
			parent = dataBaseDao.findOne(SqlBuilder.build(entity).where("dictName=? and parentId=-1"));
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
		}else {
			return update(entity);
		}
		
		return Results.success(entity.getId());
	}


	
	private Results<Long> update(@Validated(Validator.update.class) BaseDict entity) {
		Results<Long> results = new Results<>();
		int len = 0;
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
			String[] fields = {"appId","appName","dictType","dictValue","dictShow","ordered","isLeaf","status"};
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
			String[] fields = {"dictKey","dictValue","dictShow","ordered","status"};
			SqlBuilder<BaseDict> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
			LogsUtil.add("保存数据,len:"+len);
		}
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		return results;
	}


	@PostMapping("/status")
	@Action(title="字典启用/停用",type = ActionType.Save)
	@Operation(description="启用/停用")
	public Results<Void> setStatus(@RequestBody BaseDict entity){
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
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
	@Action(title="删除字典",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(BaseDict.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
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
	
		List<BaseDict> rows = dataBaseDao.findList(SqlBuilder.build(params));
		
		return Results.success(rows);
	}

}
