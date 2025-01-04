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
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	BaseDict Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:06:12
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Api(tags = "系统管理：基础字典",description="BaseDict")
@RequestMapping("/api/system/dict")	 //TreeFeignApi
public class BaseDictController implements TreeFeignApi<BaseDict>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@ApiOperation(value="加载字典")
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
	public Results<List<BaseDict>> find(Params<BaseDict> params) {
		log.debug("进入:查询基础字典列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询基础字典列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<BaseDict>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询基础字典列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) BaseDict entity) {
		log.debug("进入:新增基础字典信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增基础字典");
		
		// 验证字典名称是否已存在
		BaseDict parent=null;
		if(Objects.equals(-1L, entity.getParentId())) {
			long count = dataBaseDao.count(SqlBuilder.build(entity).where("dictName=?"));
			AssertUtil.service().isTrue(count<=0, "字典名称["+entity.getDictName()+"]已存在");
			entity.setDictKey(entity.getDictName());
		}else {
			parent=dataBaseDao.findById(BaseDict.class, entity.getParentId());
			AssertUtil.service().notNull(parent, "父级节点未找到");
		}
		
		// 参数处理
		BeanUtils.setDefaultValue(entity, "ordered",0);
		BeanUtils.setDefaultValue(entity, "status",1);
		dataBaseDao.insert(entity);
		
		if(parent!=null && Objects.equals(1, parent.getIsLeaf())) {
			parent.setIsLeaf(0);
			int len = dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
			LogsUtil.add("设置父节点isLeaf属性,nid:%s,len:%s",parent.getId(),len);
		}
		
		LogsUtil.success(entity.getId());
		log.debug("退出:新增基础字典信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


	@Override
	public Results<Long> update(@Validated(Validator.update.class) BaseDict entity) {
		log.debug("进入:修改基础字典信息方法，entity:{}",entity);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Update, "修改基础字典",entity.getId());
		
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
		LogsUtil.save(len>0, entity.getId());

		log.debug("退出:修改基础字典信息方法，entity:{},result:{}",entity,results.isSuccess());
		return results;
	}


	@PostMapping("/status")
	@ApiOperation(value="启用/停用")
	public Results<Void> setStatus(@RequestBody BaseDict entity){
		log.debug("进入:启用/停用方法，id:{},status:{}",entity.getId(),entity.getStatus());
		LogsUtil.set(LogType.Update, "启用/停用基础字典");
		AssertUtil.service().notNull(entity, new String[] {"id","status"},"属性%s不能为空")
			.notIn(entity.getStatus(), Arrays.asList(0,1), "参数status取值范围[0,1]");
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field("status"));
		
		log.debug("退出:启用/停用方法，id:{},status:{},len:{}",entity.getId(),entity.getStatus(),len);
		LogsUtil.save(len>0);
		return Results.build(len>0);
	}
	

	@Override
	public Results<List<BaseDict>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询基础字典信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询基础字典");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<BaseDict> rows = dataBaseDao.findByIds(SqlBuilder.build(BaseDict.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询基础字典信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<BaseDict> detail(Long id) {
		log.debug("进入:查看基础字典详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看基础字典详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		BaseDict tmp = dataBaseDao.findById(SqlBuilder.build(BaseDict.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看基础字典详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Long> delete(Set<Long> ids){
		log.debug("进入:删除基础字典信息方法，ids:{}",ids);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除基础字典");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.delete(SqlBuilder.build(BaseDict.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody((long)count);
		LogsUtil.save(count>0);

		log.debug("退出:删除基础字典信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


	@Override
	public Results<List<BaseDict>> children(Long id){
		log.debug("进入:加载下级基础字典信息,id:{}",id);
		LogsUtil.set(LogType.Query, "加载下级基础字典信息",id);
		 //参数处理
		AssertUtil.service().notNull(id, "参数id不能为空");
		
		// 执行查询
		BaseDict params = new BaseDict();
		params.setParentId(id);
		LogsUtil.add("parentId:%s",id);
	
		List<BaseDict> rows = dataBaseDao.findList(SqlBuilder.build(params));
		LogsUtil.add("下级基础字典记录数量:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:加载下级基础字典信息,id:{},result:true",id);
		return Results.success(rows);
	}

}
