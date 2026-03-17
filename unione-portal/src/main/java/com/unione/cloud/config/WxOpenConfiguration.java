package com.unione.cloud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.binarywang.spring.starter.wxjava.open.properties.WxOpenProperties;

import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.open.api.WxOpenConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenOAuth2ServiceImpl;

@Configuration
@ConditionalOnClass(WxOAuth2Service.class)
@EnableConfigurationProperties(WxOpenProperties.class)
@AllArgsConstructor
public class WxOpenConfiguration {

   @Autowired
   private WxOpenProperties properties;

   @Autowired
   private WxOpenConfigStorage configStorage;


   @Bean
   @ConditionalOnMissingBean
   public WxOAuth2Service wxOAuth2Service() {
       return new WxOpenOAuth2ServiceImpl(properties.getAppId(),properties.getSecret(),configStorage);
   }

}