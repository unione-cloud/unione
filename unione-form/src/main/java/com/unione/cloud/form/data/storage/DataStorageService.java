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

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.form.cache.DataDefineCache;
import com.unione.cloud.form.data.dto.DataDefineDto.DataFieldDto;
import com.unione.cloud.form.data.dto.DataDefineDto.ForeignKeyDto;
import com.unione.cloud.form.data.storage.model.DataCommit;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.data.storage.model.DataFind;
import com.unione.cloud.form.data.storage.model.DataLoad;
import com.unione.cloud.form.data.storage.model.DataResult;

import cn.hutool.core.date.DateUtil;
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
	private DataDefineCache dataDefineCache;
	
	
	/**
	 * 	插入数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int insert(DataDefine dataDefine, Map<String, Object> data) {
		return insert(dataDefine.getDsId(), dataDefine, data);
	}

	/**
	 * 	插入数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int insert(Long dsId, DataDefine dataDefine, Map<String, Object> data) {
		Map<String, Object> params=new HashMap<>();
		params.put("data", data);
		processInsertDefaultData(dataDefine,data);
		return storageBaseService.insert(dsId, dataDefine.getSqlInsert(), params);
	}
	
	/**
	 * 	插入数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int insert(DataDefine dataDefine,DataCommit commit) {
		return insert(dataDefine, commit.getData());
	}


	/**
	 * 	批量插入数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int[] insert(DataDefine dataDefine, List<Map<String, Object>> datas) {
		return insert(dataDefine.getDsId(), dataDefine, datas);
	}

	/**
	 * 	批量插入数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int[] insert(Long dsId, DataDefine dataDefine, List<Map<String, Object>> datas) {
		datas.stream().forEach(row->{
			processInsertDefaultData(dataDefine, row);
		});
		AssertUtil.database().isTrue(false, "暂未实现");
		return null;
	}
	
	
	/**
	 * 新增操作默认数据处理
	 * @param dsId
	 * @param dataDefine
	 * @param data
	 */
	public void processInsertDefaultData(DataDefine dataDefine,Map<String, Object> data) {
		log.debug("新增操作默认数据处理,dsId:{},data model id:{}",dataDefine.getDsId(),dataDefine.getId());
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		if (idField != null && data.get(idField.getAlias())==null) {
			data.put(idField.getAlias(), IdGenHolder.generate());
		}
		DataFieldDto tenantIdField = dataDefine.getStsField(BaseField.TENANT_ID);
		if (tenantIdField != null) {
			data.put(tenantIdField.getAlias(),sessionService.getTenantId());
		}
		DataFieldDto orgIdField=dataDefine.getStsField(BaseField.ORGAN_ID);
		if (orgIdField!=null){
			data.put(orgIdField.getAlias(), sessionService.getOrgId());
		}
		DataFieldDto orgCodeField=dataDefine.getStsField(BaseField.ORGAN_CODE);
		if (orgCodeField!=null){
			data.put(orgCodeField.getAlias(), sessionService.getOrgLvsn());
		}
		DataFieldDto areaCodeField=dataDefine.getStsField(BaseField.AREA_CODE);
		if (areaCodeField!=null){
			data.put(areaCodeField.getAlias(), sessionService.getAreaCode());
		}
		DataFieldDto userIdField=dataDefine.getStsField(BaseField.USER_ID);
		if (userIdField!=null){
			data.put(userIdField.getAlias(), sessionService.getUserId());
		}
		DataFieldDto delFlagField=dataDefine.getStsField(BaseField.DEL_FLAG);
		if (delFlagField!=null){
			data.put(delFlagField.getAlias(), 0);
		}
		DataFieldDto createdField=dataDefine.getStsField(BaseField.CREATED);
		if (createdField!=null){
			if("Date".equalsIgnoreCase(createdField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(createdField.getDataType())) {
				data.put(createdField.getAlias(), DateUtil.date());
			}else {
				data.put(createdField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataFieldDto createdByField=dataDefine.getStsField(BaseField.CREATED_BY);
		if(createdByField!=null){
			data.put(createdByField.getAlias(), sessionService.getUserId());
		}
		DataFieldDto lastUpdatedField=dataDefine.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdatedField!=null){
			if("Date".equalsIgnoreCase(lastUpdatedField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(lastUpdatedField.getDataType())) {
				data.put(lastUpdatedField.getAlias(), DateUtil.date());
			}else {
				data.put(lastUpdatedField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataFieldDto lastUpdatedByField=dataDefine.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedByField!=null){
			data.put(lastUpdatedByField.getAlias(), sessionService.getUserId());
		}
	}

	
	/**
	 * 	更新数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int update(DataDefine dataDefine, Map<String, Object> data, Map<String, Object> params,String... fields) {
		return update(dataDefine.getDsId(), dataDefine, data,params,fields);
	}

	/**
	 * 	更新数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int update(Long dsId, DataDefine dataDefine, Map<String, Object> data, Map<String, Object> params,String... fields) {
		Map<String, Boolean> fieldMap = new HashMap<>();
		processUpdateDeleteDefaultData(dataDefine,data);
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
		return storageBaseService.update(dsId, dataDefine.getSqlUpdate(), paramObj);
	}
	
	
	/**
	 * 	更新数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int updateById(DataDefine dataDefine,DataCommit commit,String ...fields) {
		return updateById(dataDefine.getDsId(),dataDefine, commit,fields);
	}
	
	
	/**
	 * 	更新数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int updateById(Long dsId,DataDefine dataDefine,DataCommit commit,String ...fields) {
		DataFieldDto sidField=dataDefine.getStsField(BaseField.ID);
		AssertUtil.service().notNull(commit.getId(), "主键不能为空").notNull(sidField, "未配置主键字段");
		Map<String, Object> params=new HashMap<>();
		Map<String, Object> fieldMap=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", commit.getData());
		paramObj.put("params", params);
		paramObj.put("fields", fieldMap);
		params.put(sidField.getName(), commit.getId());
		
		processUpdateDeleteDefaultData(dataDefine,commit.getData());
		
		if(fields.length>0) {
			for(String field:fields) {
				fieldMap.put(field, true);
			}
		}else {
			for(String field:commit.getData().keySet()) {
				fieldMap.put(field, true);
			}
		}
		
		return storageBaseService.update(dsId, dataDefine.getSqlUpdate(), paramObj);
	}
	
	
	/**
	 * 	更新/删除操作默认数据处理
	 * @param dsId
	 * @param dataDefine
	 * @param data
	 */
	public void processUpdateDeleteDefaultData(DataDefine dataDefine,Map<String, Object> data) {
		log.debug("新增操作默认数据处理,dsId:{},data model id:{}",dataDefine.getDsId(),dataDefine.getId());
		DataFieldDto lastUpdatedField=dataDefine.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdatedField!=null){
			if("Date".equalsIgnoreCase(lastUpdatedField.getDataType()) || 
					"Timestamp".equalsIgnoreCase(lastUpdatedField.getDataType())) {
				data.put(lastUpdatedField.getAlias(), DateUtil.date());
			}else {
				data.put(lastUpdatedField.getAlias(), DateUtil.date().getTime());
			}
		}
		DataFieldDto lastUpdatedByField=dataDefine.getStsField(BaseField.LAST_UPDATED_BY);
		if(lastUpdatedByField!=null){
			data.put(lastUpdatedByField.getAlias(), sessionService.getUserId());
		}
	}

	
	/**
	 * 	删除数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int delete(DataDefine dataDefine, Map<String, Object> params) {
		return delete(dataDefine.getDsId(), dataDefine, params);
	}
	
	
	/**
	 * 	删除数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int delete(Long dsId,DataDefine dataDefine, Map<String, Object> params) {
		Map<String, Object> data=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", data);
		paramObj.put("params", params);
		
		processUpdateDeleteDefaultData(dataDefine,data);
		
		return storageBaseService.delete(dsId, dataDefine.getSqlDelete(), params);
	}
	
	
	/**
	 * 	删除数据:物理/逻辑删除
	 * @param dataDefine
	 * @param ids
	 * @return
	 */
	public int deleteByIds(DataDefine dataDefine,Set<Long> ids) {
		return deleteByIds(dataDefine.getDsId(),dataDefine, ids);
	}
	
	
	/**
	 * 	删除数据:物理/逻辑删除
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public int deleteByIds(Long dsId,DataDefine dataDefine,Set<Long> ids) {
		DataFieldDto delFlagField=dataDefine.getStsField(BaseField.DEL_FLAG);
		DataFieldDto idField=dataDefine.getStsField(BaseField.ID);
		AssertUtil.service().notEmpty(ids, "主键集合不能为空");
		Map<String, Object> data=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		Map<String, Object> fieldMap=new HashMap<>();
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("data", data);
		paramObj.put("params", params);
		paramObj.put("fields", fieldMap);
		
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"), ids);
		data.put(delFlagField.getName(), 1);
		for(String field:data.keySet()) {
			fieldMap.put(field, true);
		}
		
		processUpdateDeleteDefaultData(dataDefine,data);
		
		return storageBaseService.delete(dsId, delFlagField!=null?dataDefine.getSqlUpdate():dataDefine.getSqlDelete(), paramObj);
	}
	
	

	/**
	 * 	查询一条数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public Map<String, Object> findOne(DataDefine dataDefine, Map<String, Object> params) {
		return findOne(dataDefine.getDsId(),dataDefine, params);
	}
	

	/**
	 * 	查询一条数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public Map<String, Object> findOne(Long dsId,DataDefine dataDefine, Map<String, Object> params) {
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findOne(dsId, sql, paramObj,dataDefine.getFields());
	}
	
	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param load
	 * @return
	 */
	public Map<String, Object> findById(DataDefine dataDefine,DataLoad load) {
		return findById(dataDefine.getDsId(),dataDefine, load);
	}
	
	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param load
	 * @return
	 */
	public Map<String, Object> findById(Long dsId,DataDefine dataDefine,DataLoad load) {
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(idField!=null?idField.getAlias():"id", load.getId());
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findOne(dsId, sql, paramObj,dataDefine.getFields());
	}
	
	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param load
	 * @return
	 */
	public List<Map<String, Object>> findByIds(DataDefine dataDefine,DataLoad load) {
		return findByIds(dataDefine.getDsId(),dataDefine, load);
	}
	
	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param load
	 * @return
	 */
	public List<Map<String, Object>> findByIds(Long dsId,DataDefine dataDefine,DataLoad load) {
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"),load.getIds());
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findList(dsId, sql, paramObj,dataDefine.getFields());
	}
	
	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param id
	 * @return
	 */
	public Map<String, Object> findById(DataDefine dataDefine,Long id) {
		return findById(dataDefine.getDsId(),dataDefine, id);
	}
	

	/**
	 * 	根据主键查询数据
	 * @param dataDefine
	 * @param id
	 * @return
	 */
	public Map<String, Object> findById(Long dsId,DataDefine dataDefine,Long id) {
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(idField!=null?idField.getAlias():"id", id);
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findOne(dsId, sql, paramObj,dataDefine.getFields());
	}
	
	
	/**
	 * 	根据主键集合查询数据
	 * 
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findByIds(DataDefine dataDefine,Set<Long> ids) {
		return findByIds(dataDefine.getDsId(),dataDefine, ids);
	}
	

	/**
	 * 	根据主键集合查询数据
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findByIds(Long dsId,DataDefine dataDefine,Set<Long> ids) {
		AssertUtil.service().notEmpty(ids, "主键不能为空");
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"), ids);
		
		paramObj.put("params", params);
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findList(dsId, sql, paramObj,dataDefine.getFields());
	}
	

	/**
	 * 	查询数据列表，未分页
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findList(DataDefine dataDefine, Map<String, Object> params) {
		return findList(dataDefine.getDsId(), dataDefine, params);
	}

	
	/**
	 * 	查询数据列表，未分页
	 * @param dataDefine
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> findList(Long dsId, DataDefine dataDefine, Map<String, Object> params) {
		String sql=dataDefine.getSqlFind().replace("#{page('*')}", "*");
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findList(dsId, sql, paramObj,dataDefine.getFields());
	}

	/**
	 * 	查询数据列表，分页
	 * @param dataDefine
	 * @param params
	 * @param page
	 * @param size
	 * @return
	 */
	public DataResult<List<Map<String, Object>>> findListPage(DataDefine dataDefine, Map<String, Object> params,
			int page, int size) {
		return findListPage(dataDefine.getDsId(), dataDefine, params, page, size);
	}

	/**
	 * 	查询数据列表，分页
	 * @param dataDefine
	 * @param params
	 * @param page
	 * @param size
	 * @return
	 */
	public DataResult<List<Map<String, Object>>> findListPage(Long dsId,DataDefine dataDefine,
			Map<String, Object> params, int page, int size) {
		Map<String, Object> paramObj=new HashMap<>();
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		return storageBaseService.findListPage(dsId, dataDefine.getSqlFind(), paramObj, page, size,dataDefine.getFields());
	}
	
	
	/**
	 * 	查询数据列表，分页
	 * @param dataDefine
	 * @param find
	 * @return
	 */
	public DataResult<List<Map<String, Object>>> findListPage(DataDefine dataDefine,DataFind find) {
		return findListPage(dataDefine.getDsId(),dataDefine, find);
	}

	/**
	 * 	查询数据列表，分页
	 * @param dsId
	 * @param dataDefine
	 * @param find
	 * @return
	 */
	public DataResult<List<Map<String, Object>>> findListPage(Long dsId,DataDefine dataDefine,DataFind find) {
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
		
		processFindDefaultParams(dataDefine, find.getBody());
		
		return storageBaseService.findListPage(dsId, dataDefine.getSqlFind(), paramObj, find.getPage(),find.getPageSize(), dataDefine.getFields());
	}
	
	
	/**
	 * 	查询操作默认参数
	 * @param dataDefine
	 * @param data
	 */
	public void processFindDefaultParams(DataDefine dataDefine,Map<String, Object> data) {
		log.debug("查询操作默认参数处理,data model id:{}",dataDefine.getId());
		
		
		
	}
	
	/**
	 * 	根据外键字段从外键数据表中加载数据
	 * @param dataDefine
	 * @param fkey
	 * @param vlaues
	 * @return
	 */
	public Map<String, Map<String, Object>> loadFkeyEntrys(DataDefine dataDefine,DataFieldDto fkey,Set<Object> vlaues){
		return loadFkeyEntrys(dataDefine.getDsId(), fkey, vlaues);
	}
	
	
	/**
	 * 	根据外键字段从外键数据表中加载数据
	 * @param dsId
	 * @param fkey
	 * @param vlaues
	 * @return
	 */
	public Map<String, Map<String, Object>> loadFkeyEntrys(Long dsId,DataFieldDto fkey,Set<Object> vlaues){
		Map<String, Map<String, Object>> map=new HashMap<>();
		String sql = this.buildFkeyQuerySql(fkey);
		if(StringUtils.isEmpty(sql)) {
			return map;
		}
		
		
		DataDefine dataDefine = dataDefineCache.load(fkey.getFkey().getDsn());
		AssertUtil.service().notNull(dataDefine, "外键关联数据定义未找到");
		DataFieldDto idField = dataDefine.getStsField(BaseField.ID);
		
		Map<String, Object> paramObj=new HashMap<>();
		Map<String, Object> params=new HashMap<>();
		params.put(String.format("%ss", idField!=null?idField.getAlias():"id"), vlaues);
		paramObj.put("params", params);
		
		processFindDefaultParams(dataDefine, params);
		
		List<Map<String, Object>> list = storageBaseService.findList(dsId, sql, paramObj,dataDefine.getFields());
		
		list.stream().forEach(row->{
			Object fkeyValue=row.get(fkey.getFkey().getFieldName());
			if(fkeyValue!=null) {
				map.put(fkeyValue.toString(), row);
			}
		});
		
		return map;
	}
	
	
	/**
	 * 	加载外键数据集合
	 * @param dataDefine
	 * @param rows
	 * @param fkeys
	 */
	public void loadFkeyEntrys(DataDefine dataDefine,List<Map<String, Object>> rows,String... fkeys) {
		loadFkeyEntrys(dataDefine.getDsId(), dataDefine, rows, fkeys);
	}
	
	/**
	 * 	加载外键数据集合
	 * @param dataDefine	数据模型
	 * @param rows		数据记录集合
	 * @param fkeys		外键字段，为空则是所有
	 */
	public void loadFkeyEntrys(Long dsId,DataDefine dataDefine,List<Map<String, Object>> rows,String... fkeys) {
		log.debug("进入：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataDefine.getId(),fkeys,rows.size());
		List<String> fkeyList=new ArrayList<>();
		Map<String, DataFieldDto> fkeyFieldMap=new HashMap<>();
		for(String field:fkeys) {
			fkeyList.add(field);
		}
		dataDefine.getFields().stream().filter(field->{
			if(field.getFkey()!=null && field.getFkey().isEnable()) {
				return true;
			}		
			return false;
		}).filter(fkey->!StringUtils.isEmpty(fkey.getName()))
		.filter(fkey->(fkeyList.isEmpty()||fkeyList.contains(fkey.getName()))).forEach(fkey->{
			fkeyFieldMap.put(fkey.getName(), fkey);
		});
		if(fkeyFieldMap.isEmpty()) {
			// 外键字段为空
			log.debug("进入：加载外键数据集合方法,data model id:{},外键字段为空",dataDefine.getId());
			return;
		}
		log.debug("开始：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataDefine.getId(),fkeyFieldMap.keySet(),rows.size());
		
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
				DataFieldDto fkeyField=fkeyFieldMap.get(entry.getKey());
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
						DataFieldDto fkeyField=fkeyFieldMap.get(fkeyEntry.getKey());
						if(fkeyField!=null && fkeyValue!=null) {
							Map<String, Map<String, Object>> fkeyValueObj=fkeyEntry.getValue();
							if(fkeyValueObj!=null) {
								 Map<String, Object> fkeyvs=fkeyValueObj.get(fkeyValue.toString());
								 if(fkeyvs!=null) {
									 Object fkeyLable=fkeyvs.get(fkeyField.getFkey().getLabelName());
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
		
		log.debug("退出：加载外键数据集合方法,data model id:{},fkeys:{},row count:{}",dataDefine.getId(),fkeyFieldMap.keySet(),rows.size());
	}
	
	
	
	/**
	 * 	构建外键搜索sql
	 * @param dataDefineDto
	 * @param fkeyName
	 * @return
	 */
	private String buildFkeyQuerySql(DataFieldDto fkeyField) {
		
		ForeignKeyDto fkey = fkeyField.getFkey();
		if(fkey!=null && fkey.isEnable() && !StringUtils.isEmpty(fkey.getFieldName()) && 
				!StringUtils.isEmpty(fkey.getLabelName()) && !StringUtils.isEmpty(fkey.getDsn())) {
			DataDefine dataDefine=dataDefineCache.load(fkey.getDsn());
			
			StringBuffer sql=new StringBuffer();
			StringBuffer field=new StringBuffer();
			StringBuffer where=new StringBuffer();
			
			field.append(fkey.getFieldName()).append(",").append(fkey.getLabelName());
			if(fkey.getFields()!=null) {
				fkey.getFields().stream().forEach(fk->{
					field.append(",").append(fk.getName());
				});
			}
			
			where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(fkey.getFieldName()).append(")){")
				 .append(System.lineSeparator()).append(" AND ").append(fkey.getFieldName()).append(" = #{params.").append(fkey.getFieldName()).append("}")
				 .append(System.lineSeparator()).append("-- @}");
			
			where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.ids)){")
			 	 .append(System.lineSeparator()).append(" AND ").append(fkey.getFieldName()).append(" IN (#{join(params.ids)})")
			 	 .append(System.lineSeparator()).append("-- @}");
			
			where.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.keywords)){")
			 	 .append(System.lineSeparator()).append(" AND ").append(fkey.getLabelName()).append(" LIKE #{'%'+params.keywords+'%'}")
			 	 .append(System.lineSeparator()).append("-- @}");
			
			sql.append("SELECT ").append(field).append(" FROM ").append(dataDefine.getName()).append(" WHERE 1=1 ").append(where);
			return sql.toString();
		}
		return null;
	}
	

}
