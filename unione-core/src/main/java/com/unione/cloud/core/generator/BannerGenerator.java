package com.unione.cloud.core.generator;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BannerGenerator{
    
    // 静态代码块 - 在类加载时立即执行，这是JVM层面最高优先级的执行方式之一
    // 当JVM首次加载这个类时，静态代码块会被立即执行，优先级高于Spring容器的初始化过程
    static {
        // 直接调用generate方法并打印结果
        System.out.println(generate());
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("             _                         _                 _ \n");
        sb.append(" _   _ _ __ (_) ___  _ __   ___    ___| | ___  _   _  __| | \n");
        sb.append("| | | | '_ \\| |/ _ \\| '_ \\ / _ \\  / __| |/ _ \\| | | |/ _` |\n");
        sb.append("| |_| | | | | | (_) | | | |  __/ | (__| | (_) | |_| | (_| |\n");
        sb.append(" \\__,_|_| |_| |\\___/|_| |_|\\___|  \\___| |\\___/ \\__,_|\\__,_|\n");
        sb.append("\n");
        sb.append("  ::  UNIONE CLOUD  ::  (v1.0.0)\n");
        sb.append("\n");
        return sb.toString();
    }
}
