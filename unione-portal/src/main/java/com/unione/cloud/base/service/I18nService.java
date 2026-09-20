package com.unione.cloud.base.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unione.cloud.base.dto.I18nDtos.BundleResponse;
import com.unione.cloud.base.dto.I18nDtos.BundleSaveRequest;
import com.unione.cloud.base.dto.I18nDtos.BundleQueryRequest;
import com.unione.cloud.base.dto.I18nDtos.EntryResponse;
import com.unione.cloud.base.dto.I18nDtos.EntrySaveRequest;
import com.unione.cloud.base.dto.I18nDtos.EntryQueryRequest;
import com.unione.cloud.base.dto.I18nDtos.PreferenceResponse;
import com.unione.cloud.base.dto.I18nDtos.PreferenceSaveRequest;
import com.unione.cloud.base.dto.I18nDtos.PublishRequest;
import com.unione.cloud.base.dto.I18nDtos.PublishedBundleResponse;
import com.unione.cloud.base.dto.I18nDtos.PublishedBundleSummaryResponse;
import com.unione.cloud.base.dto.I18nDtos.ReleaseResponse;
import com.unione.cloud.base.dto.I18nDtos.RollbackRequest;
import com.unione.cloud.base.dto.I18nDtos.TenantCustomizationRequest;
import com.unione.cloud.base.dto.I18nDtos.MissingStatsResponse;
import com.unione.cloud.base.model.BaseI18nBundle;
import com.unione.cloud.base.model.BaseI18nEntry;
import com.unione.cloud.base.model.BaseI18nPref;
import com.unione.cloud.base.model.BaseI18nRelease;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;

/** 方言模块基础数据服务。 */
@Service
public class I18nService {
    @Autowired
    private DataBaseDao dataBaseDao;
    @Autowired
    private SessionService sessionService;

    public List<BundleResponse> listBundles(String bundleCode) {
        BaseI18nBundle condition = BaseI18nBundle.builder().bundleCode(bundleCode).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)
                .sort(Sort.build("created", "desc"))).stream().map(this::toBundleResponse).toList();
    }

    public Results<List<BundleResponse>> findBundles(Params<BundleQueryRequest> request) {
        AssertUtil.service().notNull(request, "请求参数不能为空");
        AssertUtil.service().notNull(request.getBody(), "请求参数body不能为空");
        BundleQueryRequest query = request.getBody();
        BaseI18nBundle condition = BaseI18nBundle.builder().bundleCode(query.getBundleCode())
                .bundleName(query.getBundleName()).status(query.getStatus()).build();
        condition.setTenantId(sessionService.getTenantId());
        Results<List<BaseI18nBundle>> rows = dataBaseDao.findPages(
                SqlBuilder.build(copyPage(request, condition)).dataPermis(PermisRule.ALL));
        return mapPage(rows, rows.getBody().stream().map(this::toBundleResponse).toList());
    }

    public List<BundleResponse> findBundlesByIds(Set<Long> ids) {
        AssertUtil.service().isTrue(ids != null && !ids.isEmpty(), "参数ids不能为空");
        return ids.stream().map(this::loadOwnedBundle).map(this::toBundleResponse).toList();
    }

    public BundleResponse bundleDetail(Long id) {
        return toBundleResponse(loadOwnedBundle(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveBundle(BundleSaveRequest request) {
        assertPlatformAdmin();
        BaseI18nBundle entity = request.getId() == null ? new BaseI18nBundle() : loadOwnedBundle(request.getId());
        AssertUtil.service().isTrue(entity.getId() == null || Objects.equals(entity.getIsGlobal(), 1),
                "租户个性化语言包不能通过通用保存接口修改");
        assertBundleCodeUnique(request.getId(), request.getBundleCode().trim());
        entity.setBundleCode(request.getBundleCode().trim());
        entity.setBundleName(request.getBundleName().trim());
        entity.setClientScopes(I18nRules.normalizeClientScopes(request.getClientScopes()));
        AssertUtil.service().isTrue(Objects.equals(request.getDefaultLocale(), 0)
                || Objects.equals(request.getDefaultLocale(), 1),
                "默认语言只能为0或1");
        entity.setDefaultLocale(request.getDefaultLocale());
        if (Objects.equals(entity.getDefaultLocale(), 0)) {
            assertAnotherDefaultBundleExists(entity.getId());
        }
        entity.setDescs(request.getDescs());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        I18nRules.validateStatus(entity.getStatus());
        if (entity.getId() == null) {
            entity.setIsGlobal(1);
            fillOwner(entity);
            dataBaseDao.insert(entity);
        } else {
            dataBaseDao.updateById(SqlBuilder.build(entity).field(
                    "bundleCode", "bundleName", "clientScopes", "defaultLocale", "descs", "status"));
        }
        if (Objects.equals(entity.getDefaultLocale(), 1)) {
            clearOtherDefaultBundles(entity);
        }
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteBundles(Set<Long> ids) {
        assertPlatformAdmin();
        AssertUtil.service().isTrue(ids != null && !ids.isEmpty(), "参数ids不能为空");
        ids.forEach(id -> {
            BaseI18nBundle bundle = loadOwnedBundle(id);
            AssertUtil.service().isTrue(!Objects.equals(bundle.getDefaultLocale(), 1),
                    "默认语言包不能删除，请先将其他语言包设为默认");
            AssertUtil.service().isTrue(findEntries(id, null).isEmpty(), "语言包存在条目，不能删除");
            AssertUtil.service().isTrue(findReleases(id).isEmpty(), "语言包存在发布版本，不能删除");
        });
        return dataBaseDao.deleteById(SqlBuilder.build(BaseI18nBundle.class, ids));
    }

    public List<EntryResponse> listEntries(Long bundleId, String localeCode) {
        loadOwnedBundle(bundleId);
        BaseI18nEntry condition = BaseI18nEntry.builder().bundleId(bundleId).localeCode(localeCode).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)
                .sort(Sort.build("entryKey", "asc"))).stream().map(this::toEntryResponse).toList();
    }

    public Results<List<EntryResponse>> findEntries(Params<EntryQueryRequest> request) {
        AssertUtil.service().notNull(request, "请求参数不能为空");
        AssertUtil.service().notNull(request.getBody(), "请求参数body不能为空");
        EntryQueryRequest query = request.getBody();
        BaseI18nEntry condition = BaseI18nEntry.builder().bundleId(query.getBundleId())
                .localeCode(query.getLocaleCode()).entryKey(query.getEntryKey()).status(query.getStatus()).build();
        condition.setTenantId(sessionService.getTenantId());
        Results<List<BaseI18nEntry>> rows = dataBaseDao.findPages(
                SqlBuilder.build(copyPage(request, condition)).dataPermis(PermisRule.ALL));
        return mapPage(rows, rows.getBody().stream().map(this::toEntryResponse).toList());
    }

    public List<EntryResponse> findEntriesByIds(Set<Long> ids) {
        AssertUtil.service().isTrue(ids != null && !ids.isEmpty(), "参数ids不能为空");
        return ids.stream().map(this::loadOwnedEntry).map(this::toEntryResponse).toList();
    }

    public EntryResponse entryDetail(Long id) {
        return toEntryResponse(loadOwnedEntry(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveEntry(EntrySaveRequest request) {
        BaseI18nBundle bundle = loadAccessibleBundle(request.getBundleId());
        assertCanManage(bundle);
        if (Objects.equals(bundle.getIsGlobal(), 0) && request.getId() == null) {
            assertGlobalEntryExists(bundle.getGlobalBundleId(), request.getLocaleCode(), request.getEntryKey());
        }
        BaseI18nEntry entity = request.getId() == null ? new BaseI18nEntry() : loadOwnedEntry(request.getId());
        if (entity.getId() != null) {
            AssertUtil.service().isTrue(Objects.equals(entity.getBundleId(), request.getBundleId()),
                    "不能变更条目所属语言包");
        }
        entity.setBundleId(request.getBundleId());
        String localeCode = I18nRules.normalizeLocale(request.getLocaleCode());
        assertEntryUnique(request.getId(), request.getBundleId(), localeCode, request.getEntryKey().trim());
        entity.setLocaleCode(localeCode);
        entity.setEntryKey(request.getEntryKey().trim());
        entity.setEntryValue(request.getEntryValue());
        entity.setSourceType(I18nRules.normalizeSourceType(request.getSourceType()));
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        I18nRules.validateStatus(entity.getStatus());
        if (entity.getId() == null) {
            entity.setRevisionNo(1);
            fillOwner(entity);
            dataBaseDao.insert(entity);
        } else {
            entity.setRevisionNo(entity.getRevisionNo() + 1);
            dataBaseDao.updateById(SqlBuilder.build(entity).field(
                    "localeCode", "entryKey", "entryValue", "sourceType", "revisionNo", "status"));
        }
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteEntries(Set<Long> ids) {
        assertPlatformAdmin();
        AssertUtil.service().isTrue(ids != null && !ids.isEmpty(), "参数ids不能为空");
        ids.forEach(this::loadOwnedEntry);
        return dataBaseDao.deleteById(SqlBuilder.build(BaseI18nEntry.class, ids));
    }

    public List<ReleaseResponse> listReleases(Long bundleId) {
        loadOwnedBundle(bundleId);
        return findReleases(bundleId).stream().map(this::toReleaseResponse).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PublishedBundleResponse release(PublishRequest request) {
        BaseI18nBundle bundle = loadAccessibleBundle(request.getBundleId());
        assertCanManage(bundle);
        AssertUtil.service().isTrue(Objects.equals(bundle.getStatus(), 1), "停用的语言包不能发布");
        List<BaseI18nEntry> enabledEntries = findEntries(bundle.getId(), null).stream()
                .filter(entry -> Objects.equals(entry.getStatus(), 1))
                .toList();
        String snapshotData;
        if (Objects.equals(bundle.getIsGlobal(), 1)) {
            AssertUtil.service().isTrue(!enabledEntries.isEmpty(), "语言包没有可发布的启用条目");
            snapshotData = I18nSnapshotCodec.encode(enabledEntries);
        } else {
            BaseI18nBundle globalBundle = loadGlobalBundle(bundle.getGlobalBundleId());
            BaseI18nRelease globalRelease = loadCurrentRelease(globalBundle);
            snapshotData = I18nSnapshotCodec.merge(globalRelease.getSnapshotData(), enabledEntries);
        }
        return createRelease(bundle, request.getVersionDesc(), snapshotData,
                I18nSnapshotCodec.checksum(snapshotData), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublishedBundleResponse rollback(RollbackRequest request) {
        BaseI18nBundle bundle = loadAccessibleBundle(request.getBundleId());
        assertCanManage(bundle);
        BaseI18nRelease source = loadRelease(request.getReleaseId());
        AssertUtil.service().isTrue(Objects.equals(source.getBundleId(), bundle.getId()),
                "回滚版本不属于当前语言包");
        AssertUtil.service().isTrue(I18nSnapshotCodec.verify(source.getSnapshotData(), source.getChecksum()),
                "回滚版本快照校验失败");
        String versionDesc = request.getVersionDesc() == null || request.getVersionDesc().isBlank()
                ? "回滚至版本 " + source.getVersionNo() : request.getVersionDesc().trim();
        return createRelease(bundle, versionDesc, source.getSnapshotData(), source.getChecksum(), source.getId());
    }

    public PublishedBundleResponse getPublished(String code, String local) {
        BaseI18nBundle globalBundle = resolvePublishedBundle(code, local);
        BaseI18nBundle tenantBundle = findTenantBundle(globalBundle.getId());
        BaseI18nBundle selected = tenantBundle != null && tenantBundle.getCurrentReleaseId() != null
                ? tenantBundle : globalBundle;
        return toPublishedResponse(selected, loadCurrentRelease(selected));
    }

    public List<PublishedBundleSummaryResponse> listPublished() {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1).status(1).build();
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)
                .sort(Sort.build("bundleCode", "asc"))).stream()
                .filter(bundle -> bundle.getCurrentReleaseId() != null)
                .map(global -> {
                    BaseI18nBundle tenant = findTenantBundle(global.getId());
                    BaseI18nBundle selected = tenant != null && tenant.getCurrentReleaseId() != null
                            ? tenant : global;
                    return toPublishedSummaryResponse(selected);
                }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long personalize(TenantCustomizationRequest request) {
        assertTenantAdmin();
        BaseI18nBundle global = loadGlobalBundle(request.getGlobalBundleId());
        AssertUtil.service().notNull(global.getCurrentReleaseId(), "全局语言包尚未发布，不能个性化设置");
        BaseI18nBundle existing = findTenantBundle(global.getId());
        if (existing != null) {
            return existing.getId();
        }
        BaseI18nBundle tenant = new BaseI18nBundle();
        fillOwner(tenant);
        tenant.setIsGlobal(0);
        tenant.setGlobalBundleId(global.getId());
        tenant.setBundleCode(global.getBundleCode());
        tenant.setBundleName(global.getBundleName());
        tenant.setClientScopes(global.getClientScopes());
        tenant.setDefaultLocale(global.getDefaultLocale());
        tenant.setDescs("租户个性化：" + global.getBundleName());
        tenant.setStatus(1);
        dataBaseDao.insert(tenant);
        return tenant.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int restoreTenantCustomization(TenantCustomizationRequest request) {
        assertTenantAdmin();
        BaseI18nBundle tenant = findTenantBundle(request.getGlobalBundleId());
        if (tenant == null) {
            return 0;
        }
        List<BaseI18nEntry> overrides = findEntries(tenant.getId(), null);
        if (!overrides.isEmpty()) {
            dataBaseDao.deleteById(SqlBuilder.build(BaseI18nEntry.class,
                    overrides.stream().map(BaseI18nEntry::getId).collect(Collectors.toSet())));
        }
        tenant.setCurrentReleaseId(null);
        dataBaseDao.updateById(SqlBuilder.build(tenant).field("currentReleaseId"));
        return 1;
    }

    public MissingStatsResponse missingStats(Long bundleId, String localeCode) {
        loadOwnedBundle(bundleId);
        BaseI18nBundle defaultBundle = findDefaultBundle();
        String normalizedLocale = I18nRules.normalizeLocale(localeCode);
        Set<String> referenceKeys = findEntries(defaultBundle.getId(), defaultBundle.getBundleCode()).stream()
                .filter(entry -> Objects.equals(entry.getStatus(), 1))
                .map(BaseI18nEntry::getEntryKey).collect(Collectors.toSet());
        Set<String> translatedKeys = findEntries(bundleId, normalizedLocale).stream()
                .filter(entry -> Objects.equals(entry.getStatus(), 1))
                .map(BaseI18nEntry::getEntryKey).collect(Collectors.toSet());
        translatedKeys.retainAll(referenceKeys);
        MissingStatsResponse response = new MissingStatsResponse();
        response.setBundleId(bundleId);
        response.setLocaleCode(normalizedLocale);
        response.setReferenceCount(referenceKeys.size());
        response.setTranslatedCount(translatedKeys.size());
        response.setMissingCount(referenceKeys.size() - translatedKeys.size());
        return response;
    }

    public PreferenceResponse getPreference() {
        BaseI18nPref entity = findPreference();
        return entity == null ? new PreferenceResponse() : toPreferenceResponse(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public PreferenceResponse savePreference(PreferenceSaveRequest request) {
        BaseI18nPref condition = preferenceCondition();
        BaseI18nPref entity = dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        if (entity == null) {
            entity = new BaseI18nPref();
            fillOwner(entity);
            copyPreference(request, entity, true);
            dataBaseDao.insert(entity);
        } else {
            copyPreference(request, entity, false);
            dataBaseDao.updateById(SqlBuilder.build(entity)
                    .field("interfaceLocale", "contentLocale", "dialectLocale"));
        }
        return toPreferenceResponse(entity);
    }

    private BaseI18nPref preferenceCondition() {
        BaseI18nPref condition = new BaseI18nPref();
        condition.setTenantId(sessionService.getTenantId());
        condition.setUserId(sessionService.getUserId());
        return condition;
    }

    private BaseI18nPref findPreference() {
        return dataBaseDao.findOne(SqlBuilder.build(preferenceCondition()).dataPermis(PermisRule.ALL));
    }

    /**
     * 解析应用端实际使用的语言包：显式编码优先，其次为用户界面语言偏好、浏览器语言和系统默认语言。
     */
    private BaseI18nBundle resolvePublishedBundle(String code, String local) {
        if (code != null && !code.isBlank()) {
            return findGlobalBundle(I18nRules.normalizeLocale(code));
        }

        BaseI18nPref preference = findPreference();
        if (preference != null && preference.getInterfaceLocale() != null
                && !preference.getInterfaceLocale().isBlank()) {
            return findGlobalBundle(I18nRules.normalizeLocale(preference.getInterfaceLocale()));
        }

        BaseI18nBundle localBundle = findPublishedGlobalBundle(local);
        return localBundle == null ? findDefaultBundle() : localBundle;
    }

    private BaseI18nBundle findPublishedGlobalBundle(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1)
                .bundleCode(I18nRules.normalizeLocale(code)).status(1).build();
        BaseI18nBundle bundle = dataBaseDao.findOne(
                SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        return bundle != null && bundle.getCurrentReleaseId() != null ? bundle : null;
    }

    private <Q, E> Params<E> copyPage(Params<Q> source, E body) {
        Params<E> target = Params.build(body);
        target.setPage(source.getPage());
        target.setPageSize(source.getPageSize());
        target.setNeedCount(source.isNeedCount());
        target.setTotal(source.getTotal());
        target.setKeywords(source.getKeywords());
        target.setSorts(source.getSorts());
        return target;
    }

    private <S, T> Results<List<T>> mapPage(Results<List<S>> source, List<T> body) {
        Results<List<T>> target = Results.success(body);
        target.setPage(source.getPage());
        target.setPageSize(source.getPageSize());
        target.setTotal(source.getTotal());
        return target;
    }

    private void copyPreference(PreferenceSaveRequest request, BaseI18nPref entity, boolean create) {
        if (create || request.getInterfaceLocale() != null) {
            entity.setInterfaceLocale(normalizeNullableLocale(request.getInterfaceLocale()));
        }
        if (create || request.getContentLocale() != null) {
            entity.setContentLocale(normalizeNullableLocale(request.getContentLocale()));
        }
        if (create || request.getDialectLocale() != null) {
            entity.setDialectLocale(normalizeNullableLocale(request.getDialectLocale()));
        }
    }

    private String normalizeNullableLocale(String localeCode) {
        return localeCode == null || localeCode.isBlank() ? null : I18nRules.normalizeLocale(localeCode);
    }

    private void assertBundleCodeUnique(Long currentId, String bundleCode) {
        BaseI18nBundle condition = BaseI18nBundle.builder().bundleCode(bundleCode).build();
        condition.setTenantId(sessionService.getTenantId());
        BaseI18nBundle existing = dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        AssertUtil.service().isTrue(existing == null || Objects.equals(existing.getId(), currentId),
                "语言包编码已存在");
    }

    private void clearOtherDefaultBundles(BaseI18nBundle current) {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1).defaultLocale(1).build();
        condition.setTenantId(sessionService.getTenantId());
        dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)).stream()
                .filter(bundle -> !Objects.equals(bundle.getId(), current.getId()))
                .forEach(bundle -> {
                    bundle.setDefaultLocale(0);
                    dataBaseDao.updateById(SqlBuilder.build(bundle).field("defaultLocale"));
                });
    }

    private void assertAnotherDefaultBundleExists(Long currentId) {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1).defaultLocale(1).build();
        condition.setTenantId(sessionService.getTenantId());
        boolean exists = dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)).stream()
                .anyMatch(bundle -> !Objects.equals(bundle.getId(), currentId));
        AssertUtil.service().isTrue(exists, "必须保留一个默认语言包");
    }

    private BaseI18nBundle findDefaultBundle() {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1).defaultLocale(1).build();
        condition.setTenantId(sessionService.getTenantId());
        BaseI18nBundle defaultBundle = dataBaseDao.findOne(
                SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(defaultBundle, "未配置默认语言包");
        return defaultBundle;
    }

    private void assertEntryUnique(Long currentId, Long bundleId, String localeCode, String entryKey) {
        BaseI18nEntry condition = BaseI18nEntry.builder().bundleId(bundleId)
                .localeCode(localeCode).entryKey(entryKey).build();
        BaseI18nEntry existing = dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        AssertUtil.service().isTrue(existing == null || Objects.equals(existing.getId(), currentId),
                "当前语言下文案键已存在");
    }

    private List<BaseI18nEntry> findEntries(Long bundleId, String localeCode) {
        BaseI18nEntry condition = BaseI18nEntry.builder().bundleId(bundleId).localeCode(localeCode).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
    }

    private List<BaseI18nRelease> findReleases(Long bundleId) {
        BaseI18nRelease condition = BaseI18nRelease.builder().bundleId(bundleId).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)
                .sort(Sort.build("versionNo", "desc")));
    }

    private PublishedBundleResponse createRelease(BaseI18nBundle bundle, String versionDesc,
            String snapshotData, String checksum, Long rollbackFromId) {
        int nextVersion = findReleases(bundle.getId()).stream()
                .map(BaseI18nRelease::getVersionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        BaseI18nRelease release = new BaseI18nRelease();
        fillOwner(release);
        release.setBundleId(bundle.getId());
        release.setVersionNo(nextVersion);
        release.setVersionDesc(versionDesc == null ? null : versionDesc.trim());
        release.setSnapshotData(snapshotData);
        release.setChecksum(checksum);
        release.setRollbackFromId(rollbackFromId);
        release.setReleased(new Date());
        release.setReleasedBy(sessionService.getUserId());
        dataBaseDao.insert(release);

        bundle.setCurrentReleaseId(release.getId());
        dataBaseDao.updateById(SqlBuilder.build(bundle).field("currentReleaseId"));
        return toPublishedResponse(bundle, release);
    }

    private void fillOwner(com.unione.cloud.core.model.Pojo entity) {
        entity.setTenantId(sessionService.getTenantId());
        entity.setOrgId(sessionService.getOrgId());
        entity.setUserId(sessionService.getUserId());
    }

    private void assertPlatformAdmin() {
        AssertUtil.service().isTrue(sessionService.isAdmin()
                || sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN), "仅平台管理员可管理全局方言");
    }

    private void assertTenantAdmin() {
        AssertUtil.service().isTrue(sessionService.isAdmin()
                || sessionService.getUserRoles().contains(UserRoles.TENANTADMIN), "仅租户管理员可管理个性化方言");
    }

    private void assertCanManage(BaseI18nBundle bundle) {
        if (Objects.equals(bundle.getIsGlobal(), 1)) {
            assertPlatformAdmin();
        } else {
            assertTenantAdmin();
            AssertUtil.service().isTrue(Objects.equals(bundle.getTenantId(), sessionService.getTenantId()),
                    "语言包不存在或无权访问");
        }
    }

    private void assertGlobalEntryExists(Long globalBundleId, String localeCode, String entryKey) {
        BaseI18nEntry condition = BaseI18nEntry.builder().bundleId(globalBundleId)
                .localeCode(I18nRules.normalizeLocale(localeCode)).entryKey(entryKey.trim()).build();
        AssertUtil.service().notNull(
                dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)),
                "租户管理员只能个性化全局语言包中已有的方言条目");
    }

    private BaseI18nBundle findGlobalBundle(String bundleCode) {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(1)
                .bundleCode(bundleCode).status(1).build();
        BaseI18nBundle bundle = dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(bundle, "全局语言包不存在");
        return bundle;
    }

    private BaseI18nBundle loadGlobalBundle(Long id) {
        BaseI18nBundle bundle = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nBundle.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(bundle, "全局语言包不存在");
        AssertUtil.service().isTrue(Objects.equals(bundle.getIsGlobal(), 1), "指定语言包不是全局语言包");
        return bundle;
    }

    private BaseI18nBundle findTenantBundle(Long globalBundleId) {
        BaseI18nBundle condition = BaseI18nBundle.builder().isGlobal(0)
                .globalBundleId(globalBundleId).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
    }

    private BaseI18nBundle loadAccessibleBundle(Long id) {
        BaseI18nBundle bundle = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nBundle.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(bundle, "语言包不存在");
        AssertUtil.service().isTrue(Objects.equals(bundle.getIsGlobal(), 1)
                || Objects.equals(bundle.getTenantId(), sessionService.getTenantId()), "语言包不存在或无权访问");
        return bundle;
    }

    private BaseI18nRelease loadCurrentRelease(BaseI18nBundle bundle) {
        AssertUtil.service().notNull(bundle.getCurrentReleaseId(), "语言包尚未发布");
        BaseI18nRelease release = loadRelease(bundle.getCurrentReleaseId());
        AssertUtil.service().isTrue(Objects.equals(release.getBundleId(), bundle.getId()),
                "发布版本不属于当前语言包");
        AssertUtil.service().isTrue(I18nSnapshotCodec.verify(release.getSnapshotData(), release.getChecksum()),
                "发布版本快照校验失败");
        return release;
    }

    private BaseI18nRelease loadRelease(Long id) {
        AssertUtil.service().notNull(id, "发布版本ID不能为空");
        BaseI18nRelease release = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nRelease.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(release, "发布版本不存在");
        return release;
    }

    private BaseI18nBundle loadOwnedBundle(Long id) {
        AssertUtil.service().notNull(id, "语言包ID不能为空");
        BaseI18nBundle entity = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nBundle.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(entity, "语言包不存在");
        AssertUtil.service().isTrue(Objects.equals(entity.getTenantId(), sessionService.getTenantId()),
                "语言包不存在或无权访问");
        return entity;
    }

    private BaseI18nEntry loadOwnedEntry(Long id) {
        BaseI18nEntry entity = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nEntry.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(entity, "语言条目不存在");
        AssertUtil.service().isTrue(Objects.equals(entity.getTenantId(), sessionService.getTenantId()),
                "语言条目不存在或无权访问");
        return entity;
    }

    private BaseI18nRelease loadOwnedRelease(Long id) {
        AssertUtil.service().notNull(id, "发布版本ID不能为空");
        BaseI18nRelease entity = dataBaseDao.findById(
                SqlBuilder.build(BaseI18nRelease.class, id).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(entity, "发布版本不存在");
        AssertUtil.service().isTrue(Objects.equals(entity.getTenantId(), sessionService.getTenantId()),
                "发布版本不存在或无权访问");
        return entity;
    }

    private BundleResponse toBundleResponse(BaseI18nBundle entity) {
        BundleResponse response = new BundleResponse();
        response.setId(entity.getId());
        response.setIsGlobal(entity.getIsGlobal());
        response.setGlobalBundleId(entity.getGlobalBundleId());
        response.setBundleCode(entity.getBundleCode());
        response.setBundleName(entity.getBundleName());
        response.setClientScopes(entity.getClientScopes());
        response.setDefaultLocale(entity.getDefaultLocale());
        response.setCurrentReleaseId(entity.getCurrentReleaseId());
        response.setCurrentVersionNo(entity.getCurrentReleaseId() == null
                ? 0 : loadRelease(entity.getCurrentReleaseId()).getVersionNo());
        response.setDescs(entity.getDescs());
        response.setStatus(entity.getStatus());
        return response;
    }

    private EntryResponse toEntryResponse(BaseI18nEntry entity) {
        EntryResponse response = new EntryResponse();
        response.setId(entity.getId());
        response.setBundleId(entity.getBundleId());
        response.setLocaleCode(entity.getLocaleCode());
        response.setEntryKey(entity.getEntryKey());
        response.setEntryValue(entity.getEntryValue());
        response.setSourceType(entity.getSourceType());
        response.setRevisionNo(entity.getRevisionNo());
        response.setStatus(entity.getStatus());
        return response;
    }

    private ReleaseResponse toReleaseResponse(BaseI18nRelease entity) {
        ReleaseResponse response = new ReleaseResponse();
        response.setId(entity.getId());
        response.setBundleId(entity.getBundleId());
        response.setVersionNo(entity.getVersionNo());
        response.setVersionDesc(entity.getVersionDesc());
        response.setChecksum(entity.getChecksum());
        response.setRollbackFromId(entity.getRollbackFromId());
        response.setReleased(entity.getReleased());
        response.setReleasedBy(entity.getReleasedBy());
        return response;
    }

    private PublishedBundleResponse toPublishedResponse(BaseI18nBundle bundle, BaseI18nRelease release) {
        PublishedBundleResponse response = new PublishedBundleResponse();
        response.setBundleId(bundle.getId());
        response.setPersonalized(Objects.equals(bundle.getIsGlobal(), 0) ? 1 : 0);
        response.setGlobalBundleId(Objects.equals(bundle.getIsGlobal(), 0)
                ? bundle.getGlobalBundleId() : bundle.getId());
        response.setBundleCode(bundle.getBundleCode());
        response.setDefaultLocale(bundle.getDefaultLocale());
        response.setClientScopes(bundle.getClientScopes());
        response.setReleaseId(release.getId());
        response.setVersionNo(release.getVersionNo());
        response.setChecksum(release.getChecksum());
        response.setSnapshotData(release.getSnapshotData());
        return response;
    }

    private PublishedBundleSummaryResponse toPublishedSummaryResponse(BaseI18nBundle bundle) {
        PublishedBundleSummaryResponse response = new PublishedBundleSummaryResponse();
        response.setBundleId(bundle.getId());
        response.setPersonalized(Objects.equals(bundle.getIsGlobal(), 0) ? 1 : 0);
        response.setGlobalBundleId(Objects.equals(bundle.getIsGlobal(), 0)
                ? bundle.getGlobalBundleId() : bundle.getId());
        response.setBundleCode(bundle.getBundleCode());
        response.setBundleName(bundle.getBundleName());
        response.setDefaultLocale(bundle.getDefaultLocale());
        response.setClientScopes(bundle.getClientScopes());
        response.setReleaseId(bundle.getCurrentReleaseId());
        return response;
    }

    private PreferenceResponse toPreferenceResponse(BaseI18nPref entity) {
        PreferenceResponse response = new PreferenceResponse();
        response.setInterfaceLocale(entity.getInterfaceLocale());
        response.setContentLocale(entity.getContentLocale());
        response.setDialectLocale(entity.getDialectLocale());
        return response;
    }
}
