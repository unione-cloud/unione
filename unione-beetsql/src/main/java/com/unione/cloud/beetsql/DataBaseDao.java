package com.unione.cloud.beetsql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.SQLType;
import org.beetl.sql.core.ExecuteContext;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.SqlId;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.SqlBuilder.SqlType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.generator.SidGenHolder;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;

import cn.hutool.core.date.DateUtil;

/**
 * 	数据库操作Dao基础接口
 * @author Jeking Yang
 */
@Service
public class DataBaseDao {
	
	@Autowired
	protected SQLManager sqlManager;
	{
		SQLManager.javabeanStrict(false);
	}
	
	/**
	 *	 插入数据
	 * @param <T>
	 * @param entity
	 * @return
	 */
	public <T> int insert(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			pojo.setId(SidGenHolder.generate());
			BeanUtils.setDefaultValue(pojo, "tenantId", sessionService.getTenantId());
			BeanUtils.setDefaultValue(pojo, "orgId", sessionService.getOrgId());
			BeanUtils.setDefaultValue(pojo, "userId", sessionService.getUserId());
			BeanUtils.setDefaultValue(pojo, "created", DateUtil.current());
			BeanUtils.setDefaultValue(pojo, "createdBy", sessionService.getUsername());
			BeanUtils.setDefaultValue(pojo, "lastUpdated", DateUtil.current());
			BeanUtils.setDefaultValue(pojo, "lastUpdatedBy", sessionService.getUsername());
		}
		
