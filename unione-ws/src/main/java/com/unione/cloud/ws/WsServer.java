package com.unione.cloud.ws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.unione.cloud.core.util.SpringCtxUtil;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.handler.WsAbstractHandler;
import com.unione.cloud.ws.model.WsNodeEntity;

/**
 * WebSocket连接器
 * 管理WebSocket连接的生命周期
 */
@Component
@ConditionalOnProperty(prefix = "unione.ws", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WsServer implements ApplicationListener<ApplicationEvent> {

    @Autowired
    private WsClientManager wsClientManager;

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private WsHealthChecker wsHealthChecker;

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

        SpringCtxUtil.getBeans(WsAbstractHandler.class).values().forEach(handler -> {
            Type type = handler.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();
            socketIOServer.addEventListener(handler.getName(), (Class) types[0], handler);
        });

    }

    /**
     * 连接事件监听器
     * 
     * @return ConnectListener
     */
    private ConnectListener onConnect() {
        return client -> {
            wsClientManager.add(client);
        };
    }

    /**
     * 断开连接事件监听器
     * 
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
        wsHealthChecker.delNode(wsProperties.getNodeId());
        socketIOServer.stop();
    }

    /**
     * 获取SocketIOServer实例
     * 
     * @return SocketIOServer
     */
    public SocketIOServer getServer() {
        return socketIOServer;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent) {
            this.init();
            this.start();
            List<WsNodeEntity> nodes = wsHealthChecker.getNodes();

            System.out.println();
            System.out.println();
            System.out.println("===========================================================");
            System.out.println("  ::  WebSocket Server Started  ::  ");
            System.out.println("  ::  Host: " + wsProperties.getHostname());
            System.out.println("  ::  Port: " + wsProperties.getPort());
            System.out.println("  ::  NodeId: " + wsProperties.getNodeId());
            System.out.println("  ::  Normal Node Count: " + nodes.size());
            System.out.println("===========================================================");
        } else if (event instanceof ContextClosedEvent) {
            this.stop();
            System.out.println("===========================================================");
            System.out.println("  ::  WebSocket Server Stopped  ::  ");
            System.out.println("===========================================================");
        }

    }

}
