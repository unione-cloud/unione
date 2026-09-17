package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.unione.cloud.system.dto.ResourceMoveDto;
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

    @Transactional(rollbackFor = Exception.class)
    public Long move(ResourceMoveDto request) {
        validateMoveRequest(request);

        SysResource source = dataBaseDao.findById(SqlBuilder.build(SysResource.class, request.getId()));
        AssertUtil.service().notNull(source, "待移动资源未找到");
        if (request.getSysId() != null) {
            AssertUtil.service().isTrue(Objects.equals(request.getSysId(), source.getSysId()), "资源不属于当前系统");
        }
        AssertUtil.service().isTrue(!Objects.equals(source.getId(), request.getParentId()), "资源不能移动到自身下级");
        validateMoveTarget(source, request.getParentId());

        Long originalParentId = source.getParentId();
        if (!Objects.equals(originalParentId, request.getParentId())) {
            normalizeChildren(source.getSysId(), originalParentId, source.getId(), null, null);
        }
        normalizeChildren(source.getSysId(), request.getParentId(), source.getId(), source, request.getOrdered());
        refreshLeaf(originalParentId, source.getSysId());
        refreshLeaf(request.getParentId(), source.getSysId());
        return source.getId();
    }

    private void validateMoveRequest(ResourceMoveDto request) {
        AssertUtil.service().notNull(request, "请求参数不能为空")
                .notNull(request.getId(), "资源ID不能为空")
                .notNull(request.getParentId(), "目标父节点不能为空")
                .notNull(request.getOrdered(), "目标顺序不能为空")
                .isTrue(request.getOrdered() >= 0, "目标顺序不能小于0");
    }

    private void validateMoveTarget(SysResource source, Long targetParentId) {
        if (Objects.equals(-1L, targetParentId)) {
            return;
        }
        Set<Long> visited = new HashSet<>();
        Long currentId = targetParentId;
        while (!Objects.equals(-1L, currentId)) {
            AssertUtil.service().isTrue(visited.add(currentId), "页面树存在循环引用");
            AssertUtil.service().isTrue(!Objects.equals(source.getId(), currentId), "资源不能移动到自身下级");
            SysResource current = dataBaseDao.findById(SqlBuilder.build(SysResource.class, currentId));
            AssertUtil.service().notNull(current, "目标父节点未找到")
                    .isTrue(Objects.equals(source.getSysId(), current.getSysId()), "目标父节点不属于当前系统");
            currentId = current.getParentId();
        }
    }

    private void normalizeChildren(Long sysId, Long parentId, Long sourceId,
            SysResource source, Integer targetIndex) {
        SysResource query = new SysResource();
        query.setSysId(sysId);
        query.setParentId(parentId);
        List<SysResource> siblings = dataBaseDao.findList(SqlBuilder.build(query));
        siblings.removeIf(item -> Objects.equals(item.getId(), sourceId));
        siblings.sort(Comparator.comparing(
                SysResource::getOrdered, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SysResource::getId));
        if (source != null) {
            int index = Math.min(targetIndex, siblings.size());
            source.setParentId(parentId);
            siblings.add(index, source);
        }
        for (int index = 0; index < siblings.size(); index++) {
            SysResource sibling = siblings.get(index);
            if (!Objects.equals(sibling.getParentId(), parentId)
                    || !Objects.equals(sibling.getOrdered(), index)) {
                sibling.setParentId(parentId);
                sibling.setOrdered(index);
                dataBaseDao.updateById(SqlBuilder.build(sibling).field("parentId", "ordered"));
            }
        }
    }

    private void refreshLeaf(Long parentId, Long sysId) {
        if (parentId == null || Objects.equals(-1L, parentId)) {
            return;
        }
        SysResource parent = dataBaseDao.findById(SqlBuilder.build(SysResource.class, parentId));
        if (parent == null || !Objects.equals(parent.getSysId(), sysId)) {
            return;
        }
        SysResource query = new SysResource();
        query.setSysId(sysId);
        query.setParentId(parentId);
        parent.setIsLeaf(dataBaseDao.findList(SqlBuilder.build(query)).isEmpty() ? 1 : 0);
        dataBaseDao.updateById(SqlBuilder.build(parent).field("isLeaf"));
    }

}
