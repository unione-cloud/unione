package com.unione.cloud.beetsql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SqlId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.dto.Params;
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
	 * 	保存数据
	 */
	public <T> void insert(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			pojo.setId(SidGenHolder.generate());
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
		
		int len = this.sqlManager.insertTemplate(entity.getClass(),entity);
		AssertUtil.service().isTrue(len>0, "保存数据失败");
	}

	/**
	 * 	保存数据(批量)
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
	 * 	保存数据(自己设置主键)
	 * @param entity
	 */
	public <T> int insertWithId(T entity) {
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
	 * 	保存数据(批量自己设置主键)
	 */
	public <T> int insertBatchWithId(List<T> list) {
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
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				return this.sqlManager.update(sqlId, updater);
			} catch (Exception e) {
				log.error("更新数据失败,sql namespace:{},sql id:{},updater:{}",sqlId.getNamespace(),sqlId.getId(),updater,e);
				throw new ServiceException("更新数据失败",e);
			}
		}else {
			
			
		}
		return 0;
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
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				return this.sqlManager.update(sqlId, updater);
			} catch (Exception e) {
				log.error("更新数据失败,sql namespace:{},sql id:{},updater:{}",sqlId.getNamespace(),sqlId.getId(),updater,e);
				throw new ServiceException("更新数据失败",e);
			}
		}
		
		return 0;
	}
	
	/**
	 * 	删除数据
	 * @param params
	 * @return
	 */
	public <T> int delete(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "delete");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return this.sqlManager.update(sqlId, map);
			} catch (Exception e) {
				log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("删除数据失败",e);
			}
		}
		
		return 0;
	}
	
	
	public <T> int deleteByid(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteById");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return this.sqlManager.update(sqlId, map);
			} catch (Exception e) {
				log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("删除数据失败",e);
			}
		}
		
		return 0;
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteLogic");
		if(params instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)params;
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return this.sqlManager.update(sqlId, map);
			} catch (Exception e) {
				log.error("逻辑删除失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("逻辑删除失败",e);
			}
		}
		
		
		return 0;
	}
	
	/**
	 * 	逻辑删除(根据sid或ids集合删除数据)
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteLogicById");
		if(params instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)params;
			pojo.setLastUpdated(DateUtil.current());
			pojo.setLastUpdatedBy(sessionService.getUsername());
		}
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return this.sqlManager.update(sqlId, map);
			} catch (Exception e) {
				log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("删除数据失败",e);
			}
		}
		
		return 0;
	}

	/**
	 * 	统计数量
	 * @param params
	 * @return
	 */
	public <T> long count(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "count");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return this.sqlManager.selectUnique(sqlId, map, Long.class);
			} catch (Exception e) {
				log.error("执行统计数量失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("执行统计数量失败",e);
			}
		}
		
		return 0; 
	}
	
	/**
	 * 	查询唯一数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findUnique");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return (T) this.sqlManager.selectUnique(sqlId, map, params.getClass());
			} catch (Exception e) {
				log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("查询唯一数据失败",e);
			}
		}

		return null;
	}
	
	/**
	 * 	查询一条数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findUnique");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
			} catch (Exception e) {
				log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("查询唯一数据失败",e);
			}
		}
		
		return null;
	}
	
	/**
	 * 	查询列表(根据id查询数据)
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findById");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
			} catch (Exception e) {
				log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("查询唯一数据失败",e);
			}
		}
		
		return null;
	}
	
	
	/**
	 * 	查询列表(根据ids集合加载数据)，同时会根据租户id，机构id进行过滤
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(T params,Sort ...sort){
		List<T> result=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findByIds");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				map.put("sorts", (sort.length==0?null:Sort.use(sort)));
				result=(List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
			} catch (Exception e) {
				log.error("执行查询列表(根据ids集合加载数据)失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("执行数据查询失败",e);
			}
		}
		
		return result;
	}
	
	
	/**
	 * 	查询列表(不分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(T params,Sort ...sort){
		List<T> result=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findList");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params);
				map.put("sorts", (sort.length==0?null:Sort.use(sort)));
				result=(List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
			} catch (Exception e) {
				log.error("执行数据查询失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("执行数据查询失败",e);
			}
		}
		
		return result;
	}

	/**
	 * 	查询列表(分页),不执行total统计
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findListPage(Params<T> params){
		List<T> results=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getBody().getClass()), "findList");
		if(this.sqlManager.containSqlId(sqlId)) {
			try {
				Map<String,Object> map = new HashMap<>();
				map.put("params", params.getBody());
				results = (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
			} catch (Exception e) {
				log.error("执行分页查询失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
				throw new ServiceException("执行分页查询失败",e);
			}
		}
		return results;
	}
	
	/**
	 * 	查询列表(分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findListByPage(Params<T> params,String...fields){
		Results<List<T>> results=new Results<>();
		SqlId countsql=SqlId.of(this.getNameSpace(params.getBody().getClass()), "count");
		if(!this.sqlManager.containSqlId(countsql)) {
			countsql=SqlId.of("base", "count");
		}
		
		SqlId findsql=SqlId.of(this.getNameSpace(params.getBody().getClass()), "findList");
		if(!this.sqlManager.containSqlId(findsql)) {
			findsql=SqlId.of("base", "findList");
		}
		
		try {
			Map<String,Object> map = new HashMap<>();
			map.put("params", params.getBody());
			Long total = this.sqlManager.selectUnique(countsql, map, Long.class);
			params.setTotal(total);
			results.setTotal(total);
			List<T> rows = (List<T>) this.sqlManager.select(findsql, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
			results.setBody(rows);
			results.setSuccess(true);
		} catch (Exception e) {
			log.error("执行分页查询失败,sql namespace:{},sql id:{},params:{}",findsql.getNamespace(),findsql.getId(),params,e);
			throw new ServiceException("执行分页查询失败",e);
		}
		
		
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findListByPage(SqlBuilder<T> builder){
		Results<List<T>> results=new Results<>();
		
		try {
			// count 统计
			if(builder.isNeedCount()) {
				SqlId countsql=SqlId.of(this.getNameSpace(builder.targetClass()), "count");
				if(this.sqlManager.containSqlId(countsql)) {
					Long total = this.sqlManager.selectUnique(countsql, builder, Long.class);
					results.setTotal(total);
				}else {
					List<Long> list = this.sqlManager.execute(builder.countSql(),Long.class,builder);
					Long total = list.get(0);
					results.setTotal(total);
				}
			}
			
			// 数据查询
			SqlId findsql=SqlId.of(this.getNameSpace(builder.targetClass()), "findList");
			if(this.sqlManager.containSqlId(findsql)) {
				List<T> rows = (List<T>) this.sqlManager.select(findsql, builder, builder.targetClass(),builder.getStart(),builder.getPageSize());
				results.setBody(rows);
			}else {
				List<T> rows = (List<T>)this.sqlManager.execute(builder.findSql(),builder.targetClass(),builder);
				results.setBody(rows);
			}
			
			results.setSuccess(true);
		} catch (Exception e) {
			throw new ServiceException("执行分页查询失败",e);
		}
		
		return results;
	}
	
	/**
	 * 	获得当前Dao服务Sql命名空间名称
	 * @return
	 */
	private <T> String getNameSpace(Class<T> clas) {
		String simpleName=clas.getSimpleName();
		return (simpleName.charAt(0)+"").toLowerCase()+simpleName.substring(1, simpleName.length());
	}
	
}
