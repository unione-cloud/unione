package com.unione.cloud.form.data.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.cache.DataDefineCache;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataField;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.data.storage.model.DataDefine.DataDefineCategory;
import com.unione.cloud.form.data.storage.model.DataDefine.DataDefineConfig;
import com.unione.cloud.form.data.storage.model.DataDefine.DataField;
import com.unione.cloud.form.data.storage.model.DataDefine.DataFilter;
import com.unione.cloud.form.data.storage.model.DataDefine.DataQuery;
import com.unione.cloud.form.data.storage.model.DataDefine.DataQueryType;
import com.unione.cloud.form.data.storage.model.DataDefine.ForeignKey;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 	数据模型服务
 * @author Unione Cloud Jeking
 */
@Slf4j
@Service
public class DataDefineService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DataDefineCache dataDefineCache;
	
	@Value("${unione.form.page.default.appid:1000}")
	private Long DEFAULT_APP_ID;
	
	
	
	/**
	 * 保存数据定义
	 * @param dataDefine
	 * @return
	 */
	@Transactional
	public Results<DataDefine> save(DataDefine dataDefine){
		log.info("进入：保存数据定义方法,id:{},sn:{}",dataDefine.getId(),dataDefine.getSn());
		LogsUtil.set(LogType.Insert, "新增数据定义管理");
		if("new_".equals(dataDefine.getSn())) {
			dataDefine.setSn(null);
		}
		
		// 如果不是自定义类型，则生成sql
		if(!ObjectUtil.equal(dataDefine.getIsCustom(), 1)) {
			this.buildDataSql(dataDefine);
		}
		
		SysDataDefine tmp = null;
		if(!StringUtils.isEmpty(dataDefine.getSn())) {
			SysDataDefine param=SysDataDefine.builder().sn(dataDefine.getSn()).build();
			param.setTenantId(sessionService.getTenantId());
			tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		}
		
		try {
			if(tmp!=null) {
				LogsUtil.set(LogType.Update, "更新数据定义管理");
				dataDefine.setId(tmp.getId());
				// 更新
				String[] fields = {"dirId","dsId","title","name","isCustom","category","sqlFind","sqlInsert","sqlUpdate","sqlDelete","url","syncFlag","fields","settings","ordered","status","descs"};
				SqlBuilder<SysDataDefine> sqlBuilder=SqlBuilder.build((SysDataDefine)dataDefine).field(fields);
				int len = dataBaseDao.updateById(sqlBuilder);
				AssertUtil.service().isTrue(len>0, "数据定义保存失败");
			}else {
				// 新增
				// 参数处理
				if(StringUtils.isEmpty(dataDefine.getSn())) {
					dataDefine.setSn(RandomUtil.randomString(20));
				}
				BeanUtils.setDefaultValue(dataDefine, "appId", DEFAULT_APP_ID);
				BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
				BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
				BeanUtils.setDefaultValue(dataDefine, "status",1);
				BeanUtils.setDefaultValue(dataDefine, "ordered",0);
				BeanUtils.setDefaultValue(dataDefine, "configs","{}");
				dataDefine.setVers(1);
				
				int len = dataBaseDao.insert(dataDefine);
				AssertUtil.service().isTrue(len>0, "数据定义保存失败");
			}
			
			// 同步数据字段
			this.syncDataField(dataDefine);
			
		} catch (Exception e) {
			log.error("保存数据定义失败",e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Results.failure("保存数据定义失败");
		}
		
		log.debug("退出:新增数据定义管理信息.entity:{},result:true",dataDefine);
		return Results.success(dataDefine);
	}
	
	
	/**
	 * 同步保存数据字段信息
	 * @param dataDefine
	 */
	public void syncDataField(DataDefine dataDefine) {
		log.info("进入：同步保存数据字段信息,data define id:{},sn:{},fields:{}",dataDefine.getId(),dataDefine.getSn(),dataDefine.getFields().size());
		
		SqlBuilder<SysDataField> sqlBuilder=SqlBuilder.build(SysDataField.class).params("defineId", dataDefine.getId());
		List<SysDataField> hadFields = dataBaseDao.findList(sqlBuilder);
		Map<Long,SysDataField> hadFieldMap=new HashMap<>();
		hadFields.stream().forEach(field->{
			hadFieldMap.put(field.getId(),field);
		});		
		
		List<DataField> addFields=new ArrayList<>();
		String fieldAttr[]=new String[] {"title","name","dataType","dataFormat","dataLen","dataPrec","isPk","isNull","stsField","configs","needAuth","syncEnable","syncFlag","ordered","status","descs"}; 
		dataDefine.getFields().stream().forEach(field->{
			if(field.getId()==null || hadFieldMap.get(field.getId())==null) {
				field.setAppId(dataDefine.getAppId());
				field.setDefineId(dataDefine.getId());
				addFields.add(field);
				return;
			}
			
			// 更新字段
			SysDataField sdField=hadFieldMap.remove(field.getId());
			BeanUtils.copy(field, sdField, fieldAttr);			
			int len = dataBaseDao.updateById(SqlBuilder.build(sdField).field(fieldAttr));
			AssertUtil.service().isTrue(len>0, "字段更新失败");			
		});
		
		// 新增字段
		if(!addFields.isEmpty()) {
			dataBaseDao.insertBatch(addFields);
		}
		
		// 删除字段
		if(!hadFieldMap.values().isEmpty()) {
			List<Long> ids = hadFieldMap.values().stream()
			.filter(field->ObjectUtil.equal(field.getDelFlag(),0)&&ObjectUtil.equal(field.getStatus(),1))
			.map(field->field.getId())
			.collect(Collectors.toList());
			if(!ids.isEmpty()) {
				int len = dataBaseDao.deleteLogicById(SqlBuilder.build(SysDataField.class).ids(ids));
				AssertUtil.service().isTrue(len>0, "字段删除失败");
			}
		}
		
	}
	
	/**
	 * 生成数据定义增删改查SQL
	 * @param dataDefine
	 */
	public void buildDataSql(DataDefine dataDefine) {
		log.info("进入：生成数据定义增删改查SQL,id:{},sn:{},category:{}",dataDefine.getId(),dataDefine.getSn(),dataDefine.getCategory());
		if(!DataDefineCategory.SQL.value().equals(dataDefine.getCategory())){
			return;
		}
		
		StringBuffer findSql = new StringBuffer();
		StringBuffer keywords = new StringBuffer();
		
		StringBuffer tableFileds = new StringBuffer();
		StringBuffer updateWhereFiels = new StringBuffer();

		StringBuffer whereFields = new StringBuffer();
		List<DataField> whereIgnoreFields=new ArrayList<>();
		StringBuffer updateFields = new StringBuffer();
		StringBuffer deleteSql = new StringBuffer();
		updateFields.append(System.lineSeparator()).append("-- @trim(){");
		StringBuffer tableFiledValue = new StringBuffer();
		
		// 标准查询条件
		DataField idField = dataDefine.getStsField(BaseField.ID);
		if(idField!=null) {
			whereIgnoreFields.add(idField);
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.id)){").append(System.lineSeparator())
				.append(" AND ").append(idField.getName()).append("=#{params.id}").append(System.lineSeparator())
				.append("-- @}");
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.ids)){").append(System.lineSeparator())
				.append(" AND ").append(idField.getName()).append(" IN (#{join(params.ids)})").append(System.lineSeparator())
				.append("-- @}");
			updateWhereFiels.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.id)){").append(System.lineSeparator())
				.append(" AND ").append(idField.getName()).append("=#{params.id}").append(System.lineSeparator())
				.append("-- @}");
			updateWhereFiels.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.ids)){").append(System.lineSeparator())
				.append(" AND ").append(idField.getName()).append(" IN (#{join(params.ids)})").append(System.lineSeparator())
				.append("-- @}");
		}
		
		DataField lastUpdatedField = dataDefine.getStsField(BaseField.LAST_UPDATED);
		if(lastUpdatedField!=null) {
			whereIgnoreFields.add(lastUpdatedField);
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(lastUpdatedField.getAlias()).append("Start)){").append(System.lineSeparator())
				.append(" AND ").append(lastUpdatedField.getName()).append(" > #{params.").append(lastUpdatedField.getAlias()).append("Start}").append(System.lineSeparator())
				.append("-- @}");
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(lastUpdatedField.getAlias()).append("End)){").append(System.lineSeparator())
				.append(" AND ").append(lastUpdatedField.getName()).append(" <= #{params.").append(lastUpdatedField.getAlias()).append("End}").append(System.lineSeparator())
				.append("-- @}");
		}
		DataField createdField = dataDefine.getStsField(BaseField.CREATED);
		if(createdField!=null) {
			whereIgnoreFields.add(createdField);
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(createdField.getAlias()).append("Start)){").append(System.lineSeparator())
				.append(" AND ").append(createdField.getName()).append(" > #{params.").append(createdField.getAlias()).append("Start}").append(System.lineSeparator())
				.append("-- @}");
			whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(createdField.getAlias()).append("End)){").append(System.lineSeparator())
				.append(" AND ").append(createdField.getName()).append(" <= #{params.").append(createdField.getAlias()).append("End}").append(System.lineSeparator())
				.append("-- @}");
		}
		DataField delFlagField = dataDefine.getStsField(BaseField.DEL_FLAG);
		if(delFlagField!=null) {
			deleteSql.append("UPDATE ").append(dataDefine.getName())
				.append(" SET ").append(delFlagField.getName()).append("=1 ");
			DataField lastUpdatedByField = dataDefine.getStsField(BaseField.LAST_UPDATED_BY);
			if(lastUpdatedField!=null) {
				deleteSql.append(",").append(lastUpdatedField.getName()).append("=#{data.").append(lastUpdatedField.getAlias()).append("}");
			}
			if(lastUpdatedByField!=null) {
				deleteSql.append(",").append(lastUpdatedByField.getName()).append("=#{data.").append(lastUpdatedByField.getAlias()).append("}");
			}
		}else {
			deleteSql.append("DELETE FROM ").append(dataDefine.getName());
		}
		
		//过滤条件拼接
		DataDefineConfig dataDefineConfig = dataDefine.getConfigDto();
		if (!dataDefineConfig.getFilters().isEmpty()){
			for (DataFilter filter:dataDefineConfig.getFilters()){
				whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(filter.getName()).append(")){").append(System.lineSeparator())
					.append(filter.getFilter()).append(System.lineSeparator())
					.append("-- @}");
			}
		}

		//迭代字段拼接
		for (DataField field:dataDefine.getFields()){
			// 如果是主键字段
			if (idField!=null && field.getName().equals(idField.getName())){
				whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(" AND ").append(field.getName()).append("=#{params.").append(field.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				updateWhereFiels.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(" AND ").append(field.getName()).append("=#{params.").append(field.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
			}
			
			// 查询字段处理
			DataQuery query=field.getConfigDto().getQuery();
			if (query!=null && query.isEnable()){
				if (DataQueryType.EQ.name().equals(query.getType())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
						.append(" AND ").append(field.getName()).append("=#{params.").append(field.getAlias()).append("}").append(System.lineSeparator())
						.append("-- @}");
				}
				if (DataQueryType.LIKE.name().equals(query.getType())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
						.append(" AND ").append(field.getName()).append(" LIKE #{'%'+params.").append(field.getAlias()).append("+'%'}").append(System.lineSeparator())
						.append("-- @}");
				}
				if (DataQueryType.LLIKE.name().equals(query.getType())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
						.append(" AND ").append(field.getName()).append(" LIKE #{'%'+params.").append(field.getAlias()).append("}").append(System.lineSeparator())
						.append("-- @}");
				}
				if (DataQueryType.RLIKE.name().equals(query.getType())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
						.append(" AND ").append(field.getName()).append(" LIKE #{params.").append(field.getAlias()).append("+'%'}").append(System.lineSeparator())
						.append("-- @}");
				}
				if (query.isDefoult()){
					if(keywords.length()>0) {
						keywords.append(" OR ");
					}
					keywords.append(field.getName()).append(" LIKE #{'%'+params.keywords+'%'}");
				}
			}

			// 更新脚本
			if (idField==null || !field.getName().equals(idField.getName())){
				updateFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(fields.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(field.getName()).append("=#{data.").append(field.getAlias()).append("},").append(System.lineSeparator())
					.append("-- @}");
			}

			// 新增脚本
			tableFileds.append(field.getName()).append(",");
			tableFiledValue.append("#{data.").append(field.getAlias()).append("}").append(",");
		}
		updateFields.append(System.lineSeparator()).append("-- @}");

		
		//外键拼接 BEGIN
		dataDefine.getFields()
		.stream()
		.filter(f -> f.getConfigDto().getFkey()!=null && f.getConfigDto().getFkey().isEnable() && !ObjectUtil.isEmpty(f.getConfigDto().getFkey().getDsn()))
		.forEach(fkeyField -> {
			ForeignKey fkey=fkeyField.getConfigDto().getFkey();
			DataDefine fkeyDataDefine=dataDefineCache.load(fkey.getDsn());
			
			List<String> fkFieldNames=new ArrayList<>();
			if(fkey.getFields()!=null) {
				fkey.getFields().stream().forEach(field->{
					if(!StringUtils.isEmpty(field.getName())) {
						fkFieldNames.add(field.getName());
					}
				});
			}
			if(!StringUtils.isEmpty(fkey.getLabelName())) {
				fkFieldNames.add(fkey.getLabelName());
			}
			if(fkFieldNames.isEmpty()) {
				// 没有外键字段信息，不处理外键关联查询
				return;
			}
				
			StringBuffer tmpStr = new StringBuffer();
			StringBuffer fkWhereFields = new StringBuffer();
			tmpStr.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params) && (");
		
			// 外键关联字段查询处理
			fkeyDataDefine.getFields().stream()
			.filter(f->fkFieldNames.contains(f.getName()))
			.filter(f->f.getConfigDto().getQuery()!=null)
			.filter(f->f.getConfigDto().getQuery().isEnable())
			.forEach(fkfield->{
				tmpStr.append("isNotEmpty(params.")
				.append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
				.append(") || ");

				if (DataQueryType.EQ.name().equals(fkfield.getConfigDto().getQuery().getType())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append("=#{params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LIKE.name().equals(fkfield.getConfigDto().getQuery().getType())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append(" LIKE #{'%'+params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("+'%'}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LLIKE.name().equals(fkfield.getConfigDto().getQuery().getType())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append(" LIKE #{'%'+params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.RLIKE.name().equals(fkfield.getConfigDto().getQuery().getType())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append(" LIKE #{params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("+'%'}").append(System.lineSeparator())
					.append("-- @}");
				}
			});
		
			whereFields.append(tmpStr.substring(0,tmpStr.length()-3));
			whereFields.append(")){")
			.append(System.lineSeparator())
			.append(" AND EXISTS( SELECT 1 FROM ")
			.append(fkeyDataDefine.getName())
			.append(" AS fkt WHERE ").append(fkeyField.getName()).append(" = ").append("fkt.SID")
			.append(fkWhereFields).append(System.lineSeparator())
			.append(")")
			.append(System.lineSeparator())
			.append("-- @}");
		});
		//外键拼接 END
		
		
		// 查询SQL组装
		findSql.append("select #{page('*')} from ").append(dataDefine.getName());
		findSql.append(" where 1=1 ").append(System.lineSeparator())
				.append(whereFields).append(System.lineSeparator());
		if(keywords.length()>0) {
			// 关键字搜索
			findSql.append("-- @if(isNotEmpty(params.keywords) && !isBlank(params.keywords)){").append(System.lineSeparator())
				.append(" AND (").append(keywords).append(")").append(System.lineSeparator())
				.append("-- @}").append(System.lineSeparator());
		}
		findSql.append(" #{globalUse(\"base.orderby\")}");
		dataDefine.setFindScript(findSql.toString());
		// 查询SQL组装END
		
		// 新增SQL组装
		StringBuffer insertSql = new StringBuffer();
		insertSql.append("insert into ").append(dataDefine.getName()).append("(")
				.append(tableFileds.substring(0,tableFileds.length()-1)).append(")").append(" values (")
				.append(tableFiledValue.substring(0,tableFiledValue.length()-1)).append(")");
		dataDefine.setInsertScript(insertSql.toString());
		// 新增SQL组装 END
		
		// 修改SQL组装
		StringBuffer updateSql = new StringBuffer();
		updateSql.append("update ").append(dataDefine.getName()).append(" set ").append(updateFields)
				.append(System.lineSeparator())
				.append(" where  1=1 ").append(updateWhereFiels);
		dataDefine.setUpdateScript(updateSql.toString());
		// 修改SQL组装 END
		
		// 删除SQL组装
		deleteSql.append(" where  1=1 ").append(updateWhereFiels);
		dataDefine.setDeleteScript(deleteSql.toString());
		// 删除SQL组装 END
		
	}
	
	

}
