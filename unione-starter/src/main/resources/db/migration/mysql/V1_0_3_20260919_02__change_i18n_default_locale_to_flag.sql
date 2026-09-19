UPDATE `base_i18n_bundle`
SET `DEFAULT_LOCALE` = '0';

ALTER TABLE `base_i18n_bundle`
    MODIFY COLUMN `DEFAULT_LOCALE` int NOT NULL DEFAULT 0 COMMENT '是否默认语言：1是，0否';

UPDATE `base_i18n_bundle` bundle
INNER JOIN (
    SELECT COALESCE(`TENANT_ID`, 0) AS `TENANT_SCOPE`, MIN(`ID`) AS `DEFAULT_BUNDLE_ID`
    FROM `base_i18n_bundle`
    WHERE `IS_GLOBAL` = 1 AND `DEL_FLAG` = 0
    GROUP BY COALESCE(`TENANT_ID`, 0)
) defaults
    ON COALESCE(bundle.`TENANT_ID`, 0) = defaults.`TENANT_SCOPE`
    AND bundle.`ID` = defaults.`DEFAULT_BUNDLE_ID`
SET bundle.`DEFAULT_LOCALE` = 1;

UPDATE `base_i18n_bundle` tenant_bundle
INNER JOIN `base_i18n_bundle` global_bundle
    ON tenant_bundle.`GLOBAL_BUNDLE_ID` = global_bundle.`ID`
SET tenant_bundle.`DEFAULT_LOCALE` = global_bundle.`DEFAULT_LOCALE`
WHERE tenant_bundle.`IS_GLOBAL` = 0;
