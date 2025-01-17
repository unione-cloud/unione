package com.unione.cloud.portal.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.TreeFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.portal.system.model.SysPost;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	SysPost Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：岗位信息，岗位对应的行政区划信息直接存储在数据权限：岗位权限表中。一个岗位可以有多个行政区划，但都是当前用户所属机构关联",description="SysPost")
@RequestMapping("/api/system/post")	 //TreeFeignApi
public class SysPostController implements TreeFeignApi<SysPost>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	public Results<List<SysPost>> find(Params<SysPost> params) {
		log.debug("进入:查询岗位信息列表方法,params:{}",params);
		LogsUtil.set(LogType.Query, "查询岗位信息列表");
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<SysPost>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		LogsUtil.success();
		log.debug("退出:查询岗位信息列表方法,params:{},result:{}",params,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) SysPost entity) {
		log.debug("进入:新增岗位信息信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增岗位信息");
		// 参数处理
		dataBaseDao.insert(entity);
		
		LogsUtil.success(entity.getId());
		log.debug("退出:新增岗位信息信息.entity:{},result:true",entity);
		return Results.success(entity.getId());
	}


	@Override
	public Results<Long> update(@Validated(Validator.update.class) SysPost entity) {
		log.debug("进入:修改岗位信息信息方法，entity:{}",entity);
		Results<Long> results = new Results<>();
		LogsUtil.set(LogType.Update, "修改岗位信息",entity.getId());
		
		String[] fields = {"orgId","parentId","name","sn","types","iconFont","iconPic","duty","descs","isLeaf","ordered","status"};
		SqlBuilder<SysPost> sqlBuilder=SqlBuilder.build(entity).field(fields);
		int len = dataBaseDao.updateById(sqlBuilder);
		LogsUtil.add("保存数据,len:"+len);
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		LogsUtil.save(len>0, entity.getId());

		log.debug("退出:修改岗位信息信息方法，entity:{},result:{}",entity,results.isSuccess());
		return results;
	}



	@Override
	public Results<List<SysPost>> findByIds(Set<Long> ids) {
		log.debug("进入:批量查询岗位信息信息方法，ids:{}",ids);
		LogsUtil.set(LogType.Query, "批量查询岗位信息");
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<SysPost> rows = dataBaseDao.findByIds(SqlBuilder.build(SysPost.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:批量查询岗位信息信息方法，ids:{},result:true",ids);
		return Results.success(rows);
	}


	@Override
	public Results<SysPost> detail(Long id) {
		log.debug("进入:查看岗位信息详细信息方法，id:{}",id);
		LogsUtil.set(LogType.Query, "查看岗位信息详细",id);
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		SysPost tmp = dataBaseDao.findById(SqlBuilder.build(SysPost.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		LogsUtil.success(tmp.getId());
		log.debug("退出:查看岗位信息详细信息方法，id:{},result:true",id);
		return Results.success(tmp);
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入:删除岗位信息信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		LogsUtil.set(LogType.Delete, "删除岗位信息");
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysPost.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		LogsUtil.save(count>0);

		log.debug("退出:删除岗位信息信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


	@Override
	public Results<List<SysPost>> children(Long sid){
		log.debug("进入:加载下级岗位信息信息,sid:{}",sid);
		LogsUtil.set(LogType.Query, "加载下级岗位信息信息",sid);
		 //参数处理
		AssertUtil.service().notNull(sid, "参数sid不能为空");
		
		// 执行查询
		SysPost params = new SysPost();
		params.setParentId(sid);
		LogsUtil.add("parentId:%s",sid);
	
		List<SysPost> rows = dataBaseDao.findList(SqlBuilder.build(params));
		LogsUtil.add("下级岗位信息记录数量:"+rows.size());
		
		LogsUtil.success();
		log.debug("退出:加载下级岗位信息信息,sid:{},result:true",sid);
		return Results.success(rows);
	}

}
