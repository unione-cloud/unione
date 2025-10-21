package com.unione.cloud.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * 	微应用接口服务Api Doc配置
 * 
 * @author Unione Cloud
 * @version 5.0.0
 * @since 2024-03-12
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = true)
public class Swagger2Config {
	
	@Value("${spring.application.name}")
	private String name;
	
	@Value("${knife4j.author.name:Jeking 杨}")
	private String authorName;
	
	@Value("${knife4j.author.email:dev@unione.cloud}")
	private String authorEmail;

    @Value("${knife4j.info.version:v1.0.0}")
    private String version;

    @Value("${knife4j.info.url:https://www.unione.cloud}")
    private String url;
	
	@Bean
    public OpenAPI springDocOpenAPI() {
		Contact contact = new Contact();
        contact.setName(authorName);
        contact.setEmail(authorEmail);
        return new OpenAPI()
    		.components(new Components().addSecuritySchemes("token",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("token")))
            .info(new Info()
            	.title("Unione Cloud "+name+" 接口文档")
            	.description("Unione Cloud "+name+" 微应用开发平台接口说明文档")
                .version(version)
                .contact(contact)
                .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                .url(url));
    }

}