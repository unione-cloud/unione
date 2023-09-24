package com.unione.cloud.web.config;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EhcacheConfig {

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@SuppressWarnings("static-access")
    public CacheManager cacheManager(){
    	return CacheManagerBuilder.newCacheManagerBuilder().persistence(System.getProperty("java.io.tmpdir"))
    			.builder(CacheManagerBuilder.newCacheManagerBuilder()).build(true);
    }
	
}
