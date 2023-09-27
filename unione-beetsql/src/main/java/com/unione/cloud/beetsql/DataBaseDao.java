package com.unione.cloud.beetsql;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.SQLType;
import org.beetl.sql.core.ExecuteContext;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.SqlId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.SqlBuilder.SqlType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.SidGenHolder;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 	数据库操作Dao基础接口
 * @author Jeking Yang
 */
@Slf4j
@Service
public class DataBaseDao {
	
	@Autowired
	protected SQLManager sqlManager;
	{
		SQLManager.javabeanStrict(false);
	}
	
	/**
	 * 	插入数据
	 */
	public <T> void insert(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			pojo.setId(SidGenHolder.generate());
			if(pojo.getTenantId()==null) {
				pojo.setTenantId(sessionService.getTenantId());
			}
			if(pojo.getOrgId()==null) {
				pojo.setOrgId(sessionService.getOrgId());
			}
			if(pojo.getUserId()==null) {
				pojo.setUserId(sessionService.getUserId());
			}
			
			pojo.setCreated(DateUtil.current());
			pojo.setCreatedBy(sessionService.getUsername());
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		
		int len = this.sqlManager.insertTemplate(entity.getClass(),entity);
		AssertUtil.service().isTrue(len>0, "保存数据失败");
	}

	/**
	 * 	批量插入数据
	 */
	public <T> void insertBatch(List<T> list) {
		if(list.get(0) instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			list.stream().forEach(i->{
				Pojo pojo=(Pojo)i;
				pojo.setId(SidGenHolder.generate());
				pojo.setTenantId(sessionService.getTenantId());
				if(pojo.getOrgId()==null) {
					pojo.setOrgId(sessionService.getOrgId());
				}
				if(pojo.getUserId()==null) {
					pojo.setUserId(sessionService.getUserId());
				}
				
				pojo.setCreated(DateUtil.current());				pojo.setCreatedBy(sessionService.getUsername());
				pojo.setLastUpdated(DateUtil.current());
				pojo.setLastUpdatedBy(sessionService.getUsername());
			});
		}
		this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}

	/**
	 * 	保存数据(手动设置主键)
	 * @param entity
	 */
	public <T> int save(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			pojo.setTenantId(sessionService.getTenantId());
			if(pojo.getOrgId()==null) {
				pojo.setOrgId(sessionService.getOrgId());
			}
			if(pojo.getUserId()==null) {
				pojo.setUserId(sessionService.getUserId());
			}
			
			pojo.setCreated(DateUtil.current());
			pojo.setCreatedBy(sessionService.getUsername());
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		int len = this.sqlManager.insertTemplate(entity);
		AssertUtil.service().isTrue(len>0, "保存数据失败");
		return len;
	}
	
	/**
	 * 	批量保存数据(手动设置主键)
	 */
	public <T> int saveBatch(List<T> list) {
		if(list.get(0) instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			list.stream().forEach(i->{
				Pojo pojo=(Pojo)i;
				pojo.setTenantId(sessionService.getTenantId());
				if(pojo.getOrgId()==null) {
					pojo.setOrgId(sessionService.getOrgId());
				}
				if(pojo.getUserId()==null) {
					pojo.setUserId(sessionService.getUserId());
				}
				
				pojo.setCreated(DateUtil.current());
				pojo.setCreatedBy(sessionService.getUsername());
				pojo.setLastUpdated(DateUtil.current());
				pojo.setLastUpdatedBy(sessionService.getUsername());
			});
		}
		int ln[]= this.sqlManager.insertBatch(list.get(0).getClass(), list);
		int size=0;
		for(int i=0;i<ln.length;i++) {
			size=size+ln[i];
		}
		return size;
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
	 * 更新数据
	 * @param <T>
	 * @param params
	 * @return
	 */
	public <T> int updateById(T params) {
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE,"UPDATE_BY_ID");
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
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE);
		return this.sqlManager.update(sqlId,builder.toParams());
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(T params) {
//		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteLogic");
//		if(params instanceof Pojo) {
//			SessionService sessionService=SessionHolder.build();
//			Pojo pojo=(Pojo)params;
//			pojo.setLastUpdated(DateUtil.current());
//			pojo.setLastUpdatedBy(sessionService.getUsername());
//		}
//		if(this.sqlManager.containSqlId(sqlId)) {
//			try {
//				Map<String,Object> map = new HashMap<>();
//				map.put("params", params);
//				return this.sqlManager.update(sqlId, map);
//			} catch (Exception e) {
//				log.error("逻辑删除失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
//				throw new ServiceException("逻辑删除失败",e);
//			}
//		}
		return 0;
	}
	
