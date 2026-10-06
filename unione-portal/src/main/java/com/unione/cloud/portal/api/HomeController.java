package com.unione.cloud.portal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.portal.dto.AppDto;
import com.unione.cloud.portal.service.HomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RefreshScope
@RestController
@Tag(name = "统一门户: 门户首页")
@RequestMapping("/api/portal")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @PostMapping("/menus/{type}")
    @Action(title = "加载门户菜单", type = ActionType.Query)
    @Operation(summary = "门户菜单", description = "加载当前用户拥有权限的应用、系统及资源树，type取值范围[pc,app]")
    public Results<List<AppDto>> menus(@PathVariable("type") String type) {
        return Results.success(homeService.menus(type));
    }
}
