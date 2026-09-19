package com.unione.cloud.base.dto;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 方言模块接口 DTO。 */
public final class I18nDtos {
    private I18nDtos() {
    }

    @Data
    public static class BundleQueryRequest {
        private String bundleCode;
        private String bundleName;
        private Integer status;
    }

    @Data
    public static class BundleSaveRequest {
        private Long id;
        @NotBlank(message = "语言包编码不能为空")
        private String bundleCode;
        @NotBlank(message = "语言包名称不能为空")
        private String bundleName;
        @NotBlank(message = "适用端不能为空")
        private String clientScopes;
        @NotNull(message = "默认语言不能为空")
        private Integer defaultLocale;
        private String descs;
        private Integer status;
    }

    @Data
    public static class BundleResponse {
        private Long id;
        private Integer isGlobal;
        private Long globalBundleId;
        private String bundleCode;
        private String bundleName;
        private String clientScopes;
        private Integer defaultLocale;
        private Long currentReleaseId;
        private Integer currentVersionNo;
        private String descs;
        private Integer status;
    }

    @Data
    public static class TenantCustomizationRequest {
        @NotNull(message = "全局语言包ID不能为空")
        private Long globalBundleId;
    }

    @Data
    public static class EntrySaveRequest {
        private Long id;
        @NotNull(message = "语言包ID不能为空")
        private Long bundleId;
        @NotBlank(message = "语言代码不能为空")
        private String localeCode;
        @NotBlank(message = "文案键不能为空")
        private String entryKey;
        @NotNull(message = "文案值不能为空")
        private String entryValue;
        private String sourceType;
        private Integer status;
    }

    @Data
    public static class EntryQueryRequest {
        private Long bundleId;
        private String localeCode;
        private String entryKey;
        private Integer status;
    }

    @Data
    public static class EntryResponse {
        private Long id;
        private Long bundleId;
        private String localeCode;
        private String entryKey;
        private String entryValue;
        private String sourceType;
        private Integer revisionNo;
        private Integer status;
    }

    @Data
    public static class ReleaseResponse {
        private Long id;
        private Long bundleId;
        private Integer versionNo;
        private String versionDesc;
        private String checksum;
        private Long rollbackFromId;
        private Date released;
        private Long releasedBy;
    }

    @Data
    public static class PublishRequest {
        @NotNull(message = "语言包ID不能为空")
        private Long bundleId;
        private String versionDesc;
    }

    @Data
    public static class RollbackRequest {
        @NotNull(message = "语言包ID不能为空")
        private Long bundleId;
        @NotNull(message = "回滚版本ID不能为空")
        private Long releaseId;
        private String versionDesc;
    }

    @Data
    public static class PublishedBundleResponse {
        private Long bundleId;
        private Integer personalized;
        private Long globalBundleId;
        private String bundleCode;
        private Integer defaultLocale;
        private String clientScopes;
        private Long releaseId;
        private Integer versionNo;
        private String checksum;
        private String snapshotData;
    }

    @Data
    public static class PreferenceSaveRequest {
        private String interfaceLocale;
        private String contentLocale;
        private String dialectLocale;
    }

    @Data
    public static class PreferenceResponse {
        private String interfaceLocale;
        private String contentLocale;
        private String dialectLocale;
    }

    @Data
    public static class MissingStatsResponse {
        private Long bundleId;
        private String localeCode;
        private Integer referenceCount;
        private Integer translatedCount;
        private Integer missingCount;
    }
}
