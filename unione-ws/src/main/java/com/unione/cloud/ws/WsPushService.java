package com.unione.cloud.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.constants.WsConstants;
import com.unione.cloud.ws.model.WsClientEntity;
import com.unione.cloud.ws.model.WsData;
import com.unione.cloud.ws.model.WsDistbuteEntity;
import com.unione.cloud.ws.model.WsEvent;
import com.unione.cloud.ws.model.WsResponse;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket推送服务
 * 负责向客户端推送消息
 */
@Slf4j
@Service
public class WsPushService {

    /**
     * 房间连接缓存
     */
    private final Map<String, List<SocketIOClient>> roomConnectionMap = new ConcurrentHashMap<>();

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
     * 连接信息键前缀
     */
    private static final String CLIENT_KEY_PREFIX = "unione:ws:client:%s";

    /**
     * 向指定用户推送数据
     * 
     * @param userId 用户ID
     * @param data   数据
     */
    public void sendData(Long userId, WsData data) {
        sendData(userId, data, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定用户推送数据
     * 
     * @param userId 用户ID
     * @param data   数据
     */
    public void sendData(Long userId, WsData data, boolean distribute) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            try{
                client.sendEvent("data", data);
            }catch(Exception e){
                wsClientManager.remove(client);
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserId(userId);
            distbute.setData(data);
            redisService.publish(WsConstants.WS_QUEUE_USER_DATA, distbute);
        }
    }

    /**
     * 向指定用户推送数据
     * 
     * @param userId 用户ID
     * @param data   数据
     */
    public void sendData(List<Long> userId, WsData data) {
        sendData(userId, data, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定用户推送数据
     * 
     * @param userId 用户ID
     * @param data   数据
     */
    public void sendData(List<Long> userIds, WsData data, boolean distribute) {
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                try{
                    client.sendEvent("data", data);
                }catch(Exception e){
                    wsClientManager.remove(client);
                }
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserIds(userIds);
            distbute.setData(data);
            redisService.publish(WsConstants.WS_QUEUE_USER_DATA, distbute);
        }
    }

    /**
     * 向指定房间中的用户推送数据
     * 
     * @param roomId  房间ID
     * @param userIds 用户ID列表
     * @param data    数据
     */
    public void sendData(String roomId, List<Long> userIds, WsData data) {
        sendData(roomId, userIds, data, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定房间中的用户推送数据
     * 
     * @param roomId  房间ID
     * @param userIds 用户ID列表
     * @param data    数据
     */
    public void sendData(String roomId, List<Long> userIds, WsData data, boolean distribute) {
        if (ObjectUtil.isEmpty(userIds) || ObjectUtil.isEmpty(roomId)) {
            return;
        }
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                if (client.getAllRooms().contains(roomId)) {
                    try{
                        client.sendEvent("data", data);
                    }catch(Exception e){
                        wsClientManager.remove(client);
                    }
                }
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
            distbute.setUserIds(userIds);
            distbute.setData(data);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_DATA, distbute);
        }
    }

    /**
     * 向指定房间中的用户推送数据
     * 
     * @param roomId 房间ID
     * @param data   数据
     */
    public void sendData(String roomId, WsData data) {
        sendData(roomId, data, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定房间中的用户推送数据
     * 
     * @param roomId 房间ID
     * @param data   数据
     */
    public void sendData(String roomId, WsData data, boolean distribute) {
        if (ObjectUtil.isEmpty(roomId)) {
            return;
        }
        // 推送本地连接
        List<SocketIOClient> clients = roomConnectionMap.getOrDefault(roomId, new ArrayList<>());
        for (SocketIOClient client : clients) {
            if (client.getAllRooms().contains(roomId)) {
                try{
                    client.sendEvent("data", data);
                }catch(Exception e){
                    wsClientManager.remove(client);
                }
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
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
        sendResponse(userId, response, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定用户推送响应消息
     * 
     * @param userId   用户ID
     * @param response 响应消息
     */
    public void sendResponse(Long userId, WsResponse response, boolean distribute) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            try{
                client.sendEvent("response", response);
            }catch(Exception e){
                wsClientManager.remove(client);
            }
        }

        // 如果启用了分布式，通过Redis推送其他节点
        if (distribute) {
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
        sendEvent(userId, event, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定用户推送事件消息
     * 
     * @param userId 用户ID
     * @param event  事件消息
     */
    public void sendEvent(Long userId, WsEvent event, boolean distribute) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            try{
                client.sendEvent(String.format("event:%s", event.getEvent()), event);
            }catch(Exception e){
                wsClientManager.remove(client);
            }
        }

        // 如果启用了分布式，通过Redis推送其他节点
        if (distribute) {
            WsDistbuteEntity data = new WsDistbuteEntity();
            data.setNodeId(wsProperties.getNodeId());
            data.setUserId(userId);
            data.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_USER_EVENT, data);
        }
    }

    /**
     * 向指定用户推送事件消息
     * 
     * @param userId 用户ID
     * @param event  事件消息
     */
    public void sendEvent(List<Long> userIds, WsEvent event) {
        sendEvent(userIds, event, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定用户推送事件消息
     * 
     * @param userId 用户ID
     * @param event  事件消息
     */
    public void sendEvent(List<Long> userIds, WsEvent event, boolean distribute) {
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                try{
                    client.sendEvent(String.format("event:%s", event.getEvent()), event);
                }catch(Exception e){
                    wsClientManager.remove(client);
                }
            }
        }

        // 如果启用了分布式，通过Redis推送其他节点
        if (distribute) {
            WsDistbuteEntity data = new WsDistbuteEntity();
            data.setNodeId(wsProperties.getNodeId());
            data.setUserIds(userIds);
            data.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_USER_EVENT, data);
        }
    }

    /**
     * 向指定房间中的用户推送事件消息
     * 
     * @param roomId  房间ID
     * @param userIds 用户ID列表
     * @param event   事件消息
     */
    public void sendEvent(String roomId, List<Long> userIds, WsEvent event) {
        sendEvent(roomId, userIds, event, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定房间中的用户推送事件消息
     * 
     * @param roomId  房间ID
     * @param userIds 用户ID列表
     * @param event   事件消息
     */
    public void sendEvent(String roomId, List<Long> userIds, WsEvent event, boolean distribute) {
        if (ObjectUtil.isEmpty(userIds) || ObjectUtil.isEmpty(event)) {
            return;
        }
        // 推送本地连接
        for (Long userId : userIds) {
            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
            for (SocketIOClient client : clients) {
                if (client.getAllRooms().contains(roomId)) {
                    try{
                        client.sendEvent(String.format("event:%s", event.getEvent()), event);
                    }catch(Exception e){
                        wsClientManager.remove(client);
                    }
                }
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
            distbute.setUserIds(userIds);
            distbute.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_EVENT, distbute);
        }
    }

    /**
     * 向指定房间中的用户推送事件消息
     * 
     * @param roomId 房间ID
     * @param event  事件消息
     */
    public void sendEvent(String roomId, WsEvent event) {
        sendEvent(roomId, event, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向指定房间中的用户推送事件消息
     * 
     * @param roomId 房间ID
     * @param event  事件消息
     */
    public void sendEvent(String roomId, WsEvent event, boolean distribute) {
        if (ObjectUtil.isEmpty(event)) {
            return;
        }
        // 推送本地连接
        List<SocketIOClient> clients = roomConnectionMap.getOrDefault(roomId, new ArrayList<>());
        for (SocketIOClient client : clients) {
            if (client.getAllRooms().contains(roomId)) {
                try{
                    client.sendEvent(String.format("event:%s", event.getEvent()), event);
                }catch(Exception e){
                    wsClientManager.remove(client);
                }
            }
        }

        if (distribute) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setRoomId(roomId);
            distbute.setData(event);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_EVENT, distbute);
        }
    }

    public void sendEvent(WsEvent event) {
        sendEvent(event, WS_DISTRIBUTE_ENABLE);
    }

    /**
     * 向所有用户广播事件消息
     * 
     * @param event 事件消息
     */
    public void sendEvent(WsEvent event, boolean distribute) {
        if (socketIOServer == null) {
            log.warn("未开启ws服务，请检查配置:unione.ws.*");
            return;
        }
        // 广播本地连接
        socketIOServer.getBroadcastOperations().sendEvent(String.format("event:%s", event.getEvent()), event);

        // 如果启用了分布式，通过Redis广播到其他节点
        if (distribute) {
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
        try{
            client.sendEvent("data", data);
        }catch(Exception e){
            wsClientManager.remove(client);
        }
    }

    /**
     * 向指定客户端推送响应消息
     * 
     * @param client   SocketIOClient
     * @param response 响应消息
     */
    public void sendResponse(SocketIOClient client, WsResponse response) {
        try{
            client.sendEvent("response", response);
        }catch(Exception e){
            wsClientManager.remove(client);
        }
    }

    /**
     * 向指定客户端推送事件消息
     * 
     * @param client SocketIOClient
     * @param event  事件消息
     */
    public void sendEvent(SocketIOClient client, WsEvent event) {
        try{
            client.sendEvent(String.format("event:%s", event.getEvent()), event);
        }catch(Exception e){
            wsClientManager.remove(client);
        }
    }


    /**
     * 加入房间
     * 
     * @param userId
     * @param room
     */
    public void joinRoom(Long userId, String room, SocketIOClient ...ioClient) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        if (ioClient.length>0) {
            clients = Arrays.asList(ioClient);
        }
        for (SocketIOClient client : clients) {
            client.joinRoom(room);
            List<SocketIOClient> list = roomConnectionMap.getOrDefault(room, new ArrayList<>());
            list.add(client);
            roomConnectionMap.put(room, list);

            // 更新客户端房间列表
            String connectionKey = String.format(CLIENT_KEY_PREFIX, client.getSessionId());
            // 存储连接信息
            WsClientEntity clientEntity = redisService.getObj(connectionKey);
            if (clientEntity != null) {
                clientEntity.getRooms().add(room);
                redisService.put(connectionKey, clientEntity);
            }
        }

        // if (distribute) {
        //     WsDistbuteEntity distbute = new WsDistbuteEntity();
        //     distbute.setNodeId(wsProperties.getNodeId());
        //     distbute.setUserId(userId);
        //     distbute.setData(room);
        //     redisService.publish(WsConstants.WS_QUEUE_ROOM_JOIN, distbute);
        // }
    }

    /**
     * 离开房间
     * 
     * @param userId
     * @param room
     */
    public void leaveRoom(Long userId, String room) {
        // 推送本地连接
        List<SocketIOClient> clients = wsClientManager.getLocal(userId);
        for (SocketIOClient client : clients) {
            client.leaveRoom(room);
            roomConnectionMap.getOrDefault(room, new ArrayList<>()).remove(client);

            // 更新客户端房间列表
            String connectionKey = String.format(CLIENT_KEY_PREFIX, client.getSessionId());
            // 存储连接信息
            WsClientEntity clientEntity = redisService.getObj(connectionKey);
            if (clientEntity != null) {
                clientEntity.getRooms().remove(room);
                redisService.put(connectionKey, clientEntity);
            }
        }

        if (WS_DISTRIBUTE_ENABLE) {
            WsDistbuteEntity distbute = new WsDistbuteEntity();
            distbute.setNodeId(wsProperties.getNodeId());
            distbute.setUserId(userId);
            distbute.setData(room);
            redisService.publish(WsConstants.WS_QUEUE_ROOM_LEAVE, distbute);
        }
    }

    /**
     * 获取房间内所有客户端
     * 
     * @param room
     * @return
     */
    public List<SocketIOClient> getRoomClients(String room) {
        return roomConnectionMap.getOrDefault(room, new ArrayList<>());
    }

}
