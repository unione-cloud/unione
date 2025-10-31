package com.unione.cloud.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alicp.jetcache.anno.config.EnableMethodCache;

/**
 * 微应用服务核心启动类
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan({"com.unione.cloud","com.xxl.job"})
@MapperScan("com.xxl.job.admin.mapper")
@EnableFeignClients("com.unione.cloud")
@EnableMethodCache(basePackages = "com.unione.cloud")
public class UnioneJobApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UnioneJobApplication.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(UnioneJobApplication.class, args);
	}
}
