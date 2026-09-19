package com.unione.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unione.cloud.base.model.BaseI18nBundle;
import com.unione.cloud.base.model.BaseI18nEntry;
import com.unione.cloud.base.model.BaseI18nPref;
import com.unione.cloud.base.model.BaseI18nRelease;
import com.unione.cloud.base.api.BaseI18nController;
import com.unione.cloud.base.api.BaseI18nBundleController;
import com.unione.cloud.base.api.BaseI18nEntryController;
import com.unione.cloud.base.dto.I18nDtos.BundleSaveRequest;
import com.unione.cloud.base.dto.I18nDtos.PublishRequest;
import com.unione.cloud.base.dto.I18nDtos.RollbackRequest;
import com.unione.cloud.base.dto.I18nDtos.TenantCustomizationRequest;
import com.unione.cloud.base.service.I18nService;
import com.unione.cloud.base.service.I18nRules;
import com.unione.cloud.base.service.I18nSnapshotCodec;

class I18nModuleStructureTests {

    @Test
    void modelsMapToExpectedTables() {
        assertTable(BaseI18nBundle.class, "base_i18n_bundle");
        assertTable(BaseI18nEntry.class, "base_i18n_entry");
        assertTable(BaseI18nRelease.class, "base_i18n_release");
        assertTable(BaseI18nPref.class, "base_i18n_pref");
    }

    @Test
    void migrationDefinesTablesAndUniquenessConstraints() throws Exception {
        Path migration = Path.of("src/main/resources/db/migration/mysql/"
                + "V1_0_3_20260919_01__add_i18n_bundle_tables.sql");
        String sql = Files.readString(migration, StandardCharsets.UTF_8);

        assertTrue(sql.contains("CREATE TABLE `base_i18n_bundle`"));
        assertTrue(sql.contains("CREATE TABLE `base_i18n_entry`"));
        assertTrue(sql.contains("CREATE TABLE `base_i18n_release`"));
        assertTrue(sql.contains("CREATE TABLE `base_i18n_pref`"));
        assertTrue(sql.contains("UK_I18N_ENTRY_BUNDLE_LOCALE_KEY"));
        assertTrue(sql.contains("UK_I18N_RELEASE_BUNDLE_VERSION"));
        assertTrue(sql.contains("UK_I18N_PREF_TENANT_USER"));

        Path defaultLocaleMigration = Path.of("src/main/resources/db/migration/mysql/"
                + "V1_0_3_20260919_02__change_i18n_default_locale_to_flag.sql");
        String defaultLocaleSql = Files.readString(defaultLocaleMigration, StandardCharsets.UTF_8);
        assertTrue(defaultLocaleSql.contains("MODIFY COLUMN `DEFAULT_LOCALE` int NOT NULL DEFAULT 0"));
    }

    @Test
    void apiUsesDedicatedDtosAndServiceLayer() throws Exception {
        assertEquals(BundleSaveRequest.class,
                BaseI18nBundleController.class.getMethod("save", BundleSaveRequest.class).getParameterTypes()[0]);
        assertTrue(I18nService.class.getMethod("saveBundle", BundleSaveRequest.class) != null);
        assertEquals(I18nService.class,
                BaseI18nBundleController.class.getDeclaredField("i18nService").getType());
        assertEquals(I18nService.class,
                BaseI18nEntryController.class.getDeclaredField("i18nService").getType());
        assertEquals(Integer.class, BaseI18nBundle.class.getDeclaredField("defaultLocale").getType());
        assertEquals(Integer.class, BundleSaveRequest.class.getDeclaredField("defaultLocale").getType());
    }

    @Test
    void bundleAndEntryExposeStandardCrudRoutes() throws Exception {
        assertEquals("/api/base/i18n/bundle",
                BaseI18nBundleController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/api/base/i18n/entry",
                BaseI18nEntryController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertCrudRoutes(BaseI18nBundleController.class);
        assertCrudRoutes(BaseI18nEntryController.class);
    }

    @Test
    void languageListExposesAllPublishedBundles() throws Exception {
        PostMapping mapping = BaseI18nController.class.getMethod("langList")
                .getAnnotation(PostMapping.class);
        assertEquals("/lang/list", mapping.value()[0]);
        assertTrue(I18nService.class.getMethod("listPublished") != null);
    }

    @Test
    void normalizesLocaleAndClientScopes() {
        assertEquals("zh-CN", I18nRules.normalizeLocale("ZH_cn"));
        assertEquals("all,app,pc", I18nRules.normalizeClientScopes("all,app,all,pc"));
        assertEquals("manual", I18nRules.normalizeSourceType(null));
        assertEquals("ai", I18nRules.normalizeSourceType("AI"));
    }

    @Test
    void snapshotIsDeterministicAndChecksumDetectsChanges() {
        BaseI18nEntry second = BaseI18nEntry.builder().localeCode("zh-CN")
                .entryKey("title").entryValue("标题\n第二行").build();
        BaseI18nEntry first = BaseI18nEntry.builder().localeCode("en-US")
                .entryKey("title").entryValue("A \"title\"").build();

        String forward = I18nSnapshotCodec.encode(List.of(first, second));
        String reverse = I18nSnapshotCodec.encode(List.of(second, first));
        String checksum = I18nSnapshotCodec.checksum(forward);

        assertEquals(forward, reverse);
        assertTrue(forward.contains("A \\\"title\\\""));
        assertTrue(forward.contains("标题\\n第二行"));
        assertEquals(64, checksum.length());
        assertTrue(I18nSnapshotCodec.verify(forward, checksum));
        assertTrue(!I18nSnapshotCodec.verify(forward + " ", checksum));
    }

    @Test
    void publishAndRollbackUseDtosAndServiceLayer() throws Exception {
        assertEquals(PublishRequest.class,
                BaseI18nBundleController.class.getMethod("release", PublishRequest.class).getParameterTypes()[0]);
        assertEquals(RollbackRequest.class,
                BaseI18nBundleController.class.getMethod("rollback", RollbackRequest.class).getParameterTypes()[0]);
        assertTrue(I18nService.class.getMethod("release", PublishRequest.class) != null);
        assertTrue(I18nService.class.getMethod("rollback", RollbackRequest.class) != null);
        assertTrue(I18nService.class.getMethod("getPublished", String.class, Long.class) != null);
    }

    @Test
    void tenantCustomizationMigrationAndActionsExist() throws Exception {
        Path migration = Path.of("src/main/resources/db/migration/mysql/"
                + "V1_0_3_20260919_01__add_i18n_bundle_tables.sql");
        String sql = Files.readString(migration, StandardCharsets.UTF_8);

        assertTrue(sql.contains("`IS_GLOBAL`"));
        assertTrue(sql.contains("`GLOBAL_BUNDLE_ID`"));
        assertTrue(sql.contains("UK_I18N_BUNDLE_TENANT_GLOBAL"));
        assertEquals(TenantCustomizationRequest.class,
                BaseI18nBundleController.class.getMethod("personalize", TenantCustomizationRequest.class)
                        .getParameterTypes()[0]);
        assertEquals(TenantCustomizationRequest.class,
                BaseI18nBundleController.class.getMethod("restore", TenantCustomizationRequest.class)
                        .getParameterTypes()[0]);
        assertTrue(I18nService.class.getMethod("personalize", TenantCustomizationRequest.class) != null);
        assertTrue(I18nService.class.getMethod("restoreTenantCustomization", TenantCustomizationRequest.class)
                != null);
    }

    private void assertTable(Class<?> type, String expectedName) {
        Table table = type.getAnnotation(Table.class);
        assertEquals(expectedName, table.name());
    }

    private void assertCrudRoutes(Class<?> controllerType) throws Exception {
        java.util.Set<String> routes = java.util.Arrays.stream(controllerType.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(java.util.Objects::nonNull)
                .map(mapping -> mapping.value()[0])
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(routes.containsAll(
                java.util.Set.of("/find", "/save", "/findByIds", "/detail", "/delete")));
    }
}
