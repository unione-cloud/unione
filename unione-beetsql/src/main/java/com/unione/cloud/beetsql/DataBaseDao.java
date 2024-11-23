package com.unione.cloud.beetsql;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.beetl.sql.clazz.SQLType;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.core.ExecuteContext;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.SqlId;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniDataPermis.DataPermis;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.beetsql.builder.SqlType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

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
		SessionService sessionService=SessionHolder.build();
		PropertyDescriptor idProp = BeanKit.getPropertyDescriptor(entity.getClass(), BaseField.ID.getName());
		if(Long.class.equals(idProp.getPropertyType())) {
			BeanKit.setBeanProperty(entity, IdGenHolder.generate(), idProp.getName());
		}
		BeanUtils.setDefaultValue(entity, BaseField.DEL_FLAG.getName(),0);
		BeanUtils.setDefaultValue(entity, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
		BeanUtils.setDefaultValue(entity, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
		BeanUtils.setDefaultValue(entity, BaseField.USER_ID.getName(), sessionService.getUserId());
		BeanUtils.setDefaultValue(entity, BaseField.CREATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(entity, BaseField.CREATED_BY.getName(), sessionService.getUserId());
		BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.insertTemplate(entity.getClass(),entity);
	}
	
	/**
	 * 	保存数据(自己设置主键)
	 * @param <T>
	 * @param entity
	 * @return
	 */
	public <T> int insertWithId(T entity) {
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(entity, BaseField.DEL_FLAG.getName(),0);
		BeanUtils.setDefaultValue(entity, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
		BeanUtils.setDefaultValue(entity, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
		BeanUtils.setDefaultValue(entity, BaseField.USER_ID.getName(), sessionService.getUserId());
		BeanUtils.setDefaultValue(entity, BaseField.CREATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(entity, BaseField.CREATED_BY.getName(), sessionService.getUserId());
		BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		return this.sqlManager.insertTemplate(entity.getClass(),entity);
	}

	/**
	 * 	批量插入数据
	 * @param <T>
	 * @param list
	 * @return
	 */
	public <T> int[] insertBatch(List<T> list) {
		SessionService sessionService=SessionHolder.build();
		list.stream().forEach(entity->{
			PropertyDescriptor idProp = BeanKit.getPropertyDescriptor(entity.getClass(), BaseField.ID.getName());
			if(Long.class.equals(idProp.getPropertyType())) {
				BeanKit.setBeanProperty(entity, IdGenHolder.generate(), idProp.getName());
			}
			BeanUtils.setDefaultValue(entity, BaseField.DEL_FLAG.getName(),0);
			BeanUtils.setDefaultValue(entity, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
			BeanUtils.setDefaultValue(entity, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
			BeanUtils.setDefaultValue(entity, BaseField.USER_ID.getName(), sessionService.getUserId());
			BeanUtils.setDefaultValue(entity, BaseField.CREATED.getName(), DateUtil.date());
			BeanUtils.setDefaultValue(entity, BaseField.CREATED_BY.getName(), sessionService.getUserId());
			BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED.getName(), DateUtil.date());
			BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		});
		
		return this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}
	
	/**
	 * 	批量插入数据
	 * @param <T>
	 * @param list
	 * @return
	 */
	public <T> int[] insertBatchWithId(List<T> list) {
		SessionService sessionService=SessionHolder.build();
		list.stream().forEach(entity->{
			BeanUtils.setDefaultValue(entity, BaseField.DEL_FLAG.getName(),0);
			BeanUtils.setDefaultValue(entity, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
			BeanUtils.setDefaultValue(entity, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
			BeanUtils.setDefaultValue(entity, BaseField.USER_ID.getName(), sessionService.getUserId());
			BeanUtils.setDefaultValue(entity, BaseField.CREATED.getName(), DateUtil.date());
			BeanUtils.setDefaultValue(entity, BaseField.CREATED_BY.getName(), sessionService.getUserId());
			BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED.getName(), DateUtil.date());
			BeanUtils.setDefaultValue(entity, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		});
		
		return this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}

	
	/**
	 * 	更新数据,使用sql更新{ResoruceName}.update
	 * @param updater
	 * @return
	 */
	public <T> int update(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(updater.getData().getClass()), "update");
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(updater.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(updater.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		this.setDataPermis(updater.getData());
		return this.sqlManager.update(sqlId, updater);
	}
	
	/**
	 * 	更新数据
	 * @param builder
	 * @return
	 */
	public <T> int update(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE);

		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	更新数据,使用sql更新{ResoruceName}.updateById
	 * @param updater
	 * @return
	 */
	public <T> int updateById(Updater<T> updater) {
		SqlId sqlId=SqlId.of(this.getNameSpace(updater.getData().getClass()), "updateById");

		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(updater.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(updater.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		this.setDataPermis(updater.getData());
		return this.sqlManager.update(sqlId, updater);
	}
	
	
	/**
	 * 	更新数据，无数据权限验证
	 * @param <T>
	 * @param params
	 * @return
	 */
	public <T> int updateById(T params) {
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.updateById(params);
	}
	
	/**
	 * 	更新数据
	 * @param <T>
	 * @param builder
	 * @return
	 */
	public <T> int updateById(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE_BYID);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setFieldValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setFieldValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	删除数据,使用sql删除{ResoruceName}.delete
	 * @param params
	 * @return
	 */
	public <T> int delete(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "delete");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		this.setDataPermis(params);
		return this.sqlManager.update(sqlId, map);
	}
	
	
	/**
	 * 	删除数据
	 * @param params
	 * @return
	 */
	public <T> int delete(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId,builder.toParams());
	}
	
	/**
	 * 	根据数据id删除，无数据权限验证
	 * @param <T>
	 * @param cls
	 * @param id
	 * @return
	 */
	public <T> int deleteById(Class<T> cls,Object id) {
		return this.sqlManager.deleteById(cls, id);
	}
	
	
	/**
	 * 	根据数据id删除
	 * @param params
	 * @return
	 */
	public <T> int deleteById(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE_BYID);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId,builder.toParams());
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteLogic");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, map);
	}
	
	/**
	 * 	逻辑删除(根据id或ids集合删除数据)
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "deleteLogicById");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(params, BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, map);
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE_LOGIC);

		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	逻辑删除(根据id或ids集合删除数据)
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.DELETE_LOGIC_BYID);

		SessionService sessionService=SessionHolder.build();
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED.getName(), DateUtil.date());
		BeanUtils.setDefaultValue(builder.getData(), BaseField.LAST_UPDATED_BY.getName(), sessionService.getUserId());
		
		return this.sqlManager.update(sqlId, builder.toParams());
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
	 * 	统计数量,使用sql统计{ResoruceName}.count
	 * @param params
	 * @return
	 */
	public <T> long count(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "count");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		this.setDataPermis(params);
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
	 * 	查询唯一数据,使用sql查询{ResoruceName}.findUnique
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findUnique");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		this.setDataPermis(params);
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
	 * 	查询一条数据,使用sql查询{ResoruceName}.findOne
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findOne");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		this.setDataPermis(params);
		return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
	}
	
	/**
	 * 	根据id查询数据
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT);
		return (T) this.sqlManager.selectSingle(sqlId, builder.toParams(), builder.targetClass());
	}
	
	/**
	 * 	根据id查询数据，无数据权限验证
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
	 * 	查询列表(根据id查询数据),使用sql查询{ResoruceName}.findById
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(T params) {
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findById");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		this.setDataPermis(params);
		return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
	}
	
	
	/**
	 * 	查询列表(根据ids集合加载数据),使用sql查询{ResoruceName}.findById
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(T params,Sort ...sort){
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getClass()), "findById");
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		this.setDataPermis(params);
		return (List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
	}
	
	/**
	 * 	根据ids查询数据，无数据权限验证
	 * @param cls
	 * @param ids
	 * @return
	 */
	public <T> List<T> findByIds(Class<T> cls,List<Object> ids) {
		return(List<T>) this.sqlManager.selectByIds(cls, ids);
	}
	
	/**
	 * 	根据ids查询数据
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(SqlBuilder<T> builder) {
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT);
		return (List<T>) this.sqlManager.select(sqlId, builder.targetClass(), builder.toParams());
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
	 * 	查询列表(不分页),使用sql查询{ResoruceName}.findList
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
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", BeanUtil.getFieldValue(params, "keywords"));
		query.put("id", BeanUtil.getFieldValue(params, "id"));
		query.put("ids", BeanUtil.getFieldValue(params, "ids"));
		map.put("query", query);
		
		this.setDataPermis(params);
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
	 * 	查询列表(分页),不执行total统计,使用sql查询{ResoruceName}.findList
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
		if(sort.length==0 && !ObjectUtil.isEmpty(params.getSorts())) {
			List<Sort> sorts = params.getSorts().stream()
				.filter(s->s!=null)
				.map(s->Sort.build(s.getName(), s.isAsc()?"ASC":"DESC"))
				.collect(Collectors.toList());
			if(!sorts.isEmpty()) {
				sort=sorts.toArray(new Sort[] {});
			}
		}
		
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", params.getKeywords());
		query.put("id", params.getId());
		query.put("ids", params.getIds());
		map.put("query", query);
		
		this.setDataPermis(params);
		return (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
	}
	
	/**
	 * 	查询列表(分页),使用sql查询{ResoruceName}.count，{ResoruceName}.findList
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(Params<T> params,Sort ...sort){
		Results<List<T>> results=Results.success();
		SqlId sqlId=SqlId.of(this.getNameSpace(params.getBody().getClass()), "findPages");
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		this.setDataPermis(params);
		
		if(params.isNeedCount()) {
			Long total = this.sqlManager.selectUnique(SqlId.of(this.getNameSpace(params.getBody().getClass()), "count"), map, Long.class);
			results.setTotal(total);
		}
		
		// 如果未手动设置排序，则从params中获取排序
		if(sort.length==0 && !ObjectUtil.isEmpty(params.getSorts())) {
			List<Sort> sorts = params.getSorts().stream()
				.filter(s->s!=null)
				.map(s->Sort.build(s.getName(), s.isAsc()?"ASC":"DESC"))
				.collect(Collectors.toList());
			if(!sorts.isEmpty()) {
				sort=sorts.toArray(new Sort[] {});
			}
		}
		
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", params.getKeywords());
		query.put("id", params.getId());
		query.put("ids", params.getIds());
		map.put("query", query);
		
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
	
	
	private void setDataPermis(Object obj) {
		UniDataPermis dataPermis = obj.getClass().getAnnotation(UniDataPermis.class);
		if(dataPermis!=null && !dataPermis.value().equals(DataPermis.ALL)) {
			SessionService sessionService=SessionHolder.build();
			switch (dataPermis.value()) {
			case TENANTID:
				BeanUtils.setDefaultValue(obj, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
				break;
			case ORGANID:
				BeanUtils.setDefaultValue(obj, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
				break;	
			case ORGANCODE:
				BeanUtils.setDefaultValue(obj, BaseField.ORGAN_CODE.getName(), sessionService.getOrgLvsn());
				break;		
			default:
				BeanUtils.setDefaultValue(obj, BaseField.USER_ID.getName(), sessionService.getUserId());
				break;
			}
		}
	}
	
	/**
	 * 	加载SQL
	 * @param <T>
	 * @param builder
	 * @param name
	 * @return
	 */
	private <T> SqlId loadSql(SqlBuilder<T> builder,SqlType type) {
		builder.init(this.sqlManager);
		SqlId sqlid=SqlId.of(builder.nameSpace(), builder.sqlId(type));
		if(this.sqlManager.containSqlId(sqlid)) {
			return sqlid;
		}else {
			SQLSource tempSource = this.sqlManager.getSqlLoader().queryAutoSQL(sqlid);
			if(tempSource==null) {
				tempSource=new SQLSource(sqlid, builder.toSql(type));
				tempSource.setSqlType(SQLType.valueOf(type.value()));
				tempSource.setAutoGenerated(true);
				ExecuteContext context = ExecuteContext.instance(this.sqlManager);
				context.initSQLSource(tempSource);
				this.sqlManager.getSqlLoader().addAutoGenSQL(sqlid, tempSource);
			}
		}
		return sqlid;
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
