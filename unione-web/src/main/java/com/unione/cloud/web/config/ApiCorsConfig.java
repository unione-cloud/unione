package com.unione.cloud.web.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.hutool.core.util.ObjectUtil;

/**
 * 全局跨域配置类
 * 通过配置文件支持可配置的跨域设置
 */
@Configuration
public class ApiCorsConfig implements WebMvcConfigurer, InitializingBean {
    
    @Autowired
    private Environment env;
    
    /**
     * 允许的跨域配置列表
     */
    private List<ApiCorsProperties> allowed = new ArrayList<>();
    
    /**
     * 配置前缀
     */
    private static final String CORS_CONFIG_PREFIX = "unione.cors";
    
    /**
     * 初始化配置，从环境变量中读取跨域配置
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取所有以 unione.cors. 开头的配置
        allowed = new ArrayList<>();
        for (int index=0;index<200;index++) {
            String url=env.getProperty(String.format("%s.%d.url", CORS_CONFIG_PREFIX,index));
            if (!ObjectUtil.isEmpty(url)) {
                ApiCorsProperties properties = new ApiCorsProperties();
                properties.setUrl(url);

                String origins=env.getProperty(String.format("%s.%d.allowed-origins", CORS_CONFIG_PREFIX,index));
                if (!ObjectUtil.isEmpty(origins)) {
                    properties.setOrigins(origins);
                }

                String methods=env.getProperty(String.format("%s.%d.allowed-methods", CORS_CONFIG_PREFIX,index));
                if (!ObjectUtil.isEmpty(methods)) {
                    properties.setMethods(methods);
                }
                
                String headers=env.getProperty(String.format("%s.%d.allowed-headers", CORS_CONFIG_PREFIX,index));
                if (!ObjectUtil.isEmpty(headers)) {
                    properties.setHeaders(headers);
                }

                String exposed=env.getProperty(String.format("%s.%d.exposed-headers", CORS_CONFIG_PREFIX,index));
                if (!ObjectUtil.isEmpty(exposed)) {
                    properties.setExposed(exposed);
                }
                
                Boolean credentials=env.getProperty(String.format("%s.%d.allow-credentials", CORS_CONFIG_PREFIX,index),Boolean.class);
                if (!ObjectUtil.isEmpty(credentials)) {
                    properties.setCredentials(credentials);
                }
                
                Long maxAge=env.getProperty(String.format("%s.%d.max-age", CORS_CONFIG_PREFIX,index),Long.class);
                if (!ObjectUtil.isEmpty(maxAge)) {
                    properties.setMaxAge(maxAge);
                }
                
                allowed.add(properties);
            }
        }
        
    }
    
    
    
    /**
     * 配置跨域访问规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 将配置的字符串转换为数组
        if(ObjectUtil.isEmpty(allowed)){
            return;
        }
        allowed.stream().filter(a->ObjectUtil.isNotEmpty(a.getUrl())&&ObjectUtil.isNotEmpty(a.getOrigins()))
        .forEach(allow->{
            CorsRegistration corsRegistration = registry.addMapping(allow.getUrl());
            corsRegistration.allowedOrigins(StringUtils.commaDelimitedListToStringArray(allow.getOrigins()));
            if(!ObjectUtil.isEmpty(allow.getCredentials())){
                corsRegistration.allowCredentials(allow.getCredentials());
            }
            if(!ObjectUtil.isEmpty(allow.getMaxAge()) && allow.getMaxAge()>0){
                corsRegistration.maxAge(allow.getMaxAge());
            }
            if(ObjectUtil.isNotEmpty(allow.getMethods())){
                corsRegistration.allowedMethods(StringUtils.commaDelimitedListToStringArray(allow.getMethods()));
            }
            if(ObjectUtil.isNotEmpty(allow.getHeaders())){
                corsRegistration.allowedHeaders(StringUtils.commaDelimitedListToStringArray(allow.getHeaders()));
            }
            if(ObjectUtil.isNotEmpty(allow.getExposed())){
                corsRegistration.exposedHeaders(StringUtils.commaDelimitedListToStringArray(allow.getExposed()));
            }
        });
    }

    
    public static class ApiCorsProperties {
        private String configKey;
        private String url;
        private String origins;
        private String methods;
        private String headers;
        private String exposed;
        private Boolean credentials;
        private Long maxAge;

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }
        public String getConfigKey() {
            return configKey;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }
        public void setOrigins(String origins) {
            this.origins = origins;
        }
        public String getOrigins() {
            return origins;
        }
        public void setMethods(String methods) {
            this.methods = methods;
        }
        public String getMethods() {
            return methods;
        }
        public void setHeaders(String headers) {
            this.headers = headers;
        }
        public String getHeaders() {
            return headers;
        }
        public void setCredentials(Boolean credentials) {
            this.credentials = credentials;
        }
        public Boolean getCredentials() {
            return credentials;
        }
        public void setMaxAge(Long maxAge) {
            this.maxAge = maxAge;
        }
        public Long getMaxAge() {
            return maxAge;
        }
        public String getExposed() {
            return exposed;
        }
        public void setExposed(String exposed) {
            this.exposed = exposed;
        }
    }


}
