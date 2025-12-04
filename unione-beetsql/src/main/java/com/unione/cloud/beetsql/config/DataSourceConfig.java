package com.unione.cloud.beetsql.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String url;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean("dataSource")
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        try {
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.init();
        } catch (Exception e) {
            // 初始化异常时输出连接信息
            log.error("数据源初始化失败! 连接信息: URL={}, Username={}, Password={}",url, username, StrUtil.isBlank(password) ? "空" : "******");
            throw new RuntimeException("数据源初始化失败", e);
        }
        log.info("数据源初始化成功! 连接信息: URL={}, Username={}, Password={}",url, username, StrUtil.isBlank(password) ? "空" : "******");
        return dataSource;
    }
}