package com.unione.cloud.system.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntSupplier;

import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.system.model.SysSystem;

/** Allocates a runtime-wide context while holding the shared creation lock. */
@Service
public class SystemContextService {
    private final DataBaseDao dataBaseDao;
    private final RedisService redisService;

    public SystemContextService(DataBaseDao dataBaseDao, RedisService redisService) {
        this.dataBaseDao = dataBaseDao;
        this.redisService = redisService;
    }

    public int insert(SysSystem entity) {
        return save(entity, () -> dataBaseDao.insert(entity));
    }

    public int update(SysSystem entity, String[] fields) {
        return save(entity, () -> dataBaseDao.updateById(SqlBuilder.build(entity).field(fields)));
    }

    private int save(SysSystem entity, IntSupplier persistence) {
        Integer inserted = redisService.doHpdl(new HpdlProcess<Integer>("system:context:create") {
            @Override
            public Integer process() {
                Set<String> contexts = new HashSet<>();
                // Context routes are global, so include every tenant and deleted record.
                for (SysSystem system : dataBaseDao.findList(SqlBuilder.build(SysSystem.class)
                        .dataPermis(PermisRule.ALL))) {
                    if (system.getCtx() != null && !system.getId().equals(entity.getId())) {
                        contexts.add(system.getCtx().toLowerCase(Locale.ROOT));
                    }
                }
                if (entity.getId() == null) {
                    entity.setCtx(availableContext(entity.getCtx(), contexts));
                } else {
                    AssertUtil.service().isTrue(!contexts.contains(entity.getCtx().toLowerCase(Locale.ROOT)),
                            "系统 ctx 已存在，请使用其他英文简称");
                }
                return persistence.getAsInt();
            }
        }, 60000L, 100L, 100);
        AssertUtil.service().notNull(inserted, "系统创建繁忙，请稍后重试");
        return inserted;
    }

    static String availableContext(String base, Set<String> contexts) {
        AssertUtil.service().isTrue(base != null && !base.isBlank() && base.length() <= 15,
                "系统 ctx 长度须为 1～15 位");
        String candidate = base;
        for (long sequence = 1; contexts.contains(candidate.toLowerCase(Locale.ROOT)); sequence++) {
            String suffix = Long.toString(sequence);
            candidate = base.substring(0, Math.min(base.length(), 15 - suffix.length())) + suffix;
        }
        return candidate;
    }
}
