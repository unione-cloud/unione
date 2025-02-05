package com.unione.cloud.web.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RefreshScope
@Configuration
public class UniOneMvcConfigurer implements WebMvcConfigurer {
	
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 	静态资源映射配置，json对象配置，eg:{\"/**\":\"classpath:/static/\"}
	 */
	@Value("${mvc.resource.handler:}")
	private String RESOURCE_HANDLER;
	
	/**
	 * 	服务资源映射配置，/sms,/email,/doc
	 */
	@Value("${mvc.server.handler:}")
	private List<String> SERVER_HANDLER;
	
	public UniOneMvcConfigurer(ThymeleafProperties thymeleafProperties,SpringResourceTemplateResolver springResourceTemplateResolver) {
		if(ThymeleafProperties.DEFAULT_PREFIX.equals(thymeleafProperties.getPrefix())) {
			log.info("使用默认静态资源路径:classpath:/static/");
			springResourceTemplateResolver.setPrefix("classpath:/static/");
		}else {
			log.info("使用设置静态资源路径:{}",thymeleafProperties.getPrefix());
		}
		
	}
	
	
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		/**
		* 	序列换成json时,将所有的long变成string
		* 	因为js中得数字类型不能包含所有的java long值
		*/
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		objectMapper.registerModule(simpleModule);
		jackson2HttpMessageConverter.setObjectMapper(objectMapper);
		converters.add(jackson2HttpMessageConverter);
	}



	@Override
	@SuppressWarnings("unchecked")
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 静态资源
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
		// 配置knife4j
		registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
		// 公共部分内容
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		
		// 额外扩展配置
		if(!StringUtils.isEmpty(RESOURCE_HANDLER)) {
			try {
				Map<String,String> map = objectMapper.readValue(RESOURCE_HANDLER, Map.class);
				for(Entry<String, String> entry:map.entrySet()) {
					registry.addResourceHandler(entry.getKey()).addResourceLocations(entry.getValue());
				}
			} catch (Exception e) {
				log.error("解析配置项:security.filter.resourcehandler失败,value:{}",RESOURCE_HANDLER,e);
			}
		}
	}

	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index.html");
		
		if(SERVER_HANDLER!=null && !SERVER_HANDLER.isEmpty()) {
			log.info("==========注册服务资源【开始】count:{}=============",SERVER_HANDLER.size());
			SERVER_HANDLER.stream().forEach(ctx->{
				if(ctx.startsWith("/")) {
					registry.addViewController(ctx).setViewName(ctx.substring(1)+"/index.html");
				}else {
					registry.addViewController("/"+ctx).setViewName(ctx+"/index.html");
				}
				log.info("成功注册服务资源：{}",ctx);
			});
			log.info("==========注册服务资源【完成】=============");
		}
		
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        WebMvcConfigurer.super.addViewControllers(registry);
	}
	
}
