package com.unione.cloud.base.api;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.base.dto.I18nDtos.BundleQueryRequest;
import com.unione.cloud.base.dto.I18nDtos.BundleResponse;
import com.unione.cloud.base.dto.I18nDtos.BundleSaveRequest;
import com.unione.cloud.base.dto.I18nDtos.MissingStatsResponse;
import com.unione.cloud.base.dto.I18nDtos.PublishRequest;
import com.unione.cloud.base.dto.I18nDtos.PublishedBundleResponse;
import com.unione.cloud.base.dto.I18nDtos.ReleaseResponse;
import com.unione.cloud.base.dto.I18nDtos.RollbackRequest;
import com.unione.cloud.base.dto.I18nDtos.TenantCustomizationRequest;
import com.unione.cloud.base.service.I18nService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.UserRoles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 界面语言包统一 CRUD 接口。 */
@RestController
@RequestMapping("/api/base/i18n/bundle")
@Tag(name = "基础：界面语言包")
public class BaseI18nBundleController {
    @Autowired
    private I18nService i18nService;

    @PostMapping("/find")
    @Action(title = "查询界面语言包", type = ActionType.Query)
    public Results<List<BundleResponse>> find(@RequestBody Params<BundleQueryRequest> params) {
        return i18nService.findBundles(params);
    }

    @PostMapping("/save")
    @Action(title = "保存全局界面语言包", type = ActionType.Save, roles = {UserRoles.SUPPERADMIN})
    public Results<Long> save(@Validated @RequestBody BundleSaveRequest request) {
        return Results.success(i18nService.saveBundle(request));
    }

    @PostMapping("/findByIds")
    @Operation(summary = "根据ID集合加载界面语言包")
    public Results<List<BundleResponse>> findByIds(@RequestBody Set<Long> ids) {
        return Results.success(i18nService.findBundlesByIds(ids));
    }

    @PostMapping("/detail")
    @Operation(summary = "加载界面语言包详情")
    public Results<BundleResponse> detail(@RequestBody Long id) {
        return Results.success(i18nService.bundleDetail(id));
    }

    @PostMapping("/delete")
    @Action(title = "删除全局界面语言包", type = ActionType.Delete, roles = {UserRoles.SUPPERADMIN})
    public Results<Integer> delete(@RequestBody Set<Long> ids) {
        int count = i18nService.deleteBundles(ids);
        return Results.build(count > 0, count);
    }

    @PostMapping("/missing")
    @Operation(summary = "统计语言条目缺失情况")
    public Results<MissingStatsResponse> missing(@RequestParam Long bundleId,
            @RequestParam String localeCode) {
        return Results.success(i18nService.missingStats(bundleId, localeCode));
    }

    @PostMapping("/history")
    @Operation(summary = "查询语言包发布版本")
    public Results<List<ReleaseResponse>> history(@RequestParam Long bundleId) {
        return Results.success(i18nService.listReleases(bundleId));
    }

    @PostMapping("/rollback")
    @Action(title = "回滚界面语言包", type = ActionType.Save,
            roles = {UserRoles.SUPPERADMIN, UserRoles.TENANTADMIN})
    public Results<PublishedBundleResponse> rollback(@Validated @RequestBody RollbackRequest request) {
        return Results.success(i18nService.rollback(request));
    }
    
    @PostMapping("/release")
    @Action(title = "发布界面语言包", type = ActionType.Save,
            roles = {UserRoles.SUPPERADMIN, UserRoles.TENANTADMIN})
    public Results<PublishedBundleResponse> release(@Validated @RequestBody PublishRequest request) {
        return Results.success(i18nService.release(request));
    }

    @PostMapping("/personalize")
    @Action(title = "创建租户个性化方言配置", type = ActionType.Save, roles = {UserRoles.TENANTADMIN})
    public Results<Long> personalize(@Validated @RequestBody TenantCustomizationRequest request) {
        return Results.success(i18nService.personalize(request));
    }

    @PostMapping("/restore")
    @Action(title = "还原租户个性化方言配置", type = ActionType.Save, roles = {UserRoles.TENANTADMIN})
    public Results<Integer> restore(@Validated @RequestBody TenantCustomizationRequest request) {
        return Results.success(i18nService.restoreTenantCustomization(request));
    }


}

