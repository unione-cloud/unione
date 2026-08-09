package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.system.model.SysResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResourceService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private CacheManager cacheManager;


    private Cache<Long, SysResource> getCache() {
        Cache<Long, SysResource> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:RESOURCE:")
                .cacheType(CacheType.LOCAL)
                .localExpire(Duration.ofSeconds(120))
                .cacheNullValue(true)
                .build());
        return cache;
    }

    
    public Map<Long,SysResource> load(Set<Long> ids){
        Map<Long,SysResource> map = new HashMap<>();
        Set<Long> uids=new HashSet<>();
        ids.forEach(id->{
            SysResource res=getCache().get(id);
            if(res!=null){
                map.put(id, res);
            }else{
                uids.add(id);
            }
        });

        if(!uids.isEmpty()){
            dataBaseDao.findByIds(SqlBuilder.build(SysResource.class,uids).dataPermis(PermisRule.ALL)).stream().forEach(res->{
                getCache().put(res.getId(), res);
                map.put(res.getId(), res);
            });
        }

        return map;
    }

    /**
     * 清除指定的资源缓存
     * @param id
     */
    public void clear(Long id) {
        getCache().remove(id);
    }


}
