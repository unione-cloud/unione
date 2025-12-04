package com.unione.cloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.unione.cloud.gateway.filter.SecurityFilter;

@Configuration
public class UnioneConfig {
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilter securityFilter() {
		return new SecurityFilter();
	}
	
}
