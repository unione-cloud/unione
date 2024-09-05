package com.unione.cloud.form.cache;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.form.data.model.SysDataField;
import com.unione.cloud.form.data.model.SysDataFieldRelease;
import com.unione.cloud.form.data.model.SysDataModel;
import com.unione.cloud.form.data.model.SysDataModelRelease;
import com.unione.cloud.form.data.storage.model.DataField;
import com.unione.cloud.form.data.storage.model.DataFieldConfig;
import com.unione.cloud.form.data.storage.model.DataModel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataModelCache {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	/**
	 * 	加载数据模型【发布】
	 * @param sn
	 * @return
	 */
	@Cached(name="SYS:DATA:MODEL:",key = "#sn",expire = 3600,localExpire = 180,cacheType = CacheType.BOTH,cacheNullValue = true)
	public DataModel load(String sn) {
		log.info("进入：从db加载数据模型【发布】方法,sn:{}",sn);
		SysDataModelRelease tmp = new SysDataModelRelease();
		if(sn.indexOf("@")>0) {
			sn=sn.substring(0, sn.indexOf("@"));
			tmp.setVers(Integer.parseInt(sn.substring(sn.indexOf("@")+1)));
		}
		tmp.setSn(sn);
		tmp = dataBaseDao.findOne(tmp);
		log.info("退出：从db加载数据模型【发布】方法,sn:{},data model:{}",sn,tmp);
		if(tmp!=null) {
			DataModel model=new DataModel();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataFieldRelease sdfr=new SysDataFieldRelease();
			sdfr.setModelId(model.getId());
			List<SysDataFieldRelease> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataField> fields = sdfrs.stream().map(f->{
				DataField field=new DataField();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			return model;
		}
		return null;
	}
	
	/**
	 * 	加载数据模型【发布】
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:DATA:MODEL:",key = "#id",expire = 3600,localExpire = 180,cacheType = CacheType.BOTH,cacheNullValue = true)
	public DataModel load(Long id) {
		log.info("进入：从db加载数据模型【发布】方法,id:{}",id);
		SysDataModelRelease tmp = dataBaseDao.findById(SysDataModelRelease.class, id);
		log.info("退出：从db加载数据模型【发布】方法,id:{},data model:{}",id,tmp);
		if(tmp!=null) {
			DataModel model=new DataModel();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataFieldRelease sdfr=new SysDataFieldRelease();
			sdfr.setModelId(model.getId());
			List<SysDataFieldRelease> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataField> fields = sdfrs.stream().map(f->{
				DataField field=new DataField();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			
			return model;
		}
		return null;
	}
	
	/**
	 * 	清空缓存
	 * @param sn
	 */
	@CacheInvalidate(name = "SYS:DATA:MODEL:", key = "#sn")
	public void clear(String sn) {}
	
	/**
	 * 	清空缓存
	 * @param id
	 */
	@CacheInvalidate(name = "SYS:DATA:MODEL:", key = "#id")
	public void clear(Long id) {}
	
	
	/**
	 * 	加载数据模型【dev】
	 * @param sn
	 * @return
	 */
	public DataModel load4Dev(String sn) {
		log.info("进入：从db加载数据模型【dev】方法,sn:{}",sn);
		SysDataModel tmp = new SysDataModel();
		if(sn.indexOf("@")>0) {
			sn=sn.substring(0, sn.indexOf("@"));
		}
		tmp.setSn(sn);
		tmp = dataBaseDao.findOne(tmp);
		log.info("退出：从db加载数据模型【dev】方法,sn:{},data model:{}",sn,tmp);
		if(tmp!=null) {
			DataModel model=new DataModel();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataField sdfr=new SysDataField();
			sdfr.setModelId(model.getId());
			List<SysDataField> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataField> fields = sdfrs.stream().map(f->{
				DataField field=new DataField();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			return model;
		}
		return null;
	}
	
	/**
	 * 	加载数据模型【dev】
	 * @param id
	 * @return
	 */
	public DataModel load4Dev(Long id) {
		log.info("进入：从db加载数据模型【dev】方法,id:{}",id);
		SysDataModel tmp = dataBaseDao.findById(SysDataModel.class, id);
		log.info("退出：从db加载数据模型【dev】方法,id:{},data model:{}",id,tmp);
		if(tmp!=null) {
			DataModel model=new DataModel();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataField sdfr=new SysDataField();
			sdfr.setModelId(model.getId());
			List<SysDataField> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataField> fields = sdfrs.stream().map(f->{
				DataField field=new DataField();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			return model;
		}
		return null;
	}
	

}
