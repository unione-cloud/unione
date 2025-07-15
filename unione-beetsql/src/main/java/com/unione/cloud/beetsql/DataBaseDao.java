package com.unione.cloud.beetsql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.beetl.sql.clazz.SQLType;
import org.beetl.sql.core.ExecuteContext;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.SqlId;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.beetsql.builder.SqlEntity;
import com.unione.cloud.beetsql.builder.SqlField;
import com.unione.cloud.beetsql.builder.SqlKit;
import com.unione.cloud.beetsql.builder.SqlType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
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

		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, entity.getClass());
		SqlField idField=sqlEntity.getKeyField();
		AssertUtil.database().notNull(idField, "未设置主键字段");
		BeanUtils.setFieldValue(entity, idField.getAlias(), IdGenHolder.generate());
		
		SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
		if(delFlag!=null) {
			BeanUtils.setDefaultValue(entity, delFlag.getAlias(),0);
		}
		SqlField tenantId=sqlEntity.getStsField(BaseField.TENANT_ID);
		if(tenantId!=null) {
			BeanUtils.setDefaultValue(entity, tenantId.getAlias(), sessionService.getTenantId());
		}
		SqlField organId=sqlEntity.getStsField(BaseField.ORGAN_ID);
		if(organId!=null) {
			BeanUtils.setDefaultValue(entity, organId.getAlias(), sessionService.getOrgId());
		}
		SqlField userId=sqlEntity.getStsField(BaseField.USER_ID);
		if(userId!=null) {
			BeanUtils.setDefaultValue(entity, userId.getAlias(), sessionService.getUserId());
		}
		SqlField created=sqlEntity.getStsField(BaseField.CREATED);
		if(created!=null) {
			BeanUtils.setDefaultValue(entity, created.getAlias(), DateUtil.date());
		}
		SqlField createdBy=sqlEntity.getStsField(BaseField.CREATED_BY);
		if(createdBy!=null) {
			BeanUtils.setDefaultValue(entity, createdBy.getAlias(), sessionService.getUserId());
		}
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(entity, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(entity, lastUpdatedBy.getAlias(), sessionService.getUserId());	
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
		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, entity.getClass());
		SqlField idField=sqlEntity.getKeyField();
		AssertUtil.database().notNull(idField, "未设置主键字段");

		SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
		if(delFlag!=null) {
			BeanUtils.setDefaultValue(entity, delFlag.getAlias(),0);
		}
		SqlField tenantId=sqlEntity.getStsField(BaseField.TENANT_ID);
		if(tenantId!=null) {
			BeanUtils.setDefaultValue(entity, tenantId.getAlias(), sessionService.getTenantId());
		}
		SqlField organId=sqlEntity.getStsField(BaseField.ORGAN_ID);
		if(organId!=null) {
			BeanUtils.setDefaultValue(entity, organId.getAlias(), sessionService.getOrgId());
		}
		SqlField userId=sqlEntity.getStsField(BaseField.USER_ID);
		if(userId!=null) {
			BeanUtils.setDefaultValue(entity, userId.getAlias(), sessionService.getUserId());
		}
		SqlField created=sqlEntity.getStsField(BaseField.CREATED);
		if(created!=null) {
			BeanUtils.setDefaultValue(entity, created.getAlias(), DateUtil.date());
		}
		SqlField createdBy=sqlEntity.getStsField(BaseField.CREATED_BY);
		if(createdBy!=null) {
			BeanUtils.setDefaultValue(entity, createdBy.getAlias(), sessionService.getUserId());
		}
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(entity, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(entity, lastUpdatedBy.getAlias(), sessionService.getUserId());	
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
		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, list.get(0).getClass());
		SqlField idField=sqlEntity.getKeyField();
		AssertUtil.database().notNull(idField, "未设置主键字段");

		list.stream().forEach(entity->{
			BeanUtils.setFieldValue(entity, idField.getAlias(), IdGenHolder.generate());
			
			SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
			if(delFlag!=null) {
				BeanUtils.setDefaultValue(entity, delFlag.getAlias(),0);
			}
			SqlField tenantId=sqlEntity.getStsField(BaseField.TENANT_ID);
			if(tenantId!=null) {
				BeanUtils.setDefaultValue(entity, tenantId.getAlias(), sessionService.getTenantId());
			}
			SqlField organId=sqlEntity.getStsField(BaseField.ORGAN_ID);
			if(organId!=null) {
				BeanUtils.setDefaultValue(entity, organId.getAlias(), sessionService.getOrgId());
			}
			SqlField userId=sqlEntity.getStsField(BaseField.USER_ID);
			if(userId!=null) {
				BeanUtils.setDefaultValue(entity, userId.getAlias(), sessionService.getUserId());
			}
			SqlField created=sqlEntity.getStsField(BaseField.CREATED);
			if(created!=null) {
				BeanUtils.setDefaultValue(entity, created.getAlias(), DateUtil.date());
			}
			SqlField createdBy=sqlEntity.getStsField(BaseField.CREATED_BY);
			if(createdBy!=null) {
				BeanUtils.setDefaultValue(entity, createdBy.getAlias(), sessionService.getUserId());
			}
			SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
			if(lastUpdated!=null) {
				BeanUtils.setDefaultValue(entity, lastUpdated.getAlias(), DateUtil.date());
			}
			SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
			if(lastUpdatedBy!=null) {
				BeanUtils.setDefaultValue(entity, lastUpdatedBy.getAlias(), sessionService.getUserId());	
			}
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
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, list.get(0).getClass());
		SqlField idField=sqlEntity.getKeyField();
		AssertUtil.database().notNull(idField, "未设置主键字段");

		list.stream().forEach(entity->{
			SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
			if(delFlag!=null) {
				BeanUtils.setDefaultValue(entity, delFlag.getAlias(),0);
			}
			SqlField tenantId=sqlEntity.getStsField(BaseField.TENANT_ID);
			if(tenantId!=null) {
				BeanUtils.setDefaultValue(entity, tenantId.getAlias(), sessionService.getTenantId());
			}
			SqlField organId=sqlEntity.getStsField(BaseField.ORGAN_ID);
			if(organId!=null) {
				BeanUtils.setDefaultValue(entity, organId.getAlias(), sessionService.getOrgId());
			}
			SqlField userId=sqlEntity.getStsField(BaseField.USER_ID);
			if(userId!=null) {
				BeanUtils.setDefaultValue(entity, userId.getAlias(), sessionService.getUserId());
			}
			SqlField created=sqlEntity.getStsField(BaseField.CREATED);
			if(created!=null) {
				BeanUtils.setDefaultValue(entity, created.getAlias(), DateUtil.date());
			}
			SqlField createdBy=sqlEntity.getStsField(BaseField.CREATED_BY);
			if(createdBy!=null) {
				BeanUtils.setDefaultValue(entity, createdBy.getAlias(), sessionService.getUserId());
			}
			SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
			if(lastUpdated!=null) {
				BeanUtils.setDefaultValue(entity, lastUpdated.getAlias(), DateUtil.date());
			}
			SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
			if(lastUpdatedBy!=null) {
				BeanUtils.setDefaultValue(entity, lastUpdatedBy.getAlias(), sessionService.getUserId());	
			}
		});
		
		return this.sqlManager.insertBatch(list.get(0).getClass(),list);
	}

	/**
	 * 	更新数据,使用sql更新{ResoruceName}.update
	 * @param updater
	 * @return
	 */
	public <T> int update(Updater<T> updater) {
		return update("update", updater);
	}
	
	/**
	 * 	更新数据,使用sql更新{ResoruceName}.{sqlName}
	 * @param updater
	 * @return
	 */
	public <T> int update(String sqlName, Updater<T> updater) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(updater.getData().getClass()), sqlName);

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, updater.getData().getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(updater.getData(), lastUpdated.getAlias(), DateUtil.date());
			updater.getFields().put(lastUpdated.getAlias(), true);
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(updater.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			updater.getFields().put(lastUpdatedBy.getAlias(), true);
		}
		updater.setPrincipal(sessionService.getPrincipal());

		this.setDataPermis(updater.getData());
		return this.sqlManager.update(sqlId, updater);
	}
	
	/**
	 * 	更新数据
	 * @param builder
	 * @return
	 */
	public <T> int update(SqlBuilder<T> builder) {
		builder.init(this.sqlManager);

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=builder.getEntity();
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			sqlEntity.getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			sqlEntity.getFieldList().add(lastUpdatedBy.getAlias());
		}
		
		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE);
		return this.sqlManager.update(sqlId, builder.toParams());
	}

	/**
	 * 	更新数据,使用sql更新{ResoruceName}.updateById
	 * @param updater
	 * @return
	 */
	public <T> int updateById(Updater<T> updater) {
		return updateById("updateById", updater);
	}
	
	/**
	 * 	更新数据,使用sql更新{ResoruceName}.{sqlName}
	 * @param updater
	 * @return
	 */
	public <T> int updateById(String sqlName, Updater<T> updater) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(updater.getData().getClass()), sqlName);

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, updater.getData().getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(updater.getData(), lastUpdated.getAlias(), DateUtil.date());
			updater.getFields().put(lastUpdated.getAlias(), true);
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(updater.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			updater.getFields().put(lastUpdatedBy.getAlias(), true);
		}
		updater.setPrincipal(sessionService.getPrincipal());
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
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, params.getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(params, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(params, lastUpdatedBy.getAlias(), sessionService.getUserId());	
		}
		
		return this.sqlManager.updateById(params);
	}
	
	/**
	 * 	更新数据
	 * @param <T>
	 * @param builder
	 * @return
	 */
	public <T> int updateById(SqlBuilder<T> builder) {
		builder.init(this.sqlManager);

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=builder.getEntity();
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			sqlEntity.getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			sqlEntity.getFieldList().add(lastUpdatedBy.getAlias());
		}

		SqlId sqlId=this.loadSql(builder, SqlType.UPDATE_BYID);
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	删除数据,使用sql删除{ResoruceName}.delete
	 * @param params
	 * @return
	 */
	public <T> int delete(T params) {
		return delete("delete", params);
	}

	/**
	 * 	删除数据,使用sql删除{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	public <T> int delete(String sqlName,T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, params.getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(params, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(params, lastUpdatedBy.getAlias(), sessionService.getUserId());	
		}
		map.put("principal", sessionService.getPrincipal());
		
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
		SqlEntity sqlEntity=builder.getEntity();
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			sqlEntity.getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			sqlEntity.getFieldList().add(lastUpdatedBy.getAlias());
		}
		
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
		SqlEntity sqlEntity=builder.getEntity();
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			sqlEntity.getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			sqlEntity.getFieldList().add(lastUpdatedBy.getAlias());
		}
		
		return this.sqlManager.update(sqlId,builder.toParams());
	}

	/**
	 * 	逻辑删除,使用sql统计{ResoruceName}.deleteLogic
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(T params) {
		return deleteLogic("deleteLogic", params);
	}
	
	/**
	 * 	逻辑删除,使用sql统计{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(String sqlName,T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, params.getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(params, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(params, lastUpdatedBy.getAlias(), sessionService.getUserId());	
		}
		map.put("principal", sessionService.getPrincipal());
		
		return this.sqlManager.update(sqlId, map);
	}


	/**
	 * 	逻辑删除(根据id或ids集合删除数据),使用sql统计{ResoruceName}.deleteLogicById
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(T params) {
		return deleteLogicById("deleteLogicById", params);
	}
	
	/**
	 * 	逻辑删除(根据id或ids集合删除数据),使用sql统计{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(String sqlName,T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, params.getClass());
		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(params, lastUpdated.getAlias(), DateUtil.date());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(params, lastUpdatedBy.getAlias(), sessionService.getUserId());	
		}
		map.put("principal", sessionService.getPrincipal());
		
		return this.sqlManager.update(sqlId, map);
	}
	
	/**
	 * 	逻辑删除
	 * @param params
	 * @return
	 */
	public <T> int deleteLogic(SqlBuilder<T> builder) {

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, builder.getData().getClass());
		SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
		AssertUtil.database().notNull(delFlag, "未设置逻辑删除字段");
		BeanUtils.setDefaultValue(builder.getData(), delFlag.getAlias(), 1);

		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			builder.getEntity().getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			builder.getEntity().getFieldList().add(lastUpdatedBy.getAlias());
		}

		SqlId sqlId=this.loadSql(builder, SqlType.DELETE_LOGIC);
		return this.sqlManager.update(sqlId, builder.toParams());
	}
	
	/**
	 * 	逻辑删除(根据id或ids集合删除数据)
	 * @param params
	 * @return
	 */
	public <T> int deleteLogicById(SqlBuilder<T> builder) {

		SessionService sessionService=SessionHolder.build();
		SqlEntity sqlEntity=SqlKit.buildEntity(sqlManager, builder.getData().getClass());
		SqlField delFlag=sqlEntity.getStsField(BaseField.DEL_FLAG);
		AssertUtil.database().notNull(delFlag, "未设置逻辑删除字段");
		BeanUtils.setDefaultValue(builder.getData(), delFlag.getAlias(), 1);
		builder.getEntity().getFieldList().add(delFlag.getAlias());

		SqlField lastUpdated=sqlEntity.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdated!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdated.getAlias(), DateUtil.date());
			builder.getEntity().getFieldList().add(lastUpdated.getAlias());
		}
		SqlField lastUpdatedBy=sqlEntity.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedBy!=null) {
			BeanUtils.setDefaultValue(builder.getData(), lastUpdatedBy.getAlias(), sessionService.getUserId());	
			builder.getEntity().getFieldList().add(lastUpdatedBy.getAlias());
		}

		SqlId sqlId=this.loadSql(builder, SqlType.DELETE_LOGIC_BYID);
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
		return count("count",params);
	}
	
	/**
	 * 	统计数量,使用sql统计{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	public <T> long count(String sqlName,T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("principal", SessionHolder.build().getPrincipal());
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
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT_ONE);
		return (T) this.sqlManager.selectUnique(sqlId, builder.toParams(), builder.targetClass());
	}

	/**
	 * 	查询唯一数据,使用sql查询{ResoruceName}.findUnique
	 * @param params
	 * @return
	 */
	public <T> T findUnique(T params) {
		return findUnique("findUnique", params);
	}
	
	/**
	 * 	查询唯一数据,使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findUnique(String sqlName, T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("principal", SessionHolder.build().getPrincipal());

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
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT_ONE);
		return (T) this.sqlManager.selectSingle(sqlId, builder.toParams(), builder.targetClass());
	}
	

	/**
	 * 	查询一条数据,使用sql查询{ResoruceName}.findOne
	 * @param params
	 * @return
	 */
	public <T> T findOne(T params) {
		return findOne("findOne", params);
	}

	/**
	 * 	查询一条数据,使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(String sqlName, T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("principal", SessionHolder.build().getPrincipal());

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
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT_BYID);
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
	public <T> T findById(T params) {
		return findById("findById", params);
	}
	
	/**
	 * 	查询列表(根据id查询数据),使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findById(String sqlName, T params) {
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("principal", SessionHolder.build().getPrincipal());

		this.setDataPermis(params);
		return (T) this.sqlManager.selectSingle(sqlId, map, params.getClass());
	}
	

	/**
	 * 	查询列表(根据ids集合加载数据),使用sql查询{ResoruceName}.findById
	 * @param params
	 * @param sort
	 * @return
	 */
	public <T> List<T> findByIds(T params,Sort ...sort){
		return findByIds("findByIds", params, sort);
	}
	
	/**
	 * 	查询列表(根据ids集合加载数据),使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findByIds(String sqlName, T params,Sort ...sort){
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		map.put("principal", SessionHolder.build().getPrincipal());

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
		SqlId sqlId=this.loadSql(builder, SqlType.SELECT_BYID);
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
	 * 	查询列表(不分页),使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(String sqlName, SqlBuilder<T> builder){
		SqlId findsql=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(builder.targetClass()), sqlName);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.targetClass(), builder.toParams());
		return rows;
	}

	/**
	 * 	查询列表(不分页),使用sql查询{ResoruceName}.findList
	 * @param params
	 * @param sort
	 * @return
	 */
	public <T> List<T> findList(T params,Sort ...sort){
		return findList("findList", params, sort);
	}
	

	/**
	 * 	查询列表(不分页),使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findList(String sqlName, T params,Sort ...sort){
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);
		
		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", BeanUtil.getFieldValue(params, "keywords"));
		query.put("id", BeanUtil.getFieldValue(params, "id"));
		query.put("ids", BeanUtil.getFieldValue(params, "ids"));
		map.put("query", query);
		map.put("principal", SessionHolder.build().getPrincipal());
		
		this.setDataPermis(params);
		return (List<T>) this.sqlManager.select(sqlId, params.getClass(), map);
	}


	public <T> List<T> findList(String sqlName, Object params,Class<T> resultClass,Sort ...sort){
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);
		if(params instanceof Map){
			sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(resultClass), sqlName);
		}

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		map.put("sorts", (sort.length==0?null:Sort.use(sort)));
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", BeanUtil.getFieldValue(params, "keywords"));
		query.put("id", BeanUtil.getFieldValue(params, "id"));
		query.put("ids", BeanUtil.getFieldValue(params, "ids"));
		map.put("query", query);
		map.put("principal", SessionHolder.build().getPrincipal());

		
		this.setDataPermis(params);
		return (List<T>) this.sqlManager.select(sqlId, resultClass, map);
	}

	/**
	 * 查询数据集合
	 * @param <T>
	 * @param builder
	 * @param keyField
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String,T> findMap(SqlBuilder<T> builder,String keyField){
		Map<String,T> result=new HashMap<>();
		SqlId findsql=this.loadSql(builder, SqlType.SELECT);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.targetClass(), builder.toParams());
		for(T m:rows) {
			result.put(BeanUtil.getFieldValue(m, keyField).toString(), m);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public Map<String,Map> findMap(String sqlName, Object params,String keyField){
		return findMap(sqlName, params, Map.class, keyField);
	}

	public <T> Map<String,T> findMap(String sqlName, Object params,Class<T> resultType,String keyField){
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getClass()), sqlName);
		if(params instanceof Map){
			sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(resultType), sqlName);	
		}

		Map<String,Object> map = new HashMap<>();
		map.put("params", params);
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", BeanUtil.getFieldValue(params, "keywords"));
		query.put("id", BeanUtil.getFieldValue(params, "id"));
		query.put("ids", BeanUtil.getFieldValue(params, "ids"));
		map.put("query", query);
		map.put("principal", SessionHolder.build().getPrincipal());

		
		this.setDataPermis(params);
		List<T> list =  this.sqlManager.select(sqlId, resultType, map);
		Map<String,T> result=new HashMap<>();
		for(T m:list) {
			result.put(BeanUtil.getFieldValue(m, keyField).toString(), m);
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
	public <T> List<T> findPageList(Params<T> params,Sort ...sort){
		return findPageList("findList", params, sort);
	}
	
	/**
	 * 	查询列表(分页),不执行total统计,使用sql查询{ResoruceName}.{sqlName}
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findPageList(String sqlName, Params<T> params,Sort ...sort){
		SqlId sqlId=sqlName.indexOf(".")>0?SqlId.of(sqlName):SqlId.of(this.getNameSpace(params.getBody().getClass()), sqlName);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		map.put("keywords", params.getKeywords());
		
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
		map.put("principal", SessionHolder.build().getPrincipal());

		
		this.setDataPermis(params);
		return (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
	}
	

	/**
	 * 	查询列表(分页),使用sql查询{ResoruceName}.count，{ResoruceName}.findList
	 * @param params
	 * @param sort
	 * @return
	 */
	public <T> Results<List<T>> findPages(Params<T> params,Sort ...sort){
		return findPages("findPages","count", params, sort);
	}

	/**
	 * 	查询列表(分页),使用sql查询{ResoruceName}.{countSql}，{ResoruceName}.{listSql}
	 * @param params
	 * @param sort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(String listSql,String countSql,Params<T> params,Sort ...sort){
		Results<List<T>> results=Results.success();
		SqlId sqlId=listSql.indexOf(".")>0?SqlId.of(listSql):SqlId.of(this.getNameSpace(params.getBody().getClass()), listSql);

		Map<String,Object> map = new HashMap<>();
		map.put("params", params.getBody());
		map.put("keywords", params.getKeywords());
		this.setDataPermis(params);
		
		if(params.isNeedCount()) {
			SqlId sqlCount=countSql.indexOf(".")>0?SqlId.of(countSql):SqlId.of(this.getNameSpace(params.getBody().getClass()), countSql);
			Integer total = this.sqlManager.selectUnique(sqlCount, map, Integer.class);
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
		map.put("principal", SessionHolder.build().getPrincipal());
		
		List<T> rows = (List<T>) this.sqlManager.select(sqlId, map, params.getBody().getClass(),(long)params.getStart()+1,(long)params.getPageSize());
		results.setPage(params.getPage());
		results.setPageSize(params.getPageSize());
		return results.setBody(rows);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(SqlBuilder<T> builder){
		Results<List<T>> results=Results.success();
		
		// count 统计
		if(builder.isNeedCount()) {
			SqlId countsql=this.loadSql(builder, SqlType.COUNT);
			Integer total = this.sqlManager.selectUnique(countsql, builder.toParams(), Integer.class);
			results.setTotal(total);
		}
		
		// 数据查询
		SqlId findsql=this.loadSql(builder, SqlType.SELECT);
		List<T> rows = (List<T>) this.sqlManager.select(findsql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
		
		results.setPage(builder.getPage());
		results.setPageSize(builder.getPageSize());
		return results.setBody(rows);
	}

	@SuppressWarnings("unchecked")
	public <T> Results<List<T>> findPages(String listSql,String countSql,SqlBuilder<T> builder){
		SqlId $listSql=listSql.indexOf(".")>0?SqlId.of(listSql):SqlId.of(this.getNameSpace(builder.targetClass()), listSql);
		SqlId $countSql=countSql.indexOf(".")>0?SqlId.of(countSql):SqlId.of(this.getNameSpace(builder.targetClass()), countSql);
		Results<List<T>> results=Results.success();
		
		// count 统计
		if(builder.isNeedCount()) {
			Integer total = this.sqlManager.selectUnique($countSql, builder.toParams(), Integer.class);
			results.setTotal(total);
		}
		
		// 数据查询
		List<T> rows = (List<T>) this.sqlManager.select($listSql, builder.toParams(), builder.targetClass(),builder.getStart()+1,builder.getPageSize());
		
		results.setPage(builder.getPage());
		results.setPageSize(builder.getPageSize());
		return results.setBody(rows);
	}

	
	private void setDataPermis(Object obj) {
		DataPermis dataPermis = obj.getClass().getAnnotation(DataPermis.class);
		if(dataPermis!=null && !dataPermis.value().equals(PermisRule.ALL)) {
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
