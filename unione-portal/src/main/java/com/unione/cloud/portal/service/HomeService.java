package com.unione.cloud.portal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.portal.dto.AppDto;
import com.unione.cloud.portal.dto.ResourceDto;
import com.unione.cloud.web.logs.LogsUtil;

@Service
public class HomeService {

    private static final Set<String> MENU_TYPES = Set.of("menu", "btn", "form", "flow", "page");

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionService;

    public List<AppDto> menus(String type) {
        AssertUtil.service().notIn(type, Arrays.asList("pc", "app"), "参数type取值范围[pc,app]");
        LogsUtil.add("加载门户菜单,type:%s", type);
        Map<String, Object> params = new HashMap<>();
        params.put("user", sessionService.getPrincipal());
        params.put("type", type);
        params.put("isAdmin", sessionService.isAdmin() || sessionService.hasRole(UserRoles.SUPPER_ADMIN));

        List<AppDto> apps = new ArrayList<>(dataBaseDao.findList("portal.permision.loadAppPermisForUser", params, AppDto.class));
        List<AppDto> systems = dataBaseDao.findList("portal.permision.loadSystemPermisForUser", params, AppDto.class);
        // 分别装载资源，避免不同容器的菜单和工具混合。
        loadResources(apps, params, false);
        loadResources(systems, params, true);
        apps.forEach(app -> app.setCategory("app"));
        systems.forEach(system -> system.setCategory("system"));
        apps.addAll(systems);
        return apps;
    }

    private void loadResources(List<AppDto> containers, Map<String, Object> params, boolean system) {
        if (containers.isEmpty()) {
            return;
        }
        List<Long> ids = containers.stream().map(AppDto::getId).collect(Collectors.toList());
        params.put(system ? "sysIds" : "appIds", ids);
        String sql = system ? "portal.permision.loadSystemResorucePermisForUser"
                : "portal.permision.loadResorucePermisForUser";
        List<ResourceDto> resources = dataBaseDao.findList(sql, params, ResourceDto.class);
        Map<Long, List<ResourceDto>> menus = new HashMap<>();
        Map<Long, List<ResourceDto>> tools = new HashMap<>();
        Map<Long, ResourceDto> menuMap = new HashMap<>();
        for (ResourceDto resource : resources) {
            if (MENU_TYPES.contains(resource.getTypes())) {
                menuMap.put(resource.getId(), resource);
            }
        }
        for (ResourceDto resource : resources) {
            Long containerId = system ? resource.getSysId() : resource.getAppId();
            if ("tool".equals(resource.getTypes())) {
                tools.computeIfAbsent(containerId, key -> new ArrayList<>()).add(resource);
            } else if (MENU_TYPES.contains(resource.getTypes())) {
                ResourceDto parent = menuMap.get(resource.getParentId());
                Long parentContainerId = parent == null ? null : (system ? parent.getSysId() : parent.getAppId());
                if (containerId.equals(parentContainerId)) {
                    parent.getChildren().add(resource);
                } else if (!"btn".equals(resource.getTypes())) {
                    menus.computeIfAbsent(containerId, key -> new ArrayList<>()).add(resource);
                }
            }
        }
        for (AppDto container : containers) {
            container.setMenus(menus.getOrDefault(container.getId(), new ArrayList<>()));
            container.setTools(tools.getOrDefault(container.getId(), new ArrayList<>()));
        }
    }
}
