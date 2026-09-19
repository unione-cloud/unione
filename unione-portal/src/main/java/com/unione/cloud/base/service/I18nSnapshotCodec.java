package com.unione.cloud.base.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.unione.cloud.base.model.BaseI18nEntry;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.util.JsonUtil;

/** 界面语言包不可变快照的确定性编码与校验。 */
public final class I18nSnapshotCodec {
    private I18nSnapshotCodec() {
    }

    public static String encode(List<BaseI18nEntry> entries) {
        List<BaseI18nEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(BaseI18nEntry::getLocaleCode)
                        .thenComparing(BaseI18nEntry::getEntryKey))
                .toList();
        StringBuilder json = new StringBuilder("{\"locales\":{");
        String currentLocale = null;
        boolean firstLocale = true;
        boolean firstEntry = true;
        for (BaseI18nEntry entry : sorted) {
            if (!entry.getLocaleCode().equals(currentLocale)) {
                if (currentLocale != null) {
                    json.append('}');
                }
                if (!firstLocale) {
                    json.append(',');
                }
                json.append('"').append(escape(entry.getLocaleCode())).append("\":{");
                currentLocale = entry.getLocaleCode();
                firstLocale = false;
                firstEntry = true;
            }
            if (!firstEntry) {
                json.append(',');
            }
            json.append('"').append(escape(entry.getEntryKey())).append("\":\"")
                    .append(escape(entry.getEntryValue())).append('"');
            firstEntry = false;
        }
        if (currentLocale != null) {
            json.append('}');
        }
        return json.append("}}").toString();
    }

    public static String checksum(String snapshotData) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(snapshotData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("当前运行环境不支持SHA-256", e);
        }
    }

    public static String merge(String baseSnapshot, List<BaseI18nEntry> overrides) {
        Map<String, Object> root = JsonUtil.toBean(new TypeReference<Map<String, Object>>() { }, baseSnapshot);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> source = (Map<String, Map<String, String>>) root.get("locales");
        Map<String, Map<String, String>> locales = new TreeMap<>();
        source.forEach((locale, entries) -> locales.put(locale, new TreeMap<>(entries)));
        overrides.stream().filter(entry -> Integer.valueOf(1).equals(entry.getStatus())).forEach(entry ->
                locales.computeIfAbsent(entry.getLocaleCode(), key -> new TreeMap<>())
                        .put(entry.getEntryKey(), entry.getEntryValue()));
        return JsonUtil.toJson(Map.of("locales", locales));
    }

    public static boolean verify(String snapshotData, String expectedChecksum) {
        return checksum(snapshotData).equals(expectedChecksum);
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
