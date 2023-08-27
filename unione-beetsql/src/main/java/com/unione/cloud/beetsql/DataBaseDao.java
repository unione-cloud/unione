package com.unione.cloud.beetsql;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.SqlId;
import org.beetl.sql.mapper.BaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.SidGenHolder;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;

/**
 * 	数据库操作Dao基础接口
 * @author Jeking Yang
 * @param <T>
 */
public interface DataBaseDao<T> extends BaseMapper<T> {
	
	static Logger log=LoggerFactory.getLogger(DataBaseDao.class);
	
	
	/**
	 * 	保存数据
	 */
	@Override
	default public void insert(T entity) {
		if(entity instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			Pojo pojo=(Pojo)entity;
			pojo.setSid(SidGenHolder.generate());
			pojo.setTenantId(sessionService.getTenantId());
			if(pojo.getOrgId()==null) {
				pojo.setOrgId(sessionService.getOrgId());
			}
			if(pojo.getUserId()==null) {
				pojo.setUserId(sessionService.getUserId());
			}
			
			pojo.setCreated(new Date());
			pojo.setCreatedBy(sessionService.getUserId());
			pojo.setLastUpdated(new Date());
			pojo.setLastUpdatedBy(sessionService.getUserId());
		}
		int len = this.getSQLManager().insertTemplate(entity);
		AssertUtil.service().isTrue(len>0, "保存数据失败");
	}

	/**
	 * 	保存数据(批量)
	 */
	@Override
	default public void insertBatch(List<T> list) {
		if(list.get(0) instanceof Pojo) {
			SessionService sessionService=SessionHolder.build();
			list.stream().forEach(i->{
				Pojo pojo=(Pojo)i;
				pojo.setSid(SidGenHolder.generate());
				pojo.setTenantId(sessionService.getTenantId());
				if(pojo.getOrgId()==null) {
					pojo.setOrgId(sessionService.getOrgId());
				}
				if(pojo.getUserId()==null) {
					pojo.setUserId(sessionService.getUserId());
				}
				
				pojo.setCreated(new Date());
				pojo.setCreatedBy(sessionService.getUserId());
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			});
		}
		this.getSQLManager().insertBatch(this.getTargetEntity(), list);
	}