		return this.sqlManager.insertTemplate(entity.getClass(),entity);
	}
	
	/**
	 * 	保存数据(自己设置主键)
	 * @param <T>
	 * @param entity
	 * @return
	 */
	public <T> int insertWithId(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			BeanUtils.setDefaultValue(pojo, "tenantId", sessionService.getTenantId());
			BeanUtils.setDefaultValue(pojo, "orgId", sessionService.getOrgId());
			BeanUtils.setDefaultValue(pojo, "userId", sessionService.getUserId());
			BeanUtils.setDefaultValue(pojo, "created", DateUtil.current());
			BeanUtils.setDefaultValue(pojo, "createdBy", sessionService.getUsername());
			BeanUtils.setDefaultValue(pojo, "lastUpdated", DateUtil.current());
			BeanUtils.setDefaultValue(pojo, "lastUpdatedBy", sessionService.getUsername());
		}
		return this.sqlManager.insertTemplate(entity.getClass(),entity);
	}

	/**
	 * 	批量插入数据
	 * @param <T>
	 * @param list
	 * @return
	 */
	public <T> int[] insertBatch(List<T> list) {
		if(list.get(0) instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			list.stream().forEach(i->{
				Pojo pojo=(Pojo)i;
				pojo.setId(SidGenHolder.generate());
				BeanUtils.setDefaultValue(pojo, "tenantId", sessionService.getTenantId());
				BeanUtils.setDefaultValue(pojo, "orgId", sessionService.getOrgId());
				BeanUtils.setDefaultValue(pojo, "userId", sessionService.getUserId());
				BeanUtils.setDefaultValue(pojo, "created", DateUtil.current());
				BeanUtils.setDefaultValue(pojo, "createdBy", sessionService.getUsername());
				BeanUtils.setDefaultValue(pojo, "lastUpdated", DateUtil.current());
				BeanUtils.setDefaultValue(pojo, "lastUpdatedBy", sessionService.getUsername());
			});
		}
		
		return this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}
	
	/**
	 * 	批量插入数据
	 * @param <T>
	 * @param list
	 * @return
	 */
	public <T> int[] insertBatchWithId(List<T> list) {
		if(list.get(0) instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			list.stream().forEach(i->{
				Pojo pojo=(Pojo)i;
				BeanUtils.setDefaultValue(pojo, "tenantId", sessionService.getTenantId());
				BeanUtils.setDefaultValue(pojo, "orgId", sessionService.getOrgId());
				BeanUtils.setDefaultValue(pojo, "userId", sessionService.getUserId());
				BeanUtils.setDefaultValue(pojo, "created", DateUtil.current());
				BeanUtils.setDefaultValue(pojo, "createdBy", sessionService.getUsername());
				BeanUtils.setDefaultValue(pojo, "lastUpdated", DateUtil.current());
				BeanUtils.setDefaultValue(pojo, "lastUpdatedBy", sessionService.getUsername());
			});
		}
		
		return this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}

	
	/**
	 * 	更新数据
	 * @param updater
	 * @return
	 */
	public <T> int update(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(updater.getData().getClass()), "update");
		if(updater.getData() instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)updater.getData();
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		return this.sqlManager.update(sqlId, updater);
	}
	
	/**
	 * 	更新数据
	 * @param builder
	 * @return
	 */
	public <T> int update(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE);
		if(builder.getData() instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)builder.getData();
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	更新数据
	 * @param updater
	 * @return
	 */
	public <T> int updateById(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(updater.getData().getClass()), "updateById");
		if(updater.getData() instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)updater.getData();
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		return this.sqlManager.update(sqlId, updater);
	}
	
	/**
	 * 	更新数据
	 * @param <T>
	 * @param params
	 * @return
	 */
	public <T> int updateById(T params) {
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE);
		if(builder.getData() instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)builder.getData();
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	删除数据
	 * @param params
	 * @return
	 */
	public <T> int delete(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "delete");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		return this.sqlManager.update(sqlId, map);
	}
	
	
	/**
	 * 	删除数据
	 * @param params
	 * @return
	 */
	public <T> int delete(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE);
		return this.sqlManager.update(sqlId,builder.toParams());
	}
	
	
	/**
	 * 	统计数量
	 * @param params
	 * @return
	 */
	public <T> long count(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.COUNT);
		return (long) this.sqlManager.selectUnique(sqlId, builder.toParams(),Long.class);
	}
	
	/**
	 * 	统计数量
	 * @param params
	 * @return
	 */
	public <T> long count(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "count");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		return this.sqlManager.selectUnique(sqlId, map, Long.class);
	}
	
	/**
	 * 	查询唯一数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT);
		return (T) this.sqlManager.selectUnique(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	查询唯一数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findUnique");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		return (T) this.sqlManager.selectUnique(sqlId, map, params.getClass());
	}
	
	/**
	 * 	查询一条数据
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT);
		return (T) this.sqlManager.selectSingle(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	查询一条数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findOne");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
	}
	
	
	/**
	 * 	根据id查询数据
	 * @param cls
	 * @param id
	 * @return
	 */
	public <T> T findById(Class<T> cls,Object id) {
		List<T> list=(List<T>) this.sqlManager.selectByIds(cls, Arrays.asList(id));
		if(list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	/**
	 * 	查询列表(根据id查询数据)
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findById");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
	}
	
	
	/**
	 * 	查询列表(根据ids集合加载数据)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(T params,Sort ...sort){
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findByIds");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		return (List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
	}
	
	/**
	 * 	根据ids查询数据
	 * @param cls
	 * @param ids
	 * @return
	 */
	public <T> List<T> findByIds(Class<T> cls,List<Object> ids) {
		return(List<T>) this.sqlManager.selectByIds(cls, ids);
	}
	
	/**
	 * 	查询列表(不分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(SqlBuilder<T> builder){
		SqlId findsql=this.loadSql(builder, SqlType.SELECT);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.targetClass(), builder.toParams());
		return rows;
	}
	
	
	/**
	 * 	查询列表(不分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(T params,Sort ...sort){
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findList");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		
		return (List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
	}

	/**
	 * 	查询列表(分页),不执行total统计
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findPageList(SqlBuilder<T> builder){
		SqlId findsql=this.loadSql(builder, SqlType.SELECT);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
		return rows;
	}
	
	/**
	 * 	查询列表(分页),不执行total统计
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findPageList(Params<T> params,Sort ...sort){
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getBody().getClass()), "findList");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		
		// 如果未手动设置排序，则从params中获取排序
		if(sort.length==0 && !StringUtils.isEmpty(params.getSortName())) {
			String stns[]=params.getSortName().split(",");
			String sons[]=null;
			if(!StringUtils.isEmpty(params.getSortOrder())) {
				sons=params.getSortOrder().split(",");
			}
			sort=new Sort[stns.length];
			for(int i=0;i<stns.length;i++) {
				String so=(sons!=null&&(i<sons.length)?sons[i]:"asc");
				sort[i]=Sort.build(stns[i], so);
			}
		}
		
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		return (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
	}
	
	/**
	 * 	查询列表(分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(Params<T> params,Sort ...sort){
		Results<List<T>> results=Results.success();
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getBody().getClass()), "findList");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		if(params.isNeedCount()) {
			Long total = this.sqlManager.selectUnique(SqlId.of(this.getNameSpace(params.getBody().getClass()), "count"), map, Long.class);
			results.setTotal(total);
		}
		
		// 如果未手动设置排序，则从params中获取排序
		if(sort.length==0 && !StringUtils.isEmpty(params.getSortName())) {
			String stns[]=params.getSortName().split(",");
			String sons[]=null;
			if(!StringUtils.isEmpty(params.getSortOrder())) {
				sons=params.getSortOrder().split(",");
			}
			sort=new Sort[stns.length];
			for(int i=0;i<stns.length;i++) {
				String so=(sons!=null&&(i<sons.length)?sons[i]:"asc");
				sort[i]=Sort.build(stns[i], so);
			}
		}
		
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		List<T> rows = (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
		
		return results.setBody(rows);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(SqlBuilder<T> builder){
		Results<List<T>> results=Results.success();
		
		// count 统计
		if(builder.isNeedCount()) {
			SqlId countsql=this.loadSql(builder, SqlType.COUNT);
			Long total = this.sqlManager.selectUnique(countsql, builder.toParams(), Long.class);
			results.setTotal(total);
		}
		
		// 数据查询
		SqlId findsql=this.loadSql(builder, SqlType.SELECT);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
		
		return results.setBody(rows);
	}
	
	
	/**
	 * 	加载SQL
	 * @param <T>
	 * @param builder
	 * @param name
	 * @return
	 */
	private <T> SqlId loadSql(SqlBuilder<T> builder,SqlType type) {
		SqlId sql=SqlId.of(builder.nameSpace(), builder.key(type));
		if(this.sqlManager.containSqlId(sql)) {
			return sql;
		}else {
			SQLSource tempSource = this.sqlManager.getSqlLoader().queryAutoSQL(sql);
			if(tempSource==null) {
				tempSource=new SQLSource(sql, builder.toSql(type));
				tempSource.setSqlType(SQLType.valueOf(SqlType.COUNT.equals(type)?SqlType.SELECT.name():type.name()));
				tempSource.setAutoGenerated(true);
				ExecuteContext context = ExecuteContext.instance(this.sqlManager);
				context.initSQLSource(tempSource);
				this.sqlManager.getSqlLoader().addAutoGenSQL(sql, tempSource);
			}
		}
		return sql;
	}
	
	
	/**
	 * 	获得当前Dao服务Sql命名空间名称
	 * @return
	 */
	private <T> String getNameSpace(Class<T> cla) {
		SqlResource sqlResource = cla.getAnnotation(SqlResource.class);
		if(sqlResource!=null) {
			return sqlResource.value();
		}
		String simpleName=cla.getSimpleName();
		return (simpleName.charAt(0)+"").toLowerCase()+simpleName.substring(1, simpleName.length());
	}
	
}
