package com.unione.cloud.ws.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ExceptionListener;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.util.ObjectUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket配置类
 * 配置netty-socket.io服务器
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "unione.ws", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WsConfig {
    
    @Autowired
    private WsProperties wsProperties;

    /**
     * 创建SocketIOServer实例
     * @return SocketIOServer
     */
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(wsProperties.getHostname());
        config.setPort(wsProperties.getPort());
        if(!ObjectUtil.isEmpty(wsProperties.getContext())){
            config.setContext(wsProperties.getContext());
        }
        config.setMaxFramePayloadLength(wsProperties.getMaxFramePayloadLength());
        config.setMaxHttpContentLength(wsProperties.getMaxHttpContentLength());
        config.setBossThreads(wsProperties.getBossThreads());
        config.setWorkerThreads(wsProperties.getWorkerThreads());
        config.setPingInterval(wsProperties.getPingInterval());
        config.setPingTimeout(wsProperties.getPingTimeout());
        config.setUpgradeTimeout(wsProperties.getUpgradeTimeout());
        config.setJsonSupport(new WsJsonSupport());
        

        // 添加日志查看连接情况
        config.setExceptionListener(new ExceptionListener() {
            @Override
            public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
                log.error("事件异常: {}", e.getMessage(), e);
            }   
            @Override
            public void onDisconnectException(Exception e, SocketIOClient client) {
                log.error("断开连接异常: {}", e.getMessage(), e);
            }
            @Override
            public void onConnectException(Exception e, SocketIOClient client) {
                log.error("连接异常: {}", e.getMessage(), e);
            }
            @Override
            public void onPingException(Exception e, SocketIOClient client) {
                log.error("Ping异常: {}", e.getMessage(), e);
            }
            @Override
            public void onPongException(Exception e, SocketIOClient client) {
                log.error("Pong异常: {}", e.getMessage(), e);
            }
            @Override
            public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
                log.error("Channel异常: {}", e.getMessage(), e);
                return true;
            }
            @Override
            public void onAuthException(Throwable e, SocketIOClient client) {
                e.printStackTrace();
            }
        });
        return new SocketIOServer(config);
    }

}
