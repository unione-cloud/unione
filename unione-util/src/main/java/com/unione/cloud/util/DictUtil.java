package com.unione.cloud.util;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

/**
 * 字典工具类
 */
@Component
public class DictUtil {

    private static CacheManager cacheManager;

    private static DataBaseDao dataBaseDao;

    private static RedisService redisService;

    @Autowired
    public void setRedisService(RedisService redisService) {
        DictUtil.redisService = redisService;
    }
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        DictUtil.cacheManager = cacheManager;
    }
    @Autowired
    public void setDataBaseDao(DataBaseDao dataBaseDao) {
        DictUtil.dataBaseDao = dataBaseDao;
    }

    private static Cache<String,DictCache> getCache(){
        QuickConfig config = QuickConfig.newBuilder("SYS:BASE:DICT:")
			.localExpire(Duration.ofSeconds(30))
		    .cacheType(CacheType.BOTH)
		    .build();
		return cacheManager.getOrCreateCache(config);
    }


    /**
     * 加载字典缓存
     * @param name
     * @return
     */
    public static DictCache load(String name){
        Cache<String,DictCache> cache = getCache();
        DictCache dict=cache.get(name);
        if(dict==null){
            dict=redisService.doHpdl(new HpdlProcess<DictCache>(String.format("hpdl:dict:%s", name)) {
                @Override
                public DictCache process() {
                    DictCache tmp=cache.get(name);
                    if(tmp==null) {
                        tmp=new DictCache();
                        List<Map<String,Object>> list = dataBaseDao.findList(SqlBuilder.build("base_dict").field("dictKey,dictValue")
                            .where("dictName=? and status = 1").where("dictName",name)
                            .sort(Sort.build("ordered", "asc"))
                            .dataPermis(PermisRule.ALL));
                        if(!list.isEmpty()){
                            tmp.setName(name);
                            for(Map<String,Object> item:list){
                                tmp.keyMap.put(item.get("dictKey").toString(), item.get("dictValue").toString());
                                tmp.valueMap.put(item.get("dictValue").toString(), item.get("dictKey").toString());
                            }
                        }
                        cache.put(name, tmp);
                    }
                    return tmp;
                }
            }, 300, 3);
        }
        return dict;
    }

    /**
     * 清空字典缓存
     * @param name
     */
    public static void clear(String name){
        getCache().remove(name);
    }
    
    @Data
    public static class DictCache implements Serializable{
        private static final long serialVersionUID = 1L;
        private String name;
        private Map<String,String> keyMap=new HashMap<>();
        private Map<String,String> valueMap=new HashMap<>();

        /**
         * 根据字典键获取字典值
         * @param key
         * @return
         */
        public String getValue(Object key){
            if(ObjectUtil.isEmpty(key)){
                return null;
            }
            String[] keys=key.toString().split(",");
            List<String> values=new ArrayList<>();
            for(String k:keys){
                if(ObjectUtil.isEmpty(k)){
                    continue;
                }
                values.add(keyMap.get(k.trim()));
            }
            if(!values.isEmpty()){
                return String.join(",", values);
            }
            return null;
        }

        /**
         * 根据字典值获取字典键
         * @param value
         * @return
         */
        public String getKey(String value){
            if(ObjectUtil.isEmpty(value)){
                return null;
            }
            String[] values=value.toString().split(",");
            List<String> keys=new ArrayList<>();
            for(String v:values){
                if(ObjectUtil.isEmpty(v)){
                    continue;
                }
                keys.add(valueMap.get(v.trim()));
            }
            if(!keys.isEmpty()){
                return String.join(",", keys);
            }
            return null;
        }

    }

}
