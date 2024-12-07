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
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.form.data.dto.DataDefineDto.DataFieldDto;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataDefineRelease;
import com.unione.cloud.form.data.model.SysDataField;
import com.unione.cloud.form.data.model.SysDataFieldRelease;
import com.unione.cloud.form.data.storage.model.DataDefine;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataDefineCache {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	/**
	 * 	加载数据模型
	 * @param sn
	 * @return
	 */
	public DataDefine load(String sn) {
		log.debug("进入：加载数据模型方法,sn:{}",sn);
		if(sn.endsWith("@dev")) {
			return load4Dev(sn.substring(0,sn.length()-4));
		}
		return loadRelease(sn);
	}
	
	
	/**
	 * 	加载数据模型【发布】
	 * @param sn
	 * @return
	 */
	@Cached(name="SYS:DATA:MODEL:",key = "#sn",expire = 3600,localExpire = 180,cacheType = CacheType.BOTH,cacheNullValue = true)
	public DataDefine loadRelease(String sn) {
		log.info("进入：从db加载数据模型【发布】方法,sn:{}",sn);
		DataDefine model=null;
		SysDataDefineRelease tmp = new SysDataDefineRelease();
		if(sn.indexOf("@")>0) {
			sn=sn.substring(0, sn.indexOf("@"));
			tmp.setVers(Integer.parseInt(sn.substring(sn.indexOf("@")+1)));
		}
		tmp.setSn(sn);
		tmp = dataBaseDao.findOne(tmp);
		log.info("退出：从db加载数据模型【发布】方法,sn:{},data model:{}",sn,tmp);
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataFieldRelease sdfr=new SysDataFieldRelease();
			sdfr.setModelId(model.getId());
			List<SysDataFieldRelease> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataFieldDto> fields = sdfrs.stream().map(f->{
				DataFieldDto field=new DataFieldDto();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					//field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldDtoConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
		}
		
		AssertUtil.service().notNull(model, "数据模型未找到");
		return model;
	}
	
	
	/**
	 * 	加载数据模型【发布】
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:DATA:MODEL:",key = "#id",expire = 3600,localExpire = 180,cacheType = CacheType.BOTH,cacheNullValue = true)
	public DataDefine load(Long id) {
		log.info("进入：从db加载数据模型【发布】方法,id:{}",id);
		DataDefine model=null;
		SysDataDefineRelease tmp = dataBaseDao.findById(SysDataDefineRelease.class, id);
		log.info("退出：从db加载数据模型【发布】方法,id:{},data model:{}",id,tmp);
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataFieldRelease sdfr=new SysDataFieldRelease();
			sdfr.setModelId(model.getId());
			List<SysDataFieldRelease> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataFieldDto> fields = sdfrs.stream().map(f->{
				DataFieldDto field=new DataFieldDto();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
//					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldDtoConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			
		}
		
		AssertUtil.service().notNull(model, "数据模型未找到");
		return model;
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
	public DataDefine load4Dev(String sn) {
		log.info("进入：从db加载数据模型【dev】方法,sn:{}",sn);
		DataDefine model=null;
		SysDataDefine tmp = new SysDataDefine();
		tmp.setSn(sn);
		tmp = dataBaseDao.findOne(tmp);
		log.info("退出：从db加载数据模型【dev】方法,sn:{},data model:{}",sn,tmp);
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataField sdfr=new SysDataField();
			sdfr.setModelId(model.getId());
			List<SysDataField> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataFieldDto> fields = sdfrs.stream().map(f->{
				DataFieldDto field=new DataFieldDto();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
//					field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldDtoConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
		}
		
		AssertUtil.service().notNull(model, "数据模型未找到");
		return model;
	}
	
	/**
	 * 	加载数据模型【dev】
	 * @param id
	 * @return
	 */
	public DataDefine load4Dev(Long id) {
		log.info("进入：从db加载数据模型【dev】方法,id:{}",id);
		DataDefine model=null;
		SysDataDefine tmp = dataBaseDao.findById(SysDataDefine.class, id);
		log.info("退出：从db加载数据模型【dev】方法,id:{},data model:{}",id,tmp);
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
			
			SysDataField sdfr=new SysDataField();
			sdfr.setModelId(model.getId());
			List<SysDataField> sdfrs = dataBaseDao.findList(SqlBuilder.build(sdfr).where("modelId=?"));
			List<DataFieldDto> fields = sdfrs.stream().map(f->{
				DataFieldDto field=new DataFieldDto();
				BeanUtil.copyProperties(f, field);
				if(!StringUtils.isEmpty(f.getConfigs())) {
					//field.setConfigs(JSONUtil.toBean(f.getConfigs(), DataFieldDtoConfig.class));
				}
				return field;
			}).collect(Collectors.toList());
			
			model.setFields(fields);
			return model;
		}
		
		AssertUtil.service().notNull(model, "数据模型未找到");
		return model;
	}
	

}
