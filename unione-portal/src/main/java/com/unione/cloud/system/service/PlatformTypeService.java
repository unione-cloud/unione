package com.unione.cloud.system.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** 统一识别系统、应用的多选平台类型。 */
public final class PlatformTypeService {

    private PlatformTypeService() {
    }

    public static List<String> parse(String types) {
        if (types == null || types.isBlank()) {
            return List.of();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(type -> "pc".equals(type) || "app".equals(type))
                .distinct()
                .collect(Collectors.toList());
    }

    public static String normalize(String types) {
        List<String> platforms = parse(types);
        return List.of("pc", "app").stream()
                .filter(platforms::contains)
                .collect(Collectors.joining(","));
    }

    public static boolean supports(String types, String platform) {
        return parse(types).contains(platform);
    }
}
