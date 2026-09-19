package com.unione.cloud.base.api;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.base.dto.I18nDtos.EntryQueryRequest;
import com.unione.cloud.base.dto.I18nDtos.EntryResponse;
import com.unione.cloud.base.dto.I18nDtos.EntrySaveRequest;
import com.unione.cloud.base.dto.I18nDtos.MissingStatsResponse;
import com.unione.cloud.base.service.I18nService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.UserRoles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 界面语言条目统一 CRUD 接口。 */
@RestController
@RequestMapping("/api/base/i18n/entry")
@Tag(name = "基础：界面语言条目")
public class BaseI18nEntryController {
    @Autowired
    private I18nService i18nService;

    @PostMapping("/find")
    @Action(title = "查询界面语言条目", type = ActionType.Query)
    public Results<List<EntryResponse>> find(@RequestBody Params<EntryQueryRequest> params) {
        return i18nService.findEntries(params);
    }

    @PostMapping("/save")
    @Action(title = "保存界面语言条目", type = ActionType.Save,
            roles = {UserRoles.SUPPERADMIN, UserRoles.TENANTADMIN})
    public Results<Long> save(@Validated @RequestBody EntrySaveRequest request) {
        return Results.success(i18nService.saveEntry(request));
    }

    @PostMapping("/findByIds")
    @Operation(summary = "根据ID集合加载界面语言条目")
    public Results<List<EntryResponse>> findByIds(@RequestBody Set<Long> ids) {
        return Results.success(i18nService.findEntriesByIds(ids));
    }

    @PostMapping("/detail")
    @Operation(summary = "加载界面语言条目详情")
    public Results<EntryResponse> detail(@RequestBody Long id) {
        return Results.success(i18nService.entryDetail(id));
    }

    @PostMapping("/delete")
    @Action(title = "删除全局界面语言条目", type = ActionType.Delete, roles = {UserRoles.SUPPERADMIN})
    public Results<Integer> delete(@RequestBody Set<Long> ids) {
        int count = i18nService.deleteEntries(ids);
        return Results.build(count > 0, count);
    }

}
