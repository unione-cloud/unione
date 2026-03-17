package com.unione.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alicp.jetcache.anno.config.EnableMethodCache;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * 微应用服务核心启动类
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan({"com.unione.cloud","com.binarywang.spring"})
@EnableFeignClients("com.unione.cloud")
@EnableMethodCache(basePackages = "com.unione.cloud")
public class Application extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
}
