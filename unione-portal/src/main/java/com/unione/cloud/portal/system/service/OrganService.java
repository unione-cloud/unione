package com.unione.cloud.portal.system.service;

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
import com.unione.cloud.portal.system.model.SysOrgan;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class OrganService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private CacheManager cacheManager;
	
	
	/**
	 * 	加载机构信息
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:ORGAN",key = "#id",expire = 600,cacheType = CacheType.LOCAL,cacheNullValue = true)
	public SysOrgan loadOrgan(Long id) {
		log.debug("进入：加载机构信息方法，缓存未命中，从db中加载,id:{}",id);
		SysOrgan organ = dataBaseDao.findById(SqlBuilder.build(SysOrgan.class).id(id));
		log.debug("退出：加载机构信息方法，缓存未命中，从db中加载,id:{},result:{}",id,organ);
		return organ;
	}
	
	
	/**
	 * 	加载机构信息
	 * @param ids
	 * @return
	 */
	public Map<Long, SysOrgan> loadOrgan(Set<Long> ids) {
		log.debug("进入：加载机构信息方法,ids:{}",ids);
		if(ObjectUtil.isEmpty(ids)) {
			return MapUtil.empty();
		}
		
		Cache<Long, SysOrgan> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:ORGAN")
				.cacheType(CacheType.LOCAL)
				.cacheNullValue(true)
				.expire(Duration.ofSeconds(600))
				.build());
		Map<Long, SysOrgan> map = cache.getAll(ids);
		
		List<Long> idList = ids.stream().filter(id->!map.keySet().contains(id)).collect(Collectors.toList());
		if(!idList.isEmpty()) {
			dataBaseDao.findByIds(SqlBuilder.build(SysOrgan.class).ids(idList))
			.stream().forEach(organ->{
				map.put(organ.getId(), organ);
				cache.put(organ.getId(), organ);
			});
		}
		
		log.debug("退出：加载机构信息方法,ids:{},result len:{}",ids,map.size());
		return map;
	}
	

}
