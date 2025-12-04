package com.unione.cloud.core.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BannerGenerator{

    private String serverName;

    private String serverPort;
    
    private String serverCtx;

    @Autowired
    public void setEnv(Environment env) {
        this.serverName = env.getProperty("spring.application.name");
        this.serverPort = env.getProperty("server.port");
        this.serverCtx = env.getProperty("server.servlet.context-path","/");
        System.out.println(generate());
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("             _                         _                 _ \n");
        sb.append(" _   _ _ __ (_) ___  _ __   ___    ___| | ___  _   _  __| | \n");
        sb.append("| | | | '_ \\| |/ _ \\| '_ \\ / _ \\  / __| |/ _ \\| | | |/ _` |\n");
        sb.append("| |_| | | | | | (_) | | | |  __/ | (__| | (_) | |_| | (_| |\n");
        sb.append(" \\__,_|_| |_| |\\___/|_| |_|\\___|  \\___| |\\___/ \\__,_|\\__,_|\n");
        sb.append("===========================================================\n");
        sb.append("\n");
        sb.append("  ::  UNIONE CLOUD  ::  (v1.0.1)\n");
        sb.append(String.format("  ::  SERVER NAME:%s CTX:%s PORT:%s  ::\n", serverName,serverCtx,serverPort));
        sb.append("\n");
        sb.append("===========================================================\n");
        return sb.toString();
    }
}
