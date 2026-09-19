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
        assertEquals("author,user,admin", I18nRules.normalizeClientScopes("author,user,author,admin"));
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
    void deliveryDocumentationExistsAndDeclaresModuleBoundary() throws Exception {
        Path documentation = Path.of("../doc/方言与界面国际化模块使用说明.md");
        String content = Files.readString(documentation, StandardCharsets.UTF_8);

        assertTrue(content.contains("/api/base/i18n"));
        assertTrue(content.contains("不可变发布版本"));
        assertTrue(content.contains("内容语言目录、作品语言配置、内容回退"));
        assertTrue(content.contains("interfaceLocale"));
        assertTrue(content.contains("contentLocale"));
        assertTrue(content.contains("dialectLocale"));
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
