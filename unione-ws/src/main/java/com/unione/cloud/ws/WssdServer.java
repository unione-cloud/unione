package com.unione.cloud.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.unione.cloud.ws.handler.UniWebSocketHandler;

@Component
@Configuration
@EnableWebSocket
public class WssdServer implements WebSocketConfigurer{

    @Autowired
    private UniWebSocketHandler uniWebSocketHandler;
    
    @SuppressWarnings("null")
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
         registry.addHandler(uniWebSocketHandler, "/wssd")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");  // 允许跨域
    }

}
