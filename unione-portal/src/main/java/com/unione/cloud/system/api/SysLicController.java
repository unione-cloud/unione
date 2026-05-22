package com.unione.cloud.system.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.service.TenantService;

@RestController
@RequestMapping("/api/lic")
@ConditionalOnMissingBean(name="licAction")
public class SysLicController{

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TenantService tenantService;

    @RequestMapping("/info")
    public Results<Map<String,Object>> info(){
        Map<String,Object> map = new HashMap<>();
        SysTenant tenant = tenantService.loadTenant(sessionService.getTenantId());
        map.put("tenant", tenant);
        return Results.success(map);
    }

}
