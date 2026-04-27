package com.unione.cloud.system.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.system.dto.SystemInfoDto;
import com.unione.cloud.system.model.SysSystem;

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


    private Cache<String, SystemInfoDto> getCache() {
        Cache<String, SystemInfoDto> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:SYSTEM:INFO")
                .cacheType(CacheType.BOTH)
                .cacheNullValue(true)
                .build());
        return cache;
    }


    /**
     * 加载用户当前访问的系统
     * @return
     */
    public SystemInfoDto load(){
        String referer=request.getHeader("referer");
        String ctx="portal";
        try {
            String path = new URI(referer).getPath();
            if (path != null && !path.isEmpty()){
                String[] parts = path.split("/");
                for (String p : parts) {
                    if (!p.isEmpty()) {
                        ctx=p;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        if(ObjectUtil.isEmpty(ctx) || ctx.equals("/") || ctx.equals("login")){
            ctx="portal";
        }
        if(ctx.startsWith("/")){
            ctx=ctx.substring(1);
        }
        return load(ctx);
    }

    /**
     * 加载指定的系统
     * @param ctx
     * @return
     */
    public SystemInfoDto load(String ctx){
        SystemInfoDto sys=getCache().get(ctx);
        if(sys==null){
           sys=redisService.doHpdl(new HpdlProcess<SystemInfoDto>(String.format("hpdl:system:%s", ctx)) {
            @Override
            public SystemInfoDto process() {
                SystemInfoDto tmp=getCache().get(ctx);
                if(tmp==null){
                    SysSystem sys=new SysSystem();
                    sys.setCtx(ctx);
                    sys.setDelFlag(0);
                    sys=dataBaseDao.findOne(SqlBuilder.build(sys));
                    if(sys==null){
                        tmp=new SystemInfoDto();
                    }else{
                        tmp=SystemInfoDto.from(sys);
                    }
                    getCache().put(ctx, tmp);
                    return tmp;
                }
                return tmp;
            }
           }, 300, 3);
        }
        AssertUtil.service().notNull(sys, "系统信息不存在").notNull(sys.getId(), "系统信息不存在");
        return sys;
    }

    /**
     * 清除指定的系统缓存
     * @param ctx
     */
    public void clear(String ctx){
        getCache().remove(ctx);
    }


}