	/**
	 * 	统计数量
	 * @param params
	 * @return
	 */
	public <T> long count(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.COUNT);
		return (long) this.sqlManager.selectUnique(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	查询唯一数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT,"FIND_UNIQUE");
		return (T) this.sqlManager.selectUnique(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	查询一条数据
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT,"FIND_ONE");
		return (T) this.sqlManager.selectSingle(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	查询一条数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T params) {
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT,"FIND_ONE");
		return (T) this.sqlManager.selectSingle(sqlId, builder.toParams(), builder.targetClass());
	}
	
	
	/**
	 * 	查询列表(根据id查询数据)
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(T params) {
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT,"FIND_BYID");
		return (T) this.sqlManager.selectUnique(sqlId, builder.toParams(), builder.targetClass());
	}
	
	
	/**
	 * 	查询列表(根据ids集合加载数据)，同时会根据租户id，机构id进行过滤
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(T params,Sort ...sort){
		SqlBuilder<T> builder=SqlBuilder.build(params);
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT,"FIND_BYIDS");
		List<T> rows = (List<T>) this.sqlManager.select(sqlId, builder.targetClass(), builder.toParams());
		return rows;
	}
	
	
	/**
	 * 	查询列表(不分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(SqlBuilder<T> builder){
		SqlId findsql=this.loadSql(builder, SqlType.SELECT,"FIND_LIST");
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.targetClass(), builder.toParams());
		return rows;
	}

	/**
	 * 	查询列表(分页),不执行total统计
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findPage(SqlBuilder<T> builder){
		SqlId findsql=this.loadSql(builder, SqlType.SELECT,"FIND_PAGE");
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
		return rows;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(SqlBuilder<T> builder){
		Results<List<T>> results=new Results<>();
		
		try {
			// count 统计
			if(builder.isNeedCount()) {
				SqlId countsql=this.loadSql(builder, SqlType.COUNT,"FIND_PAGES_COUNT");
				Long total = this.sqlManager.selectUnique(countsql, builder.toParams(), Long.class);
				results.setTotal(total);
			}
			
			// 数据查询
			SqlId findsql=this.loadSql(builder, SqlType.SELECT,"FIND_PAGES_LIST");
			List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
			results.setBody(rows);
			
			results.setSuccess(true);
		} catch (Exception e) {
			throw new ServiceException("执行分页查询失败",e);
		}
		
		return results;
	}
	
	
	/**
	 * 	加载SQL
	 * @param <T>
	 * @param builder
	 * @param name
	 * @return
	 */
	private <T> SqlId loadSql(SqlBuilder<T> builder,SqlType type) {
		return loadSql(builder,type,null);
	}
	
	private <T> SqlId loadSql(SqlBuilder<T> builder,SqlType type,String name) {
		if(!StringUtils.isEmpty(builder.getName())) {
			SqlId sql=SqlId.of(builder.nameSpace(), builder.getName());
			if(this.sqlManager.containSqlId(sql)) {
				return sql;
			}
		}
		
		if(StringUtils.isEmpty(name)) {
			name=String.format("builder_%s_%s",builder.getKey(),type.name());
		}else {
			name=String.format("builder_%s_%s",builder.getKey(),name);
		}
		
		log.info(builder.toSql(type));
		
		SqlId sql=SqlId.of(builder.nameSpace(), name);
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
	
	
}
