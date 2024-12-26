package com.unione.cloud.form.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.form.data.model.SysDataDefine;
import com.unione.cloud.form.data.model.SysDataDefineRelease;
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
		SysDataDefineRelease tmp = SysDataDefineRelease.builder().sn(sn).build();
		if(sn.indexOf("@")>0) {
			tmp.setVers(Integer.parseInt(sn.substring(sn.indexOf("@")+1)));
			sn=sn.substring(0, sn.indexOf("@"));
		}
		tmp = dataBaseDao.findOne(SqlBuilder.build(tmp));
		log.info("退出：从db加载数据模型【发布】方法,sn:{},data model:{}",sn,tmp);
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
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
	 * 	加载数据模型【dev】
	 * @param sn
	 * @return
	 */
	public DataDefine load4Dev(String sn) {
		log.info("进入：从db加载数据模型【dev】方法,sn:{}",sn);
		DataDefine model=null;
		AssertUtil.service().notNull(sn, "数据编码不能为空");
		SysDataDefine tmp = SysDataDefine.builder().sn(sn).build();
		tmp = dataBaseDao.findOne(SqlBuilder.build(tmp));
		if(tmp!=null) {
			model=new DataDefine();
			BeanUtil.copyProperties(tmp, model);
		}
		
		AssertUtil.service().notNull(model, "数据模型未找到");
		log.info("退出：从db加载数据模型【dev】方法,sn:{},data model:{}",sn,model.getId());
		return model;
	}
	
	

}
