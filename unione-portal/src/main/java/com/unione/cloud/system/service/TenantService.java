package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.system.model.SysTenant;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TenantService {

    @Autowired
    private DataBaseDao dataBaseDao;


    @Autowired
	private CacheManager cacheManager;
	
	
	/**
	 * 	加载租户信息
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:TENANT",key = "#id",expire = 600,cacheType = CacheType.LOCAL,cacheNullValue = true)
	public SysTenant loadTenant(Long id) {
		log.debug("进入：加载租户信息方法，缓存未命中，从db中加载,id:{}",id);
		SysTenant target = dataBaseDao.findById(SqlBuilder.build(SysTenant.class).id(id));
		log.debug("退出：加载租户信息方法，缓存未命中，从db中加载,id:{},result:{}",id,target);
		return target;
	}
	
	
	/**
	 * 	加载租户信息
	 * @param ids
	 * @return
	 */
	public Map<Long, SysTenant> loadOrgan(Set<Long> ids) {
		log.debug("进入：加载租户信息方法,ids:{}",ids);
		if(ObjectUtil.isEmpty(ids)) {
			return MapUtil.empty();
		}
		
		Cache<Long, SysTenant> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:TENANT")
				.cacheType(CacheType.LOCAL)
				.cacheNullValue(true)
				.expire(Duration.ofSeconds(600))
				.build());
		Map<Long, SysTenant> map = cache.getAll(ids);
		
		List<Long> idList = ids.stream().filter(id->!map.keySet().contains(id)).collect(Collectors.toList());
		if(!idList.isEmpty()) {
			dataBaseDao.findByIds(SqlBuilder.build(SysTenant.class).ids(idList))
			.stream().forEach(row->{
				map.put(row.getId(), row);
				cache.put(row.getId(), row);
			});
		}
		
		log.debug("退出：加载租户信息方法,ids:{},result len:{}",ids,map.size());
		return map;
	}




}
