package com.unione.cloud.system.service;

/** 可独立运行的平台多选回归检查。 */
public class PlatformTypeServiceTest {
    public static void main(String[] args) {
        check(PlatformTypeService.supports("pc", "pc"));
        check(!PlatformTypeService.supports("pc", "app"));
        check(PlatformTypeService.supports("app", "app"));
        check(PlatformTypeService.supports("pc,app", "pc"));
        check(PlatformTypeService.supports("pc,app", "app"));
        check(PlatformTypeService.supports(" app, pc,app ", "pc"));
        check(!PlatformTypeService.supports("pcx", "pc"));
        check(!PlatformTypeService.supports(null, "pc"));
        check("pc,app".equals(PlatformTypeService.normalize("app, pc,app")));
        check("".equals(PlatformTypeService.normalize(" ")));
        System.out.println("Platform types: 10 checks passed");
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Platform type regression failed");
        }
    }
}
