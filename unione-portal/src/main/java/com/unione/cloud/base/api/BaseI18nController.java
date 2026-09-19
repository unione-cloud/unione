package com.unione.cloud.base.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.base.dto.I18nDtos.PreferenceResponse;
import com.unione.cloud.base.dto.I18nDtos.PreferenceSaveRequest;
import com.unione.cloud.base.dto.I18nDtos.PublishedBundleResponse;
import com.unione.cloud.base.service.I18nService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 方言及界面国际化接口。 */
@RestController
@RequestMapping("/api/base/i18n")
@Tag(name = "基础：方言与界面国际化")
public class BaseI18nController {
    @Autowired
    private I18nService i18nService;


    @PostMapping("/lang/list")
    @Operation(summary = "加载系统所有已发布语言包")
    public Results<List<PublishedBundleResponse>> langList() {
        return Results.success(i18nService.listPublished());
    }


    @PostMapping("/lang/data")
    @Operation(summary = "加载语言包数据")
    public Results<PublishedBundleResponse> langData(@RequestParam("bundleCode") String bundleCode,
            @RequestParam(value = "releaseId", required = false) Long releaseId) {
        return Results.success(i18nService.getPublished(bundleCode, releaseId));
    }

    @GetMapping("/pref/data")
    @Operation(summary = "加载用户语言偏好")
    public Results<PreferenceResponse> getPreference() {
        return Results.success(i18nService.getPreference());
    }

    @PostMapping("/pref/save")
    @Action(title = "保存用户语言偏好", type = ActionType.Save)
    public Results<PreferenceResponse> savePreference(@RequestBody PreferenceSaveRequest request) {
        return Results.success(i18nService.savePreference(request));
    }
}
