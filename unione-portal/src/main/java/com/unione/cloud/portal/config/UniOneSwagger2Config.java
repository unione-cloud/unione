package com.unione.cloud.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 应用接口服务Swagger配置
 * @author Jeking Yang
 * @version 1.0.0
 */
@EnableWebMvc
@EnableSwagger2
@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(Docket.class)
public class UniOneSwagger2Config {
	
	@Value("${spring.application.name}")
	private String name;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.unione.cloud"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Uin One Cloud "+name+" 接口文档")
                .description("in One Cloud "+name+" 微应用开发平台接口说明文档")
                .termsOfServiceUrl("https://doc.unione.cloud")
                .version("1.0")
                .contact(new Contact("Jeking Yang", "https://doc.unione.cloud", "dev@unione.cloud"))
                .build();
    }
}