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
import com.unione.cloud.base.dto.I18nDtos.ReleaseResponse;
import com.unione.cloud.base.dto.I18nDtos.RollbackRequest;
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
        BaseI18nBundle entity = request.getId() == null ? new BaseI18nBundle() : loadOwnedBundle(request.getId());
        assertBundleCodeUnique(request.getId(), request.getBundleCode().trim());
        entity.setBundleCode(request.getBundleCode().trim());
        entity.setBundleName(request.getBundleName().trim());
        entity.setClientScopes(I18nRules.normalizeClientScopes(request.getClientScopes()));
        entity.setDefaultLocale(I18nRules.normalizeLocale(request.getDefaultLocale()));
        entity.setDescs(request.getDescs());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        I18nRules.validateStatus(entity.getStatus());
        if (entity.getId() == null) {
            fillOwner(entity);
            dataBaseDao.insert(entity);
        } else {
            dataBaseDao.updateById(SqlBuilder.build(entity).field(
                    "bundleCode", "bundleName", "clientScopes", "defaultLocale", "descs", "status"));
        }
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteBundles(Set<Long> ids) {
        AssertUtil.service().isTrue(ids != null && !ids.isEmpty(), "参数ids不能为空");
        ids.forEach(id -> {
            loadOwnedBundle(id);
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
        loadOwnedBundle(request.getBundleId());
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
        BaseI18nBundle bundle = loadOwnedBundle(request.getBundleId());
        AssertUtil.service().isTrue(Objects.equals(bundle.getStatus(), 1), "停用的语言包不能发布");
        List<BaseI18nEntry> enabledEntries = findEntries(bundle.getId(), null).stream()
                .filter(entry -> Objects.equals(entry.getStatus(), 1))
                .toList();
        AssertUtil.service().isTrue(!enabledEntries.isEmpty(), "语言包没有可发布的启用条目");
        String snapshotData = I18nSnapshotCodec.encode(enabledEntries);
        return createRelease(bundle, request.getVersionDesc(), snapshotData,
                I18nSnapshotCodec.checksum(snapshotData), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublishedBundleResponse rollback(RollbackRequest request) {
        BaseI18nBundle bundle = loadOwnedBundle(request.getBundleId());
        BaseI18nRelease source = loadOwnedRelease(request.getReleaseId());
        AssertUtil.service().isTrue(Objects.equals(source.getBundleId(), bundle.getId()),
                "回滚版本不属于当前语言包");
        AssertUtil.service().isTrue(I18nSnapshotCodec.verify(source.getSnapshotData(), source.getChecksum()),
                "回滚版本快照校验失败");
        String versionDesc = request.getVersionDesc() == null || request.getVersionDesc().isBlank()
                ? "回滚至版本 " + source.getVersionNo() : request.getVersionDesc().trim();
        return createRelease(bundle, versionDesc, source.getSnapshotData(), source.getChecksum(), source.getId());
    }

    public PublishedBundleResponse getPublished(String bundleCode, Long releaseId) {
        AssertUtil.service().isTrue(bundleCode != null && !bundleCode.isBlank(), "语言包编码不能为空");
        BaseI18nBundle condition = BaseI18nBundle.builder().bundleCode(bundleCode.trim()).build();
        condition.setTenantId(sessionService.getTenantId());
        BaseI18nBundle bundle = dataBaseDao.findOne(
                SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
        AssertUtil.service().notNull(bundle, "语言包不存在");
        Long selectedReleaseId = releaseId == null ? bundle.getCurrentReleaseId() : releaseId;
        AssertUtil.service().notNull(selectedReleaseId, "语言包尚未发布");
        BaseI18nRelease release = loadOwnedRelease(selectedReleaseId);
        AssertUtil.service().isTrue(Objects.equals(release.getBundleId(), bundle.getId()),
                "发布版本不属于当前语言包");
        AssertUtil.service().isTrue(I18nSnapshotCodec.verify(release.getSnapshotData(), release.getChecksum()),
                "发布版本快照校验失败");
        return toPublishedResponse(bundle, release);
    }

    public List<PublishedBundleResponse> listPublished() {
        BaseI18nBundle condition = BaseI18nBundle.builder().status(1).build();
        condition.setTenantId(sessionService.getTenantId());
        return dataBaseDao.findList(SqlBuilder.build(condition).dataPermis(PermisRule.ALL)
                .sort(Sort.build("bundleCode", "asc"))).stream()
                .filter(bundle -> bundle.getCurrentReleaseId() != null)
                .map(bundle -> {
                    BaseI18nRelease release = loadOwnedRelease(bundle.getCurrentReleaseId());
                    AssertUtil.service().isTrue(Objects.equals(release.getBundleId(), bundle.getId()),
                            "发布版本不属于当前语言包");
                    AssertUtil.service().isTrue(
                            I18nSnapshotCodec.verify(release.getSnapshotData(), release.getChecksum()),
                            "发布版本快照校验失败");
                    return toPublishedResponse(bundle, release);
                }).toList();
    }

    public MissingStatsResponse missingStats(Long bundleId, String localeCode) {
        BaseI18nBundle bundle = loadOwnedBundle(bundleId);
        String normalizedLocale = I18nRules.normalizeLocale(localeCode);
        Set<String> referenceKeys = findEntries(bundleId, bundle.getDefaultLocale()).stream()
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
        BaseI18nPref condition = preferenceCondition();
        BaseI18nPref entity = dataBaseDao.findOne(SqlBuilder.build(condition).dataPermis(PermisRule.ALL));
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
        response.setBundleCode(entity.getBundleCode());
        response.setBundleName(entity.getBundleName());
        response.setClientScopes(entity.getClientScopes());
        response.setDefaultLocale(entity.getDefaultLocale());
        response.setCurrentReleaseId(entity.getCurrentReleaseId());
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
        response.setBundleCode(bundle.getBundleCode());
        response.setDefaultLocale(bundle.getDefaultLocale());
        response.setClientScopes(bundle.getClientScopes());
        response.setReleaseId(release.getId());
        response.setVersionNo(release.getVersionNo());
        response.setChecksum(release.getChecksum());
        response.setSnapshotData(release.getSnapshotData());
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
