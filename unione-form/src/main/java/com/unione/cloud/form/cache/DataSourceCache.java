package com.unione.cloud.form.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.form.data.model.SysDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataSourceCache {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	/**
	 * 	加载数据源
	 * @param id
	 * @return
	 */
	@Cached(name="sys:ds:",key = "#id",expire = 3600,cacheType = CacheType.BOTH,cacheNullValue = true)
	public SysDataSource load(Long id) {
		log.info("进入：从db加载数据源方法,id:{}",id);
		SysDataSource dataSource = dataBaseDao.findById(SysDataSource.class, id);
		log.info("退出：从db加载数据源方法,id:{},data source:{}",id,dataSource);
		return dataSource;
	}
	
	/**
	 * 	清空缓存
	 * @param id
	 */
	@CacheInvalidate(name = "sys:ds:", key = "#id")
	public void clear(Long id) {}
	

}
