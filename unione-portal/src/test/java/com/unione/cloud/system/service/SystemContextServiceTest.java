package com.unione.cloud.system.service;

import java.util.Set;

/** Standalone regression checks; this module does not depend on a test framework. */
public class SystemContextServiceTest {
    public static void main(String[] args) {
        check("cms", "cms", Set.of());
        check("cms1", "cms", Set.of("cms"));
        check("cms3", "cms", Set.of("cms", "cms1", "cms2"));
        check("cms2", "cms", Set.of("cms", "cms1", "cms3"));
        check("cms1", "cms", Set.of("cms", "crm"));
        check("abcdefghijklmn1", "abcdefghijklmno", Set.of("abcdefghijklmno"));
        check("cms11", "cms1", Set.of("cms1"));
        System.out.println("System context allocation: 7 checks passed");
    }

    private static void check(String expected, String base, Set<String> occupied) {
        String actual = SystemContextService.availableContext(base, occupied);
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
