package com.unione.cloud.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.dto.ResTreeNodeDto;
import com.unione.cloud.system.model.SysAppInfo;
import com.unione.cloud.system.model.SysResource;
import com.unione.cloud.system.model.SysRolePermis;
import com.unione.cloud.system.model.SysSystem;
import com.unione.cloud.system.model.SysUserPermis;

@Service
public class SystemResourceService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionService;

    public List<ResTreeNodeDto> tree(String type, Long targetId) {
        List<ResTreeNodeDto> nodes = new ArrayList<>();
        if (!"permisUser".equals(type) && !"permisRole".equals(type)
                && !"view".equals(type) && !"mine".equals(type)) {
            return nodes;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("user", sessionService.getPrincipal());
        params.put("isAdmin", sessionService.isAdmin() || sessionService.hasRole(UserRoles.SUPPER_ADMIN));
        List<SysSystem> systems = dataBaseDao.findList("system.SysResource.loadSysSystemList", params, SysSystem.class);
        if (systems.isEmpty()) {
            return nodes;
        }
        Map<String, Integer> permissions = new HashMap<>();
        if (targetId != null && "permisUser".equals(type)) {
            dataBaseDao.findList(SqlBuilder.build(SysUserPermis.class).where("userId", targetId))
                    .forEach(row -> permissions.put(key(row.getResType(), row.getResId()), row.getEnDilivery()));
        }
        if (targetId != null && "permisRole".equals(type)) {
            dataBaseDao.findList(SqlBuilder.build(SysRolePermis.class).where("roleId", targetId))
                    .forEach(row -> permissions.put(key(row.getResType(), row.getResId()), row.getEnDilivery()));
        }
        Map<Long, SysSystem> systemMap = new HashMap<>();
        systems.forEach(system -> {
            systemMap.put(system.getId(), system);
            ResTreeNodeDto node = new ResTreeNodeDto();
            node.setId(system.getId());
            node.setPid(-1L);
            node.setNtype("system");
            node.setIconName("GlobalOutlined");
            node.setAppId(system.getId());
            node.setSysId(system.getId());
            node.setTitle(system.getName());
            node.setName(system.getCtx());
            node.setPlatform(system.getTypes());
            applyPermission(node, permissions);
            nodes.add(node);
        });
        params.put("sysIds", systemMap.keySet());
        List<SysResource> resources = dataBaseDao.findList("system.SysResource.loadSysSystemResourceTree", params, SysResource.class);
        Set<Long> resourceIds = resources.stream().map(SysResource::getId).collect(Collectors.toSet());
        resources.forEach(resource -> {
            if (!Long.valueOf(-1L).equals(resource.getParentId()) && !resourceIds.contains(resource.getParentId())) {
                return;
            }
            ResTreeNodeDto node = new ResTreeNodeDto();
            BeanUtils.copyProperties(resource, node);
            node.setNtype(resource.getTypes());
            node.setSysId(resource.getSysId());
            // 沿用授权表appId字段保存所属容器，根节点类型区分system和app。
            node.setAppId(resource.getSysId());
            node.setPid(Long.valueOf(-1L).equals(resource.getParentId()) ? resource.getSysId() : resource.getParentId());
            node.setPlatform(systemMap.get(resource.getSysId()).getTypes());
            applyPermission(node, permissions);
            nodes.add(node);
        });
        return nodes;
    }

    public String loadTitle(String resType, Long resId) {
        if ("system".equals(resType)) {
            SysSystem system = dataBaseDao.findById(SqlBuilder.build(SysSystem.class, resId));
            AssertUtil.service().notNull(system, "系统不存在");
            return system.getName();
        }
        if ("app".equals(resType)) {
            SysAppInfo app = dataBaseDao.findById(SqlBuilder.build(SysAppInfo.class, resId));
            AssertUtil.service().notNull(app, "应用不存在");
            return app.getName();
        }
        SysResource resource = dataBaseDao.findById(SqlBuilder.build(SysResource.class, resId));
        AssertUtil.service().notNull(resource, "资源不存在");
        return resource.getTitle();
    }

    private String key(String type, Long id) {
        return type + ":" + id;
    }

    private void applyPermission(ResTreeNodeDto node, Map<String, Integer> permissions) {
        String key = key(node.getNtype(), node.getId());
        if (permissions.containsKey(key)) {
            node.setChecked(true);
            node.setEnDilivery(Integer.valueOf(1).equals(permissions.get(key)) ? 1 : 0);
        }
    }
}