	/**
	 * 	保存数据(自己设置主键)
	 * @param entity
	 */
	default public int insertWithId(T entity) {
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
			
			pojo.setCreated(new Date());
			pojo.setCreatedBy(sessionService.getUserId());
			pojo.setLastUpdated(new Date());
			pojo.setLastUpdatedBy(sessionService.getUserId());
		}
		int len = this.getSQLManager().insertTemplate(entity);
		AssertUtil.service().isTrue(len>0, "保存数据失败");
		return len;
	}
	
	/**
	 * 	保存数据(批量自己设置主键)
	 */
	default public int insertBatchWithId(List<T> list) {
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
				
				pojo.setCreated(new Date());
				pojo.setCreatedBy(sessionService.getUserId());
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			});
		}
		int ln[]= this.getSQLManager().insertBatch(this.getTargetEntity(), list);
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
	default public int update(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "update");
		
		try {
			if(updater.getData() instanceof Pojo) {
				SessionService sessionService=SessionHolder.build();
				Pojo pojo=(Pojo)updater.getData();
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			}
			return this.getSQLManager().update(sqlId, updater);
		} catch (Exception e) {
			log.error("更新数据失败,sql namespace:{},sql id:{},updater:{}",sqlId.getNamespace(),sqlId.getId(),updater,e);
			throw new ServiceException("更新数据失败",e);
		}
		
	 }
	
	/**
	 * 	更新数据
	 * @param updater
	 * @return
	 */
	default public int updateById(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "updateById");
		
		try {
			if(updater.getData() instanceof Pojo) {
				SessionService sessionService=SessionHolder.build();
				Pojo pojo=(Pojo)updater.getData();
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			}
			return this.getSQLManager().update(sqlId, updater);
		} catch (Exception e) {
			log.error("更新数据失败,sql namespace:{},sql id:{},updater:{}",sqlId.getNamespace(),sqlId.getId(),updater,e);
			throw new ServiceException("更新数据失败",e);
		}
	}
	
	/**
	 * 	删除数据
	 * @param params
	 * @return
	 */
	default public int delete(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "delete");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		try {
			return this.getSQLManager().update(sqlId, map);
		} catch (Exception e) {
			log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("删除数据失败",e);
		}
	}
	
	/**
	 * 	删除数据(根据sid或ids集合删除数据)
	 * @param params
	 * @return
	 */
	default public int deleteBySid(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "deleteById");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		try {
			return this.getSQLManager().update(sqlId, map);
		} catch (Exception e) {
			log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("删除数据失败",e);
		}
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	default public int deleteLogic(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "deleteLogic");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		try {
			if(params instanceof Pojo) {
				SessionService sessionService=SessionHolder.build();
				Pojo pojo=(Pojo)params;
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			}
			
			return this.getSQLManager().update(sqlId, map);
		} catch (Exception e) {
			log.error("逻辑删除失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("逻辑删除失败",e);
		}
	}
	
	/**
	 * 	逻辑删除(根据sid或ids集合删除数据)
	 * @param params
	 * @return
	 */
	default public int deleteLogicById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "deleteLogicById");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		try {
			if(params instanceof Pojo) {
				SessionService sessionService=SessionHolder.build();
				Pojo pojo=(Pojo)params;
				pojo.setLastUpdated(new Date());
				pojo.setLastUpdatedBy(sessionService.getUserId());
			}
			
			return this.getSQLManager().update(sqlId, map);
		} catch (Exception e) {
			log.error("删除数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("删除数据失败",e);
		}
	}

	/**
	 * 	统计数量
	 * @param params
	 * @return
	 */
	default public long count(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "count");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);

		try {
			return this.getSQLManager().selectUnique(sqlId, map, Long.class);
		} catch (Exception e) {
			log.error("执行统计数量失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("执行统计数量失败",e);
		}
		
	}
	
	
	/**
	 * 	查询唯一数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public T findUnique(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findUnique");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);

		try {
			return (T) this.getSQLManager().selectUnique(sqlId, map, this.getTargetEntity());
		} catch (Exception e) {
			log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("查询唯一数据失败",e);
		}
		
	}
	
	/**
	 * 	查询一条数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public T findOne(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findUnique");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);

		try {
			return (T) this.getSQLManager().selectSingle(sqlId, map, this.getTargetEntity());
		} catch (Exception e) {
			log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("查询唯一数据失败",e);
		}
		
	}
	
	/**
	 * 	查询列表(根据sid查询数据)
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public T findById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findById");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);

		try {
			return (T) this.getSQLManager().selectSingle(sqlId, map, this.getTargetEntity());
		} catch (Exception e) {
			log.error("查询唯一数据失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("查询唯一数据失败",e);
		}
		
	}
	
	
	/**
	 * 	查询列表(不分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public List<T> findList(T params,Sort ...sort){
		List<T> result=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findList");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		try {
			result=(List<T>) this.getSQLManager().select(sqlId, this.getTargetEntity(), map);
		} catch (Exception e) {
			log.error("执行数据查询失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("执行数据查询失败",e);
		}
		
		return result;
	}
	
	/**
	 * 	查询列表(根据ids集合加载数据)，同时会根据租户id，机构id进行过滤
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public List<T> findListByIds(T params,Sort ...sort){
		List<T> result=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findListByIds");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		try {
			result=(List<T>) this.getSQLManager().select(sqlId, this.getTargetEntity(), map);
		} catch (Exception e) {
			log.error("执行查询列表(根据ids集合加载数据)失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("执行数据查询失败",e);
		}
		
		return result;
	}
	
	
	/**
	 * 	查询列表(分页)
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public Results<List<T>> findListByPage(Params<T> params,Sort ...sort){
		Results<List<T>> results=new Results<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findList");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		try {
			Long total = this.getSQLManager().selectUnique(SqlId.of(this.getNameSpace(), "count"), map, Long.class);
			params.setTotal(total);
			map.put("sorts", (sort.length==0?null:Sort.use(sort)));
			List<T> rows = (List<T>) this.getSQLManager().select(sqlId, map, this.getTargetEntity(),(long)params.getStart()+1,(long)params.getPageSize());
			results.setTotal(total);
			results.setBody(rows);
			results.setSuccess(true);
		} catch (Exception e) {
			log.error("执行分页查询失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("执行分页查询失败",e);
		}
		
		return results;
	}
	
	/**
	 * 	查询列表(分页),不执行total统计
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public List<T> findListForPage(Params<T> params,Sort ...sort){
		List<T> results=new ArrayList<>();
		SqlId sqlId=SqlId.of(this.getNameSpace(), "findList");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		try {
			map.put("sorts", (sort.length==0?null:Sort.use(sort)));
			results = (List<T>) this.getSQLManager().select(sqlId, map, this.getTargetEntity(),(long)params.getStart()+1,(long)params.getPageSize());
		} catch (Exception e) {
			log.error("执行分页查询失败,sql namespace:{},sql id:{},params:{}",sqlId.getNamespace(),sqlId.getId(),params,e);
			throw new ServiceException("执行分页查询失败",e);
		}
		
		return results;
	}
	
	/**
	 * 	获得当前Dao服务Sql命名空间名称
	 * @return
	 */
	default public String getNameSpace() {
		String simpleName=this.getTargetEntity().getSimpleName();
		return (simpleName.charAt(0)+"").toLowerCase()+simpleName.substring(1, simpleName.length());
	}
	
}
