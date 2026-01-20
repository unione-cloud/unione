package com.unione.cloud.ws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.unione.cloud.core.util.SpringCtxUtil;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.handler.WsAbstractHandler;

/**
 * WebSocket连接器
 * 管理WebSocket连接的生命周期
 */
@Component
@ConditionalOnProperty(prefix = "unione.ws", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WsServer implements ApplicationRunner {
    
    @Autowired
    private WsClientManager wsClientManager;
    
    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private WsProperties wsProperties;

    /**
     * 初始化WebSocket连接器
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void init() {
        // 注册连接事件监听器
        socketIOServer.addConnectListener(onConnect());
        
        // 注册断开连接事件监听器
        socketIOServer.addDisconnectListener(onDisconnect());
        
        SpringCtxUtil.getBeans(WsAbstractHandler.class).values().forEach(handler->{
            Type type = handler.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();
            socketIOServer.addEventListener(handler.getName(),(Class) types[0], handler);
        });
        
    }

    /**
     * 连接事件监听器
     * @return ConnectListener
     */
    private ConnectListener onConnect() {
        return client -> {
            wsClientManager.add(client);
        };
    }

    /**
     * 断开连接事件监听器
     * @return DisconnectListener
     */
    private DisconnectListener onDisconnect() {
        return client -> {
            wsClientManager.remove(client);
        };
    }

    /**
     * 启动WebSocket服务器
     */
    public void start() {
        socketIOServer.start();
    }

    /**
     * 停止WebSocket服务器
     */
    public void stop() {
        socketIOServer.stop();
    }

    /**
     * 获取SocketIOServer实例
     * @return SocketIOServer
     */
    public SocketIOServer getServer() {
        return socketIOServer;
    }

    /**
     * 应用启动时启动WebSocket服务器
     * @param event ApplicationEvent
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	 this.init();
         this.start();
         
         System.out.println();
         System.out.println();
         System.out.println("===========================================================");
         System.out.println("  ::  WebSocket Server Started  ::  ");
         System.out.println("  ::  Host: " + wsProperties.getHostname());
         System.out.println("  ::  Port: " + wsProperties.getPort());
         System.out.println("  ::  NodeId: " + wsProperties.getNodeId());
         System.out.println("===========================================================");
    }
}
