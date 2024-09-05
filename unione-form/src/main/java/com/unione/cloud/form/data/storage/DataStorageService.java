package com.unione.cloud.form.data.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.SidGenHolder;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.form.cache.DataModelCache;
import com.unione.cloud.form.data.storage.model.DataCommit;
import com.unione.cloud.form.data.storage.model.DataField;
import com.unione.cloud.form.data.storage.model.DataFieldConfig;
import com.unione.cloud.form.data.storage.model.DataFind;
import com.unione.cloud.form.data.storage.model.DataModel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 	数据存储服务 
 * 1、支撑用户对数据进行CRUD 
 * 2、根据用户所拥有的数据权限自动过滤数据
 * @author Jeking 杨
 */
@Slf4j
@Service
@RefreshScope
public class DataStorageService {

	@Autowired
	private StorageBaseService storageBaseService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DataModelCache dataModelCache;
	
	
	/**
	 * 	插入数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int insert(DataModel dataModel, Map<String, Object> data) {
		return insert(dataModel.getDsId(), dataModel, data);
	}

	/**
	 * 	插入数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int insert(Long dsId, DataModel dataModel, Map<String, Object> data) {
		Map<String, Object> params=new HashMap<>();
		params.put("data", data);
		processInsertDefaultData(dataModel,data);
		return storageBaseService.insert(dsId, dataModel.getSqlInsert(), params);
	}
	
	/**
	 * 	插入数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int insert(DataModel dataModel,DataCommit commit) {
		return insert(dataModel, commit.getData());
	}


	/**
	 * 	批量插入数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int[] insert(DataModel dataModel, List<Map<String, Object>> datas) {
		return insert(dataModel.getDsId(), dataModel, datas);
	}

	/**
	 * 	批量插入数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int[] insert(Long dsId, DataModel dataModel, List<Map<String, Object>> datas) {
		datas.stream().forEach(row->{
			processInsertDefaultData(dataModel, row);
		});
		AssertUtil.database().isTrue(false, "暂未实现");
		return null;
	}
	
	
	/**
	 * 新增操作默认数据处理
	 * @param dsId
	 * @param dataModel
	 * @param data
	 */
	public void processInsertDefaultData(DataModel dataModel,Map<String, Object> data) {
		log.debug("新增操作默认数据处理,dsId:{},data model id:{}",dataModel.getDsId(),dataModel.getId());
		DataField idField = dataModel.getStsField(BaseField.ID);
		if (idField != null && data.get(idField.getAlias())==null) {
			data.put(idField.getAlias(), SidGenHolder.generate());
		}
		DataField tenantIdField = dataModel.getStsField(BaseField.TENANT_ID);
		if (tenantIdField != null) {
			data.put(tenantIdField.getAlias(),sessionService.getTenantId());
		}
		DataField orgIdField=dataModel.getStsField(BaseField.ORGAN_ID);
		if (orgIdField!=null){
			data.put(orgIdField.getAlias(), sessionService.getOrgId());
		}
		DataField orgCodeField=dataModel.getStsField(BaseField.ORGAN_CODE);
		if (orgCodeField!=null){
			data.put(orgCodeField.getAlias(), sessionService.getOrgLvsn());
		}
		DataField areaCodeField=dataModel.getStsField(BaseField.AREA_CODE);
		if (areaCodeField!=null){
			data.put(areaCodeField.getAlias(), sessionService.getAreaCode());
		}
		DataField userIdField=dataModel.getStsField(BaseField.USER_ID);
		if (userIdField!=null){
			data.put(userIdField.getAlias(), sessionService.getUserId());
		}
		DataField delFlagField=dataModel.getStsField(BaseField.DEL_FLAG);
		if (delFlagField!=null){
			data.put(delFlagField.getAlias(), 0);
		}
		DataField createdField=dataModel.getStsField(BaseField.CREATED);
		if (createdField!=null){
			if("Date".equalsIgnoreCase(createdField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(createdField.getDataType())) {
				data.put(createdField.getAlias(), DateUtil.date());
			}else {
				data.put(createdField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataField createdByField=dataModel.getStsField(BaseField.CREATED_BY);
		if(createdByField!=null){
			data.put(createdByField.getAlias(), sessionService.getUserId());
		}
		DataField lastUpdatedField=dataModel.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdatedField!=null){
			if("Date".equalsIgnoreCase(lastUpdatedField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(lastUpdatedField.getDataType())) {
				data.put(lastUpdatedField.getAlias(), DateUtil.date());
			}else {
				data.put(lastUpdatedField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataField lastUpdatedByField=dataModel.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedByField!=null){
			data.put(lastUpdatedByField.getAlias(), sessionService.getUserId());
		}
	}

	
	/**
	 * 	更新数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int update(DataModel dataModel, Map<String, Object> data, Map<String, Object> params,String... fields) {
		return update(dataModel.getDsId(), dataModel, data,params,fields);
	}

	/**
	 * 	更新数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int update(Long dsId, DataModel dataModel, Map<String, Object> data, Map<String, Object> params,String... fields) {
		Map<String, Boolean> fieldMap = new HashMap<>();
		processUpdateDeleteDefaultData(dataModel,data);
		if(fields.length>0) {
			for(String field:fields) {
				fieldMap.put(field, true);
			}
		}else {
			data.keySet().forEach(field->{
				fieldMap.put(field, true);
			});
		}
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", data);
		paramObj.put("params", params);
		paramObj.put("fields", fieldMap);
		return storageBaseService.update(dsId, dataModel.getSqlUpdate(), paramObj);
	}
	
	
	/**
	 * 	更新数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int updateById(DataModel dataModel,DataCommit commit,String ...fields) {
		return updateById(dataModel.getDsId(),dataModel, commit,fields);
	}
	
	
	/**
	 * 	更新数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int updateById(Long dsId,DataModel dataModel,DataCommit commit,String ...fields) {
		DataField sidField=dataModel.getStsField(BaseField.ID);
		AssertUtil.service().notNull(commit.getId(), "主键不能为空").notNull(sidField, "未配置主键字段");
		Map<String, Object> params=new HashMap<>();
		Map<String, Object> fieldMap=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", commit.getData());
		paramObj.put("params", params);
		paramObj.put("fields", fieldMap);
		params.put(sidField.getName(), commit.getId());
		
		processUpdateDeleteDefaultData(dataModel,commit.getData());
		
		if(fields.length>0) {
			for(String field:fields) {
				fieldMap.put(field, true);
			}
		}else {
			for(String field:commit.getData().keySet()) {
				fieldMap.put(field, true);
			}
		}
		
		return storageBaseService.update(dsId, dataModel.getSqlUpdate(), paramObj);
	}
	
	
	/**
	 * 	更新/删除操作默认数据处理
	 * @param dsId
	 * @param dataModel
	 * @param data
	 */
	public void processUpdateDeleteDefaultData(DataModel dataModel,Map<String, Object> data) {
		log.debug("新增操作默认数据处理,dsId:{},data model id:{}",dataModel.getDsId(),dataModel.getId());
		DataField lastUpdatedField=dataModel.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdatedField!=null){
			if("Date".equalsIgnoreCase(lastUpdatedField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(lastUpdatedField.getDataType())) {
				data.put(lastUpdatedField.getAlias(), DateUtil.date());
			}else {
				data.put(lastUpdatedField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataField lastUpdatedByField=dataModel.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedByField!=null){
			data.put(lastUpdatedByField.getAlias(), sessionService.getUserId());
		}
	}

	
	/**
	 * 	删除数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int delete(DataModel dataModel, Map<String, Object> params) {
		return delete(dataModel.getDsId(), dataModel, params);
	}
	
	
	/**
	 * 	删除数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int delete(Long dsId,DataModel dataModel, Map<String, Object> params) {
		Map<String, Object> data=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", data);
		paramObj.put("params", params);
		
		processUpdateDeleteDefaultData(dataModel,data);
		
		return storageBaseService.delete(dsId, dataModel.getSqlDelete(), params);
	}
	
	
	/**
	 * 	删除数据:物理/逻辑删除
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int deleteByIds(DataModel dataModel,DataCommit commit) {
		return deleteByIds(dataModel.getDsId(),dataModel, commit);
	}
	
	
	/**
	 * 	删除数据:物理/逻辑删除
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public int deleteByIds(Long dsId,DataModel dataModel,DataCommit commit) {
		DataField delFlagField=dataModel.getStsField(BaseField.DEL_FLAG);
		AssertUtil.service().notEmpty(commit.getIds(), "主键集合不能为空");
		Map<String, Object> data=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		Map<String, Object> fieldMap=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", data);
		paramObj.put("params", params);
		paramObj.put("fields", fieldMap);
		
		params.put("dsIds", commit.getIds());
		data.put(delFlagField.getName(), 1);
		for(String field:data.keySet()) {
			fieldMap.put(field, true);
		}
		
		processUpdateDeleteDefaultData(dataModel,data);
		
		return storageBaseService.delete(dsId, delFlagField!=null?dataModel.getSqlUpdate():dataModel.getSqlDelete(), paramObj);
	}
	
	

	/**
	 * 	查询一条数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public Map<String, Object> findOne(DataModel dataModel, Map<String, Object> params) {
		return findOne(dataModel.getDsId(),dataModel, params);
	}
	

	/**
	 * 	查询一条数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public Map<String, Object> findOne(Long dsId,DataModel dataModel, Map<String, Object> params) {
		String sql=dataModel.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataModel, params);
		
		return storageBaseService.findOne(dsId, sql, paramObj,dataModel.getFields());
	}
	
	
	/**
	 * 	根据主键查询数据
	 * @param dataModel
	 * @param id
	 * @return
	 */
	public Map<String, Object> findById(DataModel dataModel,Object id) {
		return findById(dataModel.getDsId(),dataModel, id);
	}
	

	/**
	 * 	根据主键查询数据
	 * @param dataModel
	 * @param id
	 * @return
	 */
	public Map<String, Object> findById(Long dsId,DataModel dataModel,Object id) {
		String sql=dataModel.getSqlFind().replace("#{page('*')}", "*");
		DataField idField = dataModel.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(idField!=null?idField.getAlias():"id", id);
		paramObj.put("params", params);
		
		processFindDefaultParams(dataModel, params);
		
		return storageBaseService.findOne(dsId, sql, paramObj,dataModel.getFields());
	}
	
	
	/**
	 * 	根据主键集合查询数据
	 * 
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findByIds(DataModel dataModel,Set<Object> ids) {
		return findByIds(dataModel.getDsId(),dataModel, ids);
	}
	

	/**
	 * 	根据主键集合查询数据
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findByIds(Long dsId,DataModel dataModel,Set<Object> ids) {
		AssertUtil.service().notEmpty(ids, "主键不能为空");
		DataField idField = dataModel.getStsField(BaseField.ID);
		
		String sql=dataModel.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"), ids);
		
		paramObj.put("params", params);
		processFindDefaultParams(dataModel, params);
		
		return storageBaseService.findList(dsId, sql, paramObj,dataModel.getFields());
	}
	

	/**
	 * 	查询数据列表，未分页
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findList(DataModel dataModel, Map<String, Object> params) {
		return findList(dataModel.getDsId(), dataModel, params);
	}

	
	/**
	 * 	查询数据列表，未分页
	 * @param dataModel
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findList(Long dsId, DataModel dataModel, Map<String, Object> params) {
		String sql=dataModel.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataModel, params);
		
		return storageBaseService.findList(dsId, sql, paramObj,dataModel.getFields());
	}

	/**
	 * 	查询数据列表，分页
	 * @param dataModel
	 * @param params
	 * @param page
	 * @param size
	 * @return
	 */
	public Results<List<Map<String, Object>>> findListPage(DataModel dataModel, Map<String, Object> params,
			int page, int size) {
		return findListPage(dataModel.getDsId(), dataModel, params, page, size);
	}

	/**
	 * 	查询数据列表，分页
	 * @param dataModel
	 * @param params
	 * @param page
	 * @param size
	 * @return
	 */
	public Results<List<Map<String, Object>>> findListPage(Long dsId,DataModel dataModel,
			Map<String, Object> params, int page, int size) {
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataModel, params);
		
		return storageBaseService.findListPage(dsId, dataModel.getSqlFind(), paramObj, page, size,dataModel.getFields());
	}
	
	
	/**
	 * 	查询数据列表，分页
	 * @param dataModel
	 * @param find
	 * @return
	 */
	public Results<List<Map<String, Object>>> findListPage(DataModel dataModel,DataFind find) {
		return findListPage(dataModel.getDsId(),dataModel, find);
	}

	/**
	 * 	查询数据列表，分页
	 * @param dsId
	 * @param dataModel
	 * @param find
	 * @return
	 */
	public Results<List<Map<String, Object>>> findListPage(Long dsId,DataModel dataModel,DataFind find) {
		Map<String, Object> paramObj=new HashMap<>();
		
		String keywords=StringUtils.trimToNull(find.getKeywords());
		if(keywords!=null) {
			find.getBody().put("keywords", keywords);
		}
		paramObj.put("params", find.getBody());
		String sorts=find.getSortText();
		if(!StringUtils.isEmpty(sorts)) {
			paramObj.put("sorts", sorts);
		}
		
		processFindDefaultParams(dataModel, find.getBody());
		
		return storageBaseService.findListPage(dsId, dataModel.getSqlFind(), paramObj, find.getPage(),find.getPageSize(), dataModel.getFields());
	}
	
	
	/**
	 * 	查询操作默认参数
	 * @param dataModel
	 * @param data
	 */
	public void processFindDefaultParams(DataModel dataModel,Map<String, Object> data) {
		log.debug("查询操作默认参数处理,data model id:{}",dataModel.getId());
		
		
		
	}
	
	/**
	 * 	根据外键字段从外键数据表中加载数据
	 * @param dataModel
	 * @param fkey
	 * @param vlaues
	 * @return
	 */
	public Map<String, Map<String, Object>> loadFkeyEntrys(DataModel dataModel,DataField fkey,Set<Object> vlaues){
		return loadFkeyEntrys(dataModel.getDsId(), fkey, vlaues);
	}
	
	
	/**
	 * 	根据外键字段从外键数据表中加载数据
	 * @param dsId
	 * @param fkey
	 * @param vlaues
	 * @return
	 */
	public Map<String, Map<String, Object>> loadFkeyEntrys(Long dsId,DataField fkey,Set<Object> vlaues){
		Map<String, Map<String, Object>> map=new HashMap<>();
		String sql = this.buildFkeyQuerySql(fkey);
		if(StringUtils.isEmpty(sql)) {
			return map;
		}
		
		DataModel dataModel = dataModelCache.load(fkey.getFkTableId());
		AssertUtil.service().notNull(dataModel, "外键关联数据定义未找到");
		DataField idField = dataModel.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"), vlaues);
		paramObj.put("params", params);
		
		processFindDefaultParams(dataModel, params);
		
		List<Map<String, Object>> list = storageBaseService.findList(dsId, sql, paramObj,dataModel.getFields());
		
		list.stream().forEach(row->{
			Object fkeyValue=row.get(fkey.getFkFieldName());
			if(fkeyValue!=null) {
				map.put(fkeyValue.toString(), row);
			}
		});
		
		return map;
	}
	
	
	/**
	 * 	加载外键数据集合
	 * @param dataModel
	 * @param rows
	 * @param fkeys
	 */
	public void loadFkeyEntrys(DataModel dataModel,List<Map<String, Object>> rows,String... fkeys) {
		loadFkeyEntrys(dataModel.getDsId(), dataModel, rows, fkeys);
	}
	
	/**
	 * 	加载外键数据集合
	 * @param dataModel
	 * @param rows
	 */
	public void loadFkeyEntrys(Long dsId,DataModel dataModel,List<Map<String, Object>> rows,String... fkeys) {
		log.debug("进入：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataModel.getId(),fkeys,rows.size());
		List<String> fkeyList=new ArrayList<>();
		Map<String, DataField> fkeyFieldMap=new HashMap<>();
		for(String field:fkeys) {
			fkeyList.add(field);
		}
		dataModel.getFields().stream().filter(field->ObjectUtil.equal(1, field.getIsFk()))
			.filter(fkey->!StringUtils.isEmpty(fkey.getName()))
			.filter(fkey->(fkeyList.isEmpty()||fkeyList.contains(fkey.getName()))).forEach(fkey->{
				fkeyFieldMap.put(fkey.getName(), fkey);
			});
		if(fkeyFieldMap.isEmpty()) {
			// 外键字段为空
			log.debug("进入：加载外键数据集合方法,data model id:{},外键字段为空",dataModel.getId());
			return;
		}
		log.debug("开始：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataModel.getId(),fkeyFieldMap.keySet(),rows.size());
		
		Map<String, Set<Object>> fkeyValueMap=new HashMap<>();
		rows.stream().forEach(row->{
			fkeyFieldMap.entrySet().stream().forEach(fkField->{
				Object fkeyValue=row.get(fkField.getKey());
				if(fkeyValue!=null) {
					Set<Object> fkeyvs=fkeyValueMap.get(fkField.getKey());
					if(fkeyvs==null) {
						fkeyvs=new HashSet<>();
						fkeyValueMap.put(fkField.getKey(),fkeyvs);
					}
					fkeyvs.add(fkeyValue);
				}
			});
		});
		
		if(!fkeyValueMap.isEmpty()) {
			// fkeyField=>{fkeyValue:{obj}}
			Map<String, Map<String, Map<String, Object>>> fkeyEntryMap=new HashMap<>();
			fkeyValueMap.entrySet().stream().forEach(entry->{
				DataField fkeyField=fkeyFieldMap.get(entry.getKey());
				Set<Object> fkeyValues=entry.getValue();
				if(fkeyField!=null && fkeyValues!=null && !fkeyValues.isEmpty()) {
					Map<String, Map<String, Object>> fkeyMap = loadFkeyEntrys(dsId,fkeyField,fkeyValues);
					if(fkeyMap!=null && !fkeyMap.isEmpty()) {
						fkeyEntryMap.put(entry.getKey(), fkeyMap);
					}
				}
			});
			// 如果外键数据加载结果不为空，则渲染到数据列表中
			if(!fkeyEntryMap.isEmpty()) {
				rows.stream().forEach(row->{
					fkeyEntryMap.entrySet().stream().forEach(fkeyEntry->{
						Object fkeyValue=row.get(fkeyEntry.getKey());
						DataField fkeyField=fkeyFieldMap.get(fkeyEntry.getKey());
						if(fkeyField!=null && fkeyValue!=null) {
							Map<String, Map<String, Object>> fkeyValueObj=fkeyEntry.getValue();
							if(fkeyValueObj!=null) {
								 Map<String, Object> fkeyvs=fkeyValueObj.get(fkeyValue.toString());
								 if(fkeyvs!=null) {
									 Object fkeyLable=fkeyvs.get(fkeyField.getFkLabelName());
									 if(fkeyLable!=null) {
										 // 设置外键显示字段
										 row.put(String.format("%sLabel", fkeyEntry.getKey()), fkeyLable);
									 }
									 row.put(String.format("%sFEntry", fkeyEntry.getKey()), fkeyvs);
								 }
							}
						}
					});
				});
			}
		}
		
		log.debug("退出：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataModel.getId(),fkeyFieldMap.keySet(),rows.size());
	}
	
	
	
	/**
	 * 	构建外键搜索sql
	 * @param dataDefineDto
	 * @param fkeyName
	 * @return
	 */
	private String buildFkeyQuerySql(DataField fkeyField) {
		if(fkeyField.getConfigs()!=null && !StringUtils.isEmpty(fkeyField.getFkFieldName()) && 
				fkeyField.getFkTableId()!=null && !StringUtils.isEmpty(fkeyField.getFkTableName())) {
			DataFieldConfig fieldConfig=fkeyField.getConfigs();
			if(fieldConfig.getFkey()!=null) {
				StringBuffer sql=new StringBuffer();
				StringBuffer field=new StringBuffer();
				StringBuffer where=new StringBuffer();
				
				field.append(fkeyField.getFkFieldName()).append(",").append(fkeyField.getFkLabelName());
				if(fieldConfig.getFkey().getFkFields()!=null) {
					fieldConfig.getFkey().getFkFields().stream().forEach(fk->{
						field.append(",").append(fk.getName());
					});
				}
				
				where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(fkeyField.getFkFieldName()).append(")){")
					 .append(System.lineSeparator()).append(" AND ").append(fkeyField.getFkFieldName()).append(" = #{params.").append(fkeyField.getFkFieldName()).append("}")
					 .append(System.lineSeparator()).append("-- @}");
				
				where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.ids)){")
				 	 .append(System.lineSeparator()).append(" AND ").append(fkeyField.getFkFieldName()).append(" IN (#{join(params.ids)})")
				 	 .append(System.lineSeparator()).append("-- @}");
				
				where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.keywords)){")
				 	 .append(System.lineSeparator()).append(" AND ").append(fkeyField.getFkLabelName()).append(" LIKE #{'%'+params.keywords+'%'}")
				 	 .append(System.lineSeparator()).append("-- @}");
				
				sql.append("SELECT ").append(field).append(" FROM ").append(fkeyField.getFkTableName()).append(" WHERE 1=1 ").append(where);
				return sql.toString();
			}
		}
		return null;
	}
	

}
