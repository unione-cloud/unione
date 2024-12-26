package com.unione.cloud.form.data.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.ColDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.JavaType;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.meta.MetadataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.cache.DataDefineCache;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataDefineHis;
import com.unione.cloud.form.data.model.SysDataDefineRelease;
import com.unione.cloud.form.data.model.SysDataField;
import com.unione.cloud.form.data.model.SysDataFieldHis;
import com.unione.cloud.form.data.model.SysDataFieldRelease;
import com.unione.cloud.form.data.storage.StorageBaseService;
import com.unione.cloud.form.data.storage.model.DataDefine;
import com.unione.cloud.form.data.storage.model.DataDefine.DataDefineCategory;
import com.unione.cloud.form.data.storage.model.DataDefine.DataDefineConfig;
import com.unione.cloud.form.data.storage.model.DataDefine.DataField;
import com.unione.cloud.form.data.storage.model.DataDefine.DataFieldConfig;
import com.unione.cloud.form.data.storage.model.DataDefine.DataFilter;
import com.unione.cloud.form.data.storage.model.DataDefine.DataQuery;
import com.unione.cloud.form.data.storage.model.DataDefine.DataQueryType;
import com.unione.cloud.form.data.storage.model.DataDefine.FieldForm;
import com.unione.cloud.form.data.storage.model.DataDefine.FieldList;
import com.unione.cloud.form.data.storage.model.DataDefine.FieldWidget;
import com.unione.cloud.form.data.storage.model.DataDefine.ForeignKey;
import com.unione.cloud.form.page.dto.PageDefine.FormItem;
import com.unione.cloud.form.page.dto.PageDefine.FormPageDefine;
import com.unione.cloud.form.page.dto.PageDefine.ListPageDefine;
import com.unione.cloud.form.page.dto.PageDefine.PageType;
import com.unione.cloud.form.page.dto.PageDefine.QueryField;
import com.unione.cloud.form.page.dto.PageDefine.TableColumn;
import com.unione.cloud.form.page.model.SysPageDefine;
import com.unione.cloud.form.page.service.PageDefineService;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
	
	@Autowired
	private StorageBaseService storageBaseService;
	
	@Autowired
	private SecretService secretService;
	
	@Autowired
	private PageDefineService pageDefineService;
	
	@Value("${unione.form.page.default.appid:1000}")
	private Long DEFAULT_APP_ID;
	
	
	
	/**
	 * 发布数据定义
	 * @param ids
	 * @return
	 */
	@Transactional
	public Results<String> release(Set<Long> ids){
		log.info("进入：预发布数据定义方法,ids:{}",ids);		
		AssertUtil.service().notEmpty(ids, "参数数据定义id集合不能为空");		
		LogsUtil.add("ids:%s",JSONUtil.toJsonStr(ids));
		LogsUtil.setExtData(JSONUtil.toJsonStr(ids));
		LogsUtil.add("加载数据定义列表");
		List<SysDataDefine> defines=dataBaseDao.findByIds(SqlBuilder.build(SysDataDefine.class).ids(ids));		
		
		LogsUtil.add("验证数据定义加载结果");
		defines.stream().forEach(d->ids.remove(d.getId()));
		AssertUtil.service().isTrue(ids.isEmpty(), "参数数据定义对象未找到,ids:"+JSONUtil.toJsonStr(ids));	
		
		LogsUtil.add("验证数据定义是否被删除");
		List<String> delNames = defines.stream()
			.filter(d->Objects.equals(1, d.getDelFlag()))
			.map(d->d.getTitle())
			.collect(Collectors.toList());
		AssertUtil.service().isTrue(delNames.isEmpty(), "数据定义已删除"+JSONUtil.toJsonStr(delNames));
		
		LogsUtil.add("加载数据定义发布列表");
		Map<Long,SysDataDefineRelease> definesRelease=dataBaseDao.findList(SqlBuilder.build(SysDataDefineRelease.class).ids(ids))
				.stream().collect(Collectors.toMap(SysDataDefineRelease::getId, (v1)->v1));
		
		LogsUtil.add("迭代数据定义列表，处理发布逻辑");
		StringBuffer success=new StringBuffer();
		StringBuffer unchange=new StringBuffer();
		
		try {
			defines.stream().forEach(define->{
				SysDataDefineRelease release=definesRelease.get(define.getId());
				if(release==null || !Objects.equals(release.getSignature(),define.getSignature())){
					// 未发布、签名不一致（有更新）
					doRelease(define,release);
					success.append(define.getTitle()).append(",");
				}else {
					// 数据定义未更新，不需要重新发布
					unchange.append(define.getTitle()).append(",");
				}
				
			});
		} catch (Exception e) {
			log.error("发布数据定义失败",e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return	Results.failure();
		};
		
		StringBuffer info=new StringBuffer();
		info.append("成功发布:").append(success.length()>0?success.substring(0,success.length()-1):"0");
		if(unchange.length()>0) {
			info.append(",无变化：").append(unchange.substring(0,unchange.length()-1));
		}
		LogsUtil.add("result:%s",info.toString());
		
		return Results.success(info.toString());
	}
	
	
	/**
	 * 发布数据定义
	 * @param define
	 * @param relase
	 */
	private void doRelease(SysDataDefine define,SysDataDefineRelease relase) {
		log.info("进入：发布数据定义方法,define id:{},release id:{}",define.getId(),relase!=null?relase.getId():"null");		
		LogsUtil.add("进入：发布数据定义方法,define id:%s,release id:%s",define.getId(),relase!=null?relase.getId():"null");
		LogsUtil.add("data define id:%s",define.getId());
		
		define.setPublishDate(DateUtil.date());
		define.setStatus(2);
		
		LogsUtil.add("加载数据字段列表");
		List<SysDataField> fields=dataBaseDao.findList(SqlBuilder.build(SysDataField.class)
				.params("defineId", define.getId()));	
		
		// 发布记录
		if(relase!=null) {
			LogsUtil.add("新版本发布，curent ver:%s",define.getVers());
			define.setVers(define.getVers()+1);
			dataBaseDao.delete(SqlBuilder.build(SysDataDefineRelease.class).id(define.getId()));
			dataBaseDao.delete(SqlBuilder.build(SysDataFieldRelease.class).params("defineId",define.getId()));
		}else {
			define.setVers(1);
			LogsUtil.add("首次发布，curent ver:%s",define.getVers());
		}
		relase=new SysDataDefineRelease();
		BeanUtils.copy(define, relase);
		int len = dataBaseDao.insertWithId(relase);
		AssertUtil.service().isTrue(len>0, "数据定义发布失败");
		
		List<SysDataFieldRelease> fieldReleaseList= fields.stream().map(field->{
			SysDataFieldRelease fieldRelease=new SysDataFieldRelease();
			BeanUtils.copy(field, fieldRelease);
			return fieldRelease;
		}).collect(Collectors.toList());
		dataBaseDao.insertBatchWithId(fieldReleaseList);		
		LogsUtil.add("保存字段发布，field count:%s",fieldReleaseList.size());
		
		// 发布历史
		SysDataDefineHis defineHis=new SysDataDefineHis();
		BeanUtils.copy(define, defineHis);
		defineHis.setDefineId(define.getId());
		dataBaseDao.insert(defineHis);
		LogsUtil.add("保存数据定义历史，did:%s,hid:%s",defineHis.getDefineId(),defineHis.getId());		
		
		List<SysDataFieldHis> fieldHisList= fields.stream().map(field->{
			SysDataFieldHis fieldHis=new SysDataFieldHis();
			BeanUtils.copy(field, fieldHis);
			fieldHis.setDefineHisId(defineHis.getId());
			return fieldHis;
		}).collect(Collectors.toList());
		dataBaseDao.insertBatch(fieldHisList);		
		LogsUtil.add("保存数据字段历史，field count:%s",fieldHisList.size());
		
		len = dataBaseDao.updateById(SqlBuilder.build(define).field("vers","publishDate","status"));
		AssertUtil.service().isTrue(len>0, "数据定义发布失败");
		
		
		releasePageInfo(define);
		
		
	}
	
	
	
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
				
				// 同步数据字段
				this.syncDataField(dataDefine);
				
				// 签名
				this.doSignature(dataDefine);
				
				// 更新数据定义
				String[] fields = {"dirId","dsId","title","name","isCustom","category","sqlFind","sqlInsert","sqlUpdate","sqlDelete","url","syncFlag","fields","configs","signature","ordered","status","descs"};
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
				dataDefine.setVers(1);
				dataDefine.setId(IdGenHolder.generate());
				
				// 同步数据字段
				this.syncDataField(dataDefine);
				
				// 签名
				this.doSignature(dataDefine);
				
				// 保存数据定义
				int len = dataBaseDao.insertWithId(dataDefine);
				AssertUtil.service().isTrue(len>0, "数据定义保存失败");
			}
			
			// 生成页面信息
			buildPageInfo(dataDefine);
			
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
		
		AtomicInteger fieldCount=new AtomicInteger();
		List<DataField> addFields=new ArrayList<>();
		String fieldAttr[]=new String[] {"title","name","dataType","dataFormat","dataLen","dataPrec","isPk","isNull","stsField","configs","needAuth","syncEnable","syncFlag","ordered","status","descs"}; 
		dataDefine.getFields().stream().forEach(field->{
			fieldCount.addAndGet(1);
			field.setOrdered(fieldCount.get());
			BeanUtils.setDefaultValue(field, "syncEnable",0);
			BeanUtils.setDefaultValue(field, "syncFlag",0);
			BeanUtils.setDefaultValue(field, "needAuth",0);
			
			if(field.getId()==null || hadFieldMap.get(field.getId())==null) {
				field.setAppId(dataDefine.getAppId());
				field.setDefineId(dataDefine.getId());
				field.setDelFlag(0);
				field.setStatus(1);				
				addFields.add(field);
				return;
			}
			
			// 更新字段
			SysDataField sdField=hadFieldMap.remove(field.getId());
			BeanUtils.copy(field, sdField, fieldAttr);
			sdField.setConfigs(field.getConfigs());
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
	 * 数据定义：签名
	 * @param define
	 * @return
	 */
	public String doSignature(SysDataDefine define) {
		StringBuffer tmp=new StringBuffer();
		tmp.append(define.getFindScript())
		   .append(define.getInsertScript())
		   .append(define.getUpdateScript())
		   .append(define.getDeleteScript())
		   .append(define.getConfigs());
		
		String signature=secretService.hash(tmp.toString());
		define.setSignature(signature);
		return signature;
	} 
	
	
	/**
	 * 生成页面信息：增删改查
	 * @param define
	 */
	public void buildPageInfo(DataDefine define) {
		log.debug("进入：生成页面信息：增删改查,data define id:{}",define.getId());
		LogsUtil.add("进入：生成页面信息：增删改查,data define id:%s",define.getId());
		
		SysPageDefine listPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:list", define.getSn())));		
		SysPageDefine formPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:form", define.getSn())));
		SysPageDefine viewPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:view", define.getSn())));
		
		LogsUtil.add("列表页面初始化,psn:%s",String.format("%s:list", define.getSn()));
		ListPageDefine listDefine=new ListPageDefine();
		if(listPage!=null) {
			listDefine.setId(listPage.getId());
		}else {
			listDefine.setSn(String.format("%s:list", define.getSn()));
			listDefine.setComponent("unione-page-list");
			listDefine.setAppId(define.getAppId());
			listDefine.setTypes(PageType.SETTING.value());
			listDefine.setIsTmpl(0);
			listDefine.setIsGlobal(0);
			listDefine.setStatus(1);		
		}
		listDefine.setVers(define.getVers());
		listDefine.setTitle(String.format("%s列表", define.getTitle()));
		listDefine.setConfigs("{}");
		listDefine.getConfigDto().getQueryForm().setDsn(define.getSn());
		listDefine.getConfigDto().getTableList().setDsn(define.getSn());
		
		
		LogsUtil.add("表单页面初始化,psn:%s",String.format("%s:form", define.getSn()));
		FormPageDefine formDefine=new FormPageDefine();
		if(formPage!=null) {
			formDefine.setId(formPage.getId());
		}else {
			formDefine.setSn(String.format("%s:form", define.getSn()));
			formDefine.setComponent("unione-page-form");
			formDefine.setAppId(define.getAppId());
			formDefine.setTypes(PageType.SETTING.value());
			formDefine.setIsTmpl(0);
			formDefine.setIsGlobal(0);
			formDefine.setStatus(1);
		}
		formDefine.setVers(define.getVers());
		formDefine.setTitle(String.format("%s表单", define.getTitle()));
		formDefine.setConfigs("{}");
		formDefine.getConfigDto().getForm().setDsn(define.getSn());
		
		LogsUtil.add("详情页面初始化,psn:%s",String.format("%s:view", define.getSn()));
		FormPageDefine viewDefine=new FormPageDefine();
		if(viewPage!=null) {
			viewDefine.setId(viewPage.getId());
		}else {
			viewDefine.setSn(String.format("%s:view", define.getSn()));
			viewDefine.setComponent("unione-page-view");
			viewDefine.setAppId(define.getAppId());
			viewDefine.setTypes(PageType.SETTING.value());
			viewDefine.setIsTmpl(0);
			viewDefine.setIsGlobal(0);
			viewDefine.setStatus(1);
		}
		viewDefine.setVers(define.getVers());
		viewDefine.setTitle(String.format("%s详情", define.getTitle()));
		viewDefine.setConfigs("{}");
		viewDefine.getConfigDto().getForm().setDsn(define.getSn());
		
		LogsUtil.add("迭代字段列表，生成：列表页面，表单页面，详情页面");
		define.getConfigDto().getFields().stream().forEach(field->{
			DataFieldConfig config=field.getConfigDto();
			if(config!=null) {
				// 查询字段
				if(config.getQuery()!=null && config.getQuery().isEnable()) {
					QueryField queryField=new QueryField();
					BeanUtils.copy(field, queryField);
					BeanUtils.copy(field.getConfigDto(), queryField);
					queryField.setVisible(config.getQuery().isVisible());
					queryField.setDefoult(config.getQuery().isDefoult());
					queryField.setWidget(config.getWidget());
					queryField.setName(field.getAlias());
					if(!StringUtils.isEmpty(config.getQuery().getName())) {
						queryField.setName(config.getQuery().getName());
					}
					listDefine.getConfigDto().getQueryForm().getFields().add(queryField);
				}
				// 列表字段
				if(config.getShow()!=null && config.getShow().getList()!=null) {
					FieldList listField=config.getShow().getList();
					if(listField.isEnable()) {
						TableColumn column=new TableColumn();
						BeanUtils.copy(field, column);
						BeanUtils.copy(field.getConfigDto(), column);
						column.setName(field.getAlias());
						if(listField.getIndex()!=null) {
							column.setIndex(listField.getIndex());
						}else {
							column.setIndex(listDefine.getConfigDto().getTableList().getColumns().size());
						}
						column.setFixed(listField.getFixed());
						column.setWidth(listField.getWidth());
						column.setAlign(listField.getAlign());
						column.setRowMergeEnable(listField.isRowMergeEnable());
						column.setColMergeEnable(listField.isColMergeEnable());
						listDefine.getConfigDto().getTableList().getColumns().add(column);
					}
				}
				// 表单字段
				if(config.getShow()!=null && config.getShow().getForm()!=null) {
					FieldForm formField=config.getShow().getForm();
					if(formField.isEnable()) {
						FormItem formItem=new FormItem();
						BeanUtils.copy(field, formItem);
						BeanUtils.copy(field.getConfigDto(), formItem);
						formItem.setName(field.getAlias());
						if(Objects.equals(0, field.getIsNull())) {
							formItem.getProps().setRequired(true);
						}
						if(formField.getIndex()!=null) {
							formItem.setIndex(formField.getIndex());
						}else {
							formItem.setIndex(formDefine.getConfigDto().getForm().getWidgets().size());
						}
						formItem.setValue(field.getDataValue());
						formItem.getView().setDataFormat(field.getDataFormat());
						formItem.getView().setWidth(formField.getWidth());
						formItem.getView().setLabelWidth(formField.getLabelWidth());
						formItem.getView().setValueWidth(formField.getValueWidth());
						
						FieldWidget widget=config.getWidget();
						if(widget!=null) {
							formItem.setWidget(widget.getName());
							formItem.getProps().setPlaceholder(widget.getPlaceholder());
							formItem.getProps().setTooltip(widget.getTooltip());
							formItem.getProps().setHelp(widget.getHelp());
						}
						formDefine.getConfigDto().getForm().getWidgets().add(formItem);
					}
					
				}
				
				// 详情字段
				if(config.getShow()!=null && config.getShow().getView()!=null) {
					FieldForm viewField=config.getShow().getView();
					if(viewField.isEnable()) {
						FormItem formItem=new FormItem();
						BeanUtils.copy(field, formItem);
						BeanUtils.copy(field.getConfigDto(), formItem);
						formItem.setName(field.getAlias());
						if(Objects.equals(0, field.getIsNull())) {
							formItem.getProps().setRequired(true);
						}
						if(viewField.getIndex()!=null) {
							formItem.setIndex(viewField.getIndex());
						}else {
							formItem.setIndex(viewDefine.getConfigDto().getForm().getWidgets().size());
						}
						formItem.setValue(field.getDataValue());
						formItem.getView().setDataFormat(field.getDataFormat());
						formItem.getView().setWidth(viewField.getWidth());
						formItem.getView().setLabelWidth(viewField.getLabelWidth());
						formItem.getView().setValueWidth(viewField.getValueWidth());
						
						FieldWidget widget=config.getWidget();
						if(widget!=null) {
							formItem.setWidget(widget.getName());
							formItem.getProps().setPlaceholder(widget.getPlaceholder());
							formItem.getProps().setTooltip(widget.getTooltip());
							formItem.getProps().setHelp(widget.getHelp());
						}
						viewDefine.getConfigDto().getForm().getWidgets().add(formItem);
					}
					
				}
			}
		});	
		
		LogsUtil.add("保存：列表页面，表单页面，详情页面");
		pageDefineService.doSignature(listDefine);
		if(listDefine.getId()!=null) {
			// 更新
			int len = dataBaseDao.updateById(SqlBuilder.build(listDefine).field("vers","title","configs","signature"));
			LogsUtil.add("更新列表页面,len:%s",len);
		}else {
			// 新增
			listPage=new SysPageDefine();
			BeanUtils.copy(listDefine, listPage);
			int len = dataBaseDao.insert(listPage);
			LogsUtil.add("保存列表页面,len:%s",len);
		}
		
		pageDefineService.doSignature(formDefine);
		if(formDefine.getId()!=null) {
			// 更新
			int len = dataBaseDao.updateById(SqlBuilder.build(formDefine).field("vers","title","configs","signature"));
			LogsUtil.add("更新表单页面,len:%s",len);
		}else {
			// 新增
			formPage=new SysPageDefine();
			BeanUtils.copy(formDefine, formPage);
			int len = dataBaseDao.insert(formPage);
			LogsUtil.add("保存表单页面,len:%s",len);
		}
		
		pageDefineService.doSignature(viewDefine);
		if(viewDefine.getId()!=null) {
			// 更新
			int len = dataBaseDao.updateById(SqlBuilder.build(viewDefine).field("vers","title","configs","signature"));
			LogsUtil.add("更新详情页面,len:%s",len);
		}else {
			// 新增
			viewPage=new SysPageDefine();
			BeanUtils.copy(viewDefine, viewPage);
			int len = dataBaseDao.insert(viewPage);
			LogsUtil.add("保存详情页面,len:%s",len);
		}
		
		LogsUtil.add("退出：生成页面信息：增删改查,data define id:%s,list page id:%s,form page id:%s,view page id:%s",
				define.getId(),listPage.getId(),formPage.getId(),viewPage.getId());
		log.debug("退出：生成页面信息：增删改查,data define id:{},list page id:{},form page id:{},view page id:{}",
				define.getId(),listPage.getId(),formPage.getId(),viewPage.getId());
	}
	
	
	/**
	 * 发布页面信息：增删改查
	 * @param define
	 */
	public void releasePageInfo(SysDataDefine define) {
		log.info("进入：数据定义，列表，表单，详情页面发布方法");
		SysPageDefine listPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:list", define.getSn())));
		AssertUtil.service().notNull(listPage, "列表页面信息未找到");
		
		SysPageDefine formPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:form", define.getSn())));
		AssertUtil.service().notNull(formPage, "表单页面信息未找到");
		
		SysPageDefine viewPage=dataBaseDao.findOne(SqlBuilder.build(SysPageDefine.class)
				.params("sn", String.format("%s:view", define.getSn())));
		AssertUtil.service().notNull(viewPage, "详情页面信息未找到");
		
		pageDefineService.release(new HashSet<>(Arrays.asList(listPage.getId(),formPage.getId(),viewPage.getId())));
		
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
				if (DataQueryType.EQ.name().equals(query.getTypes())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(" AND ").append(field.getName()).append("=#{params.").append(field.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LIKE.name().equals(query.getTypes())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(" AND ").append(field.getName()).append(" LIKE #{'%'+params.").append(field.getAlias()).append("+'%'}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LLIKE.name().equals(query.getTypes())){
					whereFields.append(System.lineSeparator()).append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){").append(System.lineSeparator())
					.append(" AND ").append(field.getName()).append(" LIKE #{'%'+params.").append(field.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.RLIKE.name().equals(query.getTypes())){
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
		.filter(f->f.getConfigDto().getFkey()!=null)
		.filter(f->f.getConfigDto().getFkey().isEnable())
		.filter(f ->!ObjectUtil.isEmpty(f.getConfigDto().getFkey().getDsn()))
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

				if (DataQueryType.EQ.name().equals(fkfield.getConfigDto().getQuery().getTypes())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append("=#{params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LIKE.name().equals(fkfield.getConfigDto().getQuery().getTypes())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append(" LIKE #{'%'+params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("+'%'}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.LLIKE.name().equals(fkfield.getConfigDto().getQuery().getTypes())){
					fkWhereFields.append(System.lineSeparator())
					.append("-- @if(isNotEmpty(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias())
					.append(") && !isBlank(params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append(")")
					.append("){").append(System.lineSeparator())
					.append(" AND fkt.").append(fkfield.getName()).append(" LIKE #{'%'+params.").append(fkeyField.getAlias()).append("Params.").append(fkfield.getAlias()).append("}").append(System.lineSeparator())
					.append("-- @}");
				}
				if (DataQueryType.RLIKE.name().equals(fkfield.getConfigDto().getQuery().getTypes())){
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
	
	
	/**
	 * 	从数据库中加载table
	 * @param appId
	 * @param dsId
	 * @param tableName
	 * @return
	 */
	public Results<List<SysDataDefine>> loadFromDb(Long appId,Long dsId,String tableName){
		log.info("进入：从数据库中加载table方法,ds id:{},tableName:{}",dsId,tableName);
		
		SQLManager sqlManager = storageBaseService.getSQLManager(dsId);
		MetadataManager metadataManager = sqlManager.getMetaDataManager();
		List<String> tableNames = metadataManager.allTable().stream().filter(table->{
			if(StringUtils.isEmpty(tableName)) {
				return true;
			}
			return table.indexOf(tableName)>=0;
		}).collect(Collectors.toList());
		
		// 分批验证table是否已登记
		List<SysDataDefine> tableList = ListUtil.partition(tableNames, 500).stream().map(names->{
			Map<String, SysDataDefine> map=dataBaseDao.findList(SqlBuilder.build(SysDataDefine.class)
					.where("delFlag=0 and appId=? and dsId=? and name in [names]")
					.params("appId", appId)
					.params("dsId", dsId)
					.params("names", names))
					.stream().collect(Collectors.toMap(SysDataDefine::getName,v->v));
			return names.stream().map(name->{
				SysDataDefine define=new SysDataDefine();
				SysDataDefine tmp=map.get(name);
				if(tmp!=null) {
					define.setId(tmp.getId());
					define.setTitle(tmp.getTitle());
				}else {
					TableDesc table = metadataManager.getTable(name);
					if(StringUtils.isEmpty(table.getRemark())) {
						define.setTitle(StrUtil.toCamelCase(name));
					}else {
						define.setTitle(table.getRemark());
					}
				}
				define.setName(name);
				return define;
			}).collect(Collectors.toList());
		}).flatMap(List::stream).collect(Collectors.toList());
		
		return Results.success(tableList);
	}
	
	
	
	/**
	 * 从数据库中导入：数据定义
	 * @param appId
	 * @param dsId
	 * @param tables
	 * @param force
	 * @return
	 */
	public Results<String> impFromDb(Long appId, Long dsId,List<String> tables,boolean force){
		log.info("进入：从数据库中导入：数据定义,appId:{},dsId:{},tables:{},force:{}",appId,dsId,tables,force);
		LogsUtil.add("进入：从数据库中导入：数据定义,appId:%s,dsId:%s,tables:%s,force:%s",appId,dsId,tables,force);
		AssertUtil.service().notNull(dsId, "参数dsId不能为空").notEmpty(tables, "参数tables不能为空");
		
		if(!force) {
			// 验证表格是否已创建
			List<SysDataDefine> defineList = dataBaseDao.findList(SqlBuilder.build(SysDataDefine.class)
				.where("dsId=? and name in [?]")
				.params("dsId", dsId).params("name", tables));
			Set<String> hadTables = defineList.stream().map(df->df.getName()).collect(Collectors.toSet());
			if(!hadTables.isEmpty()) {
				return Results.failure(String.format("数据table%s已存在，是否重新导入这些table？", 
						JSONUtil.toJsonPrettyStr(hadTables)));
			}
		}
		
		// 获取SQL对象
		SQLManager sqlManager = storageBaseService.getSQLManager(dsId);
		StringBuffer error=new StringBuffer();
		Stream<TableDesc> tableDescs = tables.stream().map(table->{
			try {
				return sqlManager.getTableDesc(table);
			} catch (Exception e) {
				error.append(table).append(",");
				log.error(String.format("数据table %s 不存在", table),e);
			}
			return null;
		}).filter(t->t!=null);
		if(error.length()>0) {
			return Results.failure(String.format("数据table %s 不存在",error.subSequence(0, error.length()-1)));
		}
		
		// tableDescs 转换成 DataDefine
		tableDescs.forEach(table->{
			DataDefine dataDefine=new DataDefine();
			dataDefine.setCategory(DataDefineCategory.SQL.value());
			dataDefine.setIsCustom(0);
			dataDefine.setDelFlag(0);
			dataDefine.setStatus(1);
			dataDefine.setAppId(appId);
			dataDefine.setDsId(dsId);
			dataDefine.setSn(RandomUtil.randomString(20));
			dataDefine.setVers(1);
			BeanUtils.setDefaultValue(dataDefine, "appId",DEFAULT_APP_ID);	
			BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
			BeanUtils.setDefaultValue(dataDefine, "syncFlag",0);
			BeanUtils.setDefaultValue(dataDefine, "ordered",0);
			dataDefine.setName(table.getName());
			dataDefine.setTitle(StrUtil.toCamelCase(table.getName()));
			dataDefine.setDescs(table.getRemark());
			
			Set<String> fieldNames = table.getCols();
			Set<String> idNames = table.getIdNames();
			DataDefineConfig configs=new DataDefineConfig();
			
			// 解析table字段列表
			List<DataField> fields=fieldNames.stream().map(fieldName->{
				DataField field=new DataField();
				ColDesc col = table.getColDesc(fieldName);
				field.setName(col.getColName());
				field.setIsNull("YES".equalsIgnoreCase(col.getIsNullable())?1:0);
				if(StringUtils.isEmpty(col.getRemark())) {
					field.setTitle(col.getColName());
				}else {
					field.setTitle(col.getRemark());
					field.setDescs(col.getRemark());
				}
				field.setDataLen(col.getSize());
				field.setIsPk(0);
				if(idNames.contains(col.getColName())) {
					field.setIsPk(1);	
				}
				field.setDataType(JavaType.mapping.get(col.getSqlType()));
				field.setConfigs("{}");
				
				return field;
			}).collect(Collectors.toList());
			configs.setFields(fields);
			dataDefine.setConfigDto(configs);
			
			// 保存数据定义
			try {
				Results<DataDefine> results = save(dataDefine);
				if(!results.isSuccess()) {
					error.append("table[").append(dataDefine.getName()).append("]数据定义信息保存失败,");
				}
			} catch (Exception e) {
				log.error("保存数据定义信息失败",e);
				error.append("table[").append(dataDefine.getName()).append("]数据定义信息保存失败,");
			}
		});
		
		if(error.length()>0) {
			return Results.failure(error.subSequence(0, error.length()-1).toString());
		}
		
		return Results.success();
	}
	
	
	
	
	
	

}
