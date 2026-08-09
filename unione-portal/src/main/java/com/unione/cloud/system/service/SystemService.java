package com.unione.cloud.system.service;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.system.dto.SystemInfoDto;
import com.unione.cloud.system.model.SysResource;
import com.unione.cloud.system.model.SysSystem;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SystemService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisService redisService;

    @Autowired
    private HttpServletRequest request;

    private Cache<String, Long> getCtxCache() {
        Cache<String, Long> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:SYSTEM:CTX:")
                .cacheType(CacheType.BOTH)
                .localExpire(Duration.ofSeconds(60))
                .cacheNullValue(true)
                .build());
        return cache;
    }

    private Cache<Long, SystemInfoDto> getIdCache() {
        Cache<Long, SystemInfoDto> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:SYSTEM:ID:")
                .cacheType(CacheType.BOTH)
                .localExpire(Duration.ofSeconds(60))
                .cacheNullValue(true)
                .build());
        return cache;
    }

    /**
     * 加载用户当前访问的系统
     * 
     * @return
     */
    public SystemInfoDto load() {
        String referer = request.getHeader("referer");
        String ctx = "portal";
        try {
            String path = new URI(referer).getPath();
            if (path != null && !path.isEmpty()) {
                String[] parts = path.split("/");
                for (String p : parts) {
                    if (!p.isEmpty()) {
                        ctx = p;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        if (ObjectUtil.isEmpty(ctx) || ctx.equals("/") || ctx.equals("login")) {
            ctx = "portal";
        }
        if (ctx.startsWith("/")) {
            ctx = ctx.substring(1);
        }
        return load(ctx);
    }

    /**
     * 加载指定的系统
     * 
     * @param ctx
     * @return
     */
    public SystemInfoDto load(String ctx) {
        LogsUtil.add("加载系统:%s", ctx);
        Long sysId=getCtxCache().get(ctx);
        if(sysId!=null){
            return getIdCache().get(sysId);
        }
        SystemInfoDto sys = redisService.doHpdl(new HpdlProcess<SystemInfoDto>(String.format("hpdl:system:%s", ctx)) {
            @Override
            public SystemInfoDto process() {
                Long sid=getCtxCache().get(ctx);
                if(sid!=null){
                    return getIdCache().get(sid);
                }
               
                SystemInfoDto tmp = new SystemInfoDto();
                SysSystem sys = new SysSystem();
                sys.setCtx(ctx);
                sys.setDelFlag(0);
                sys = dataBaseDao.findOne(SqlBuilder.build(sys).dataPermis(PermisRule.ALL));
                if (sys != null) {
                    tmp = SystemInfoDto.from(sys);
                    getCtxCache().put(ctx, sys.getId());
                    getIdCache().put(sys.getId(), tmp);
                }else{
                    getCtxCache().put(ctx, -1L);
                }
                return tmp;
            }
        }, 300, 3);
        
        AssertUtil.service().notNull(sys, String.format("系统信息[%s]不存在", ctx)).notNull(sys.getId(), String.format("系统信息[%s]不存在", ctx));
        return sys;
    }

    
    public Map<Long,SystemInfoDto> load(Set<Long> ids){
        Map<Long,SystemInfoDto> map = new HashMap<>();
        Set<Long> uids=new HashSet<>();
        ids.forEach(id->{
            SystemInfoDto sys=getIdCache().get(id);
            if(sys!=null){
                map.put(id, sys);
            }else{
                uids.add(id);
            }
        });

        if(!uids.isEmpty()){
            dataBaseDao.findByIds(SqlBuilder.build(SysSystem.class,uids).dataPermis(PermisRule.ALL)).stream().forEach(sys->{
                SystemInfoDto info=SystemInfoDto.from(sys);
                getCtxCache().put(info.getCtx(),info.getId());
                getIdCache().put(info.getId(), info);
                map.put(info.getId(), info);
            });
        }

        return map;
    }

    /**
     * 清除指定的系统缓存
     * 
     * @param ctx
     */
    public void clear(String ctx) {
        Long sysId=getCtxCache().get(ctx);
        if(sysId!=null){
            getIdCache().remove(sysId);
        }
        getCtxCache().remove(ctx);
    }

    /**
     * 删除系统页面
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePage(Set<Long> ids) {
        AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
        List<SysResource> rows = dataBaseDao.findByIds(SqlBuilder.build(SysResource.class, new ArrayList<>(ids)));
        LogsUtil.add("批量删除资源:%s", rows.size());
        List<Long> refIds = new ArrayList<>();
        rows.stream().forEach(row -> {
            if ("page".equals(row.getTypes()) && row.getRefId() != null) {
                refIds.add(row.getRefId());
            }
        });
        LogsUtil.add("批量删除页面:%s", refIds.size());

        dataBaseDao.deleteById(SqlBuilder.build(SysResource.class, ids));
        if (!refIds.isEmpty()) {
            int len = dataBaseDao.deleteById("deleteSystemPage", SqlBuilder.build(SysSystem.class, refIds));
            if(len<refIds.size()){
                dataBaseDao.deleteById("deleteSystemPageLogic", SqlBuilder.build(SysSystem.class, refIds));
            }
        }
    }

}
