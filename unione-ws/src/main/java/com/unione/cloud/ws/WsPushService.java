package com.unione.cloud.ws;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.constants.WsConstants;
import com.unione.cloud.ws.model.WsData;
import com.unione.cloud.ws.model.WsDistbuteEntity;
import com.unione.cloud.ws.model.WsEvent;
import com.unione.cloud.ws.model.WsResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket推送服务
 * 负责向客户端推送消息
 */
@Slf4j
@Service
public class WsPushService {

    @Autowired(required = false)
    private SocketIOServer socketIOServer;

    @Autowired
    private WsProperties wsProperties;

    @Autowired
    private WsClientManager wsClientManager;

    @Autowired
    private RedisService redisService;

    @Value("${unione.ws.redis:true}")
    private boolean WS_DISTRIBUTE_ENABLE;

    /**
     * 向指定用户推送数据
     * 
     * @param userId 用户ID
     * @param data   数据
     */
    public void sendData(Long userId, WsData data) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.sendEvent("data", data);
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserId(userId);
            distbute.setData(data);
            redisService.publish(WsConstants.WS_QUEUE_USER_DATA, distbute);
        }
    }

    public void sendData(String roomId, List<Long> userIds, WsData data) {
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                if (client.getAllRooms().contains(roomId)) {
                    client.sendEvent("data", data);
                }
            }
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
            distbute.setUserIds(userIds);
            distbute.setData(data);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_DATA, distbute);
        }
    }

    /**
     * 向指定用户推送响应消息
     * 
     * @param userId   用户ID
     * @param response 响应消息
     */
    public void sendResponse(Long userId, WsResponse response) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.sendEvent("response", response);
        }

        // 如果启用了分布式，通过Redis推送其他节点
        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity data = new WsDistbuteEntity();
            data.setNodeId(wsProperties.getNodeId());
            data.setUserId(userId);
            data.setData(response);
            redisService.publish(WsConstants.WS_QUEUE_USER_RESPONSE, data);
        }
    }

    /**
     * 向指定用户推送事件消息
     * 
     * @param userId 用户ID
     * @param event  事件消息
     */
    public void sendEvent(Long userId, WsEvent event) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.sendEvent(String.format("event:%s", event.getName()), event);
        }

        // 如果启用了分布式，通过Redis推送其他节点
        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity data = new WsDistbuteEntity();
            data.setNodeId(wsProperties.getNodeId());
            data.setUserId(userId);
            data.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_USER_EVENT, data);
        }
    }

    public void sendEvent(String roomId, List<Long> userIds, WsEvent event) {
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                if (client.getAllRooms().contains(roomId)) {
                    client.sendEvent(String.format("event:%s", event.getName()), event);
                }
            }
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
            distbute.setUserIds(userIds);
            distbute.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_EVENT, distbute);
        }
    }

    /**
     * 向所有用户广播事件消息
     * 
     * @param event 事件消息
     */
    public void sendEvent(WsEvent event) {
        if (socketIOServer == null) {
            log.warn("未开启ws服务，请检查配置:unione.ws.*");
            return;
        }
        // 广播本地连接
        socketIOServer.getBroadcastOperations().sendEvent(String.format("event:%s", event.getName()), event);

        // 如果启用了分布式，通过Redis广播到其他节点
        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity data = new WsDistbuteEntity();
            data.setNodeId(wsProperties.getNodeId());
            data.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_BROADCAST_EVENT, data);
        }
    }

    /**
     * 向指定客户端推送消息
     * 
     * @param client SocketIOClient
     * @param data   数据
     */
    public void sendData(SocketIOClient client, WsData data) {
        client.sendEvent("data", data);
    }

    /**
     * 向指定客户端推送响应消息
     * 
     * @param client   SocketIOClient
     * @param response 响应消息
     */
    public void sendResponse(SocketIOClient client, WsResponse response) {
        client.sendEvent("response", response);
    }

    /**
     * 向指定客户端推送事件消息
     * 
     * @param client SocketIOClient
     * @param event  事件消息
     */
    public void sendEvent(SocketIOClient client, WsEvent event) {
        client.sendEvent(String.format("event:%s", event.getName()), event);
    }


    /**
     * 加入房间
     * @param userId
     * @param room
     */
    public void joinRoom(Long userId, String room) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.joinRoom(room);
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserId(userId);
            distbute.setData(room);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_JOIN, distbute);
        }
    }

    /**
     * 离开房间
     * @param userId
     * @param room
     */
    public void leaveRoom(Long userId, String room) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.leaveRoom(room);
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserId(userId);
            distbute.setData(room);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_LEAVE, distbute);
        }
    }

}
