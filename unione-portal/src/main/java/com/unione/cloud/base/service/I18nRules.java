package com.unione.cloud.base.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.unione.cloud.core.exception.AssertUtil;

/**
 * 方言模块无状态业务规则。
 */
public final class I18nRules {

    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[A-Za-z]{2,8}([_-][A-Za-z0-9]{1,8})*$");
    private static final Set<String> CLIENT_SCOPES = Set.of("author", "user", "admin");
    private static final Set<String> SOURCE_TYPES = Set.of("manual", "ai");

    private I18nRules() {
    }

    public static String normalizeLocale(String localeCode) {
        AssertUtil.service().isTrue(localeCode != null && LOCALE_PATTERN.matcher(localeCode.trim()).matches(),
                "语言或方言代码格式不正确");
        String[] parts = localeCode.trim().replace('_', '-').split("-");
        for (int index = 0; index < parts.length; index++) {
            if (index == 0) {
                parts[index] = parts[index].toLowerCase(Locale.ROOT);
            } else if (parts[index].length() == 2) {
                parts[index] = parts[index].toUpperCase(Locale.ROOT);
            } else {
                parts[index] = parts[index].toLowerCase(Locale.ROOT);
            }
        }
        return String.join("-", parts);
    }

    public static String normalizeClientScopes(String clientScopes) {
        AssertUtil.service().notNull(clientScopes, "适用端不能为空");
        LinkedHashSet<String> scopes = Arrays.stream(clientScopes.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        AssertUtil.service().isTrue(!scopes.isEmpty() && CLIENT_SCOPES.containsAll(scopes),
                "适用端只能包含author、user、admin");
        return String.join(",", scopes);
    }

    public static String normalizeSourceType(String sourceType) {
        String normalized = sourceType == null ? "manual" : sourceType.trim().toLowerCase(Locale.ROOT);
        AssertUtil.service().isTrue(SOURCE_TYPES.contains(normalized), "翻译来源只能为manual或ai");
        return normalized;
    }

    public static void validateStatus(Integer status) {
        AssertUtil.service().isTrue(status != null && (status == 0 || status == 1), "状态只能为0或1");
    }
}
