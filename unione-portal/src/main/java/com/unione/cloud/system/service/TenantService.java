package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
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

	@Autowired
	private RedisService redisService;

	@Autowired
	private SessionService sessionService;


	@Value("${unione.cache.tenant.expire:72000}")
	private long CACHE_TIME;

	private Cache<Long, SysTenant> getCache(){
		return cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:TENANT")
			.cacheType(CacheType.BOTH)
			.cacheNullValue(true)
			.expire(Duration.ofSeconds(CACHE_TIME))
			.localExpire(Duration.ofSeconds(30))
			.build());
	}
	
	
	/**
	 * 	加载租户信息
	 * @param id
	 * @return
	 */
	@Cached(name="SYS:TENANT",key = "#id",expire = 600,cacheType = CacheType.LOCAL,cacheNullValue = true)
	public SysTenant loadTenant(Long id) {
		log.debug("进入：加载租户信息方法，缓存未命中，从db中加载,id:{}",id);
		Cache<Long, SysTenant> cache = getCache();
		SysTenant target = cache.get(id);
		if(target == null) {
			target = redisService.doHpdl(new HpdlProcess<SysTenant>(String.format("hpdl:tenant:%s",id)) {
				@Override
				public SysTenant process() {
					SysTenant tenant = cache.get(id);
					if(tenant == null) {
						tenant = dataBaseDao.findById(SqlBuilder.build(SysTenant.class).id(id));
						if(tenant==null){
							tenant = new SysTenant();
						}
						cache.put(id, tenant);
					}
					return tenant;
				}
			}, 500, 3);
		}
		AssertUtil.service().notNull(target, "租户信息不存在").notNull(target.getId(), "租户id不存在");
		log.debug("退出：加载租户信息方法，缓存未命中，从db中加载,id:{},result:{}",id,target);
		return target;
	}
	
	
	/**
	 * 	加载租户信息
	 * @param ids
	 * @return
	 */
	public Map<Long, SysTenant> loadTenant(Set<Long> ids) {
		log.debug("进入：加载租户信息方法,ids:{}",ids);
		if(ObjectUtil.isEmpty(ids)) {
			return MapUtil.empty();
		}
		
		Cache<Long, SysTenant> cache = getCache();
		Map<Long, SysTenant> map = cache.getAll(ids);
		
		List<Long> idList = ids.stream().filter(id->!map.keySet().contains(id)).collect(Collectors.toList());
		if(!idList.isEmpty()) {
			redisService.doHpdl(new HpdlProcess<Void>(String.format("hpdl:tenant:%s", sessionService.getUserId())) {
				@Override
				public Void process() {
					List<Long> uids=new ArrayList<>();
					idList.forEach(id->{
						SysTenant tenant=cache.get(id);
						if(tenant!=null){
							if(tenant.getId()!=null){
								map.put(id, tenant);
							}
						}else{
							uids.add(id);
						}
					});
					if(!uids.isEmpty()){
						dataBaseDao.findByIds(SqlBuilder.build(SysTenant.class).ids(uids))
						.stream().forEach(row->{
							map.put(row.getId(), row);
							cache.put(row.getId(), row);
						});
					}
					return null;
				}
			}, 500, 3);
			dataBaseDao.findByIds(SqlBuilder.build(SysTenant.class).ids(idList))
			.stream().forEach(row->{
				map.put(row.getId(), row);
				cache.put(row.getId(), row);
			});
		}
		log.debug("退出：加载租户信息方法,ids:{},result len:{}",ids,map.size());
		return map;
	}

	/**
	 * 	清除租户信息缓存
	 * @param id
	 */
	public void clear(long id){
		Cache<Long, SysTenant> cache = getCache();
		cache.remove(id);
	}


}
