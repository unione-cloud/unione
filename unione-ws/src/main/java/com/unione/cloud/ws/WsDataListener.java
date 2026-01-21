package com.unione.cloud.ws;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.constants.WsConstants;
import com.unione.cloud.ws.model.WsData;
import com.unione.cloud.ws.model.WsDistbuteEntity;
import com.unione.cloud.ws.model.WsEvent;
import com.unione.cloud.ws.model.WsResponse;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WsDataListener {

    @Autowired
    private RedisService redisService;

    @Autowired
    private WsProperties wsProperties;

    @Autowired
    private WsPushService wsPushService;

    @Autowired
    private WsClientManager wsClientManager;

    @PostConstruct
    public void postConstruct() {
        // 订阅ws消息:用户数据
        redisService.subscribe(WsConstants.WS_QUEUE_USER_DATA, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {
                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId()) && distbute.getUserId() != null) {
                        WsData entry = BeanUtils.toBean(distbute.getData(), WsData.class);
                        List<SocketIOClient> clients = wsClientManager.getLocal(distbute.getUserId());
                        for (SocketIOClient client : clients) {
                            wsPushService.sendData(client, entry);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播用户数据异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:用户响应
        redisService.subscribe(WsConstants.WS_QUEUE_USER_RESPONSE, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {
                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId()) && distbute.getUserId() != null) {
                        WsResponse entry = BeanUtils.toBean(distbute.getData(), WsResponse.class);
                        List<SocketIOClient> clients = wsClientManager.getLocal(distbute.getUserId());
                        for (SocketIOClient client : clients) {
                            wsPushService.sendResponse(client, entry);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播用户响应异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:用户事件
        redisService.subscribe(WsConstants.WS_QUEUE_USER_EVENT, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {
                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId()) && distbute.getUserId() != null) {
                        WsEvent entry = BeanUtils.toBean(distbute.getData(), WsEvent.class);
                        List<SocketIOClient> clients = wsClientManager.getLocal(distbute.getUserId());
                        for (SocketIOClient client : clients) {
                            wsPushService.sendEvent(client, entry);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播用户事件异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:房间数据
        redisService.subscribe(WsConstants.WS_QUEUE_ROOM_DATA, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {
                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId()) && !ObjectUtil.isEmpty(distbute.getUserIds()) && !ObjectUtil.isEmpty(distbute.getRoomId())) {
                        WsData entry = BeanUtils.toBean(distbute.getData(), WsData.class);
                        for (Long userId : distbute.getUserIds()) {
                            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
                            for (SocketIOClient client : clients) {
                                if(client.getAllRooms().contains(distbute.getRoomId())){
                                    wsPushService.sendData(client, entry);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("广播房间数据异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:房间事件
        redisService.subscribe(WsConstants.WS_QUEUE_ROOM_EVENT, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {
                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId()) && !ObjectUtil.isEmpty(distbute.getUserIds()) && !ObjectUtil.isEmpty(distbute.getRoomId())) {
                        WsEvent entry = BeanUtils.toBean(distbute.getData(), WsEvent.class);
                        for (Long userId : distbute.getUserIds()) {
                            List<SocketIOClient> clients = wsClientManager.getLocal(userId);
                            for (SocketIOClient client : clients) {
                                if(client.getAllRooms().contains(distbute.getRoomId())){
                                    wsPushService.sendEvent(client, entry);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("广播房间数据异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:广播事件
        redisService.subscribe(WsConstants.WS_QUEUE_BROADCAST_EVENT, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {

                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId())) {
                        WsEvent entry = BeanUtils.toBean(distbute.getData(), WsEvent.class);
                        List<SocketIOClient> clients = wsClientManager.getLocal();
                        for (SocketIOClient client : clients) {
                            wsPushService.sendEvent(client, entry);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播事件异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:房间加入事件
        redisService.subscribe(WsConstants.WS_QUEUE_ROOM_JOIN, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {

                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId())) {
                        String room=distbute.getData().toString();
                        List<SocketIOClient> clients = wsClientManager.getLocal(distbute.getUserId());
                        for (SocketIOClient client : clients) {
                            client.joinRoom(room);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播房间加入事件异常,distbute:{}", distbute, e);
                }
            }
        });

        // 订阅ws消息:房间离开事件
        redisService.subscribe(WsConstants.WS_QUEUE_ROOM_LEAVE, new MessageListener() {
            @Override
            @SuppressWarnings("null")
            public void onMessage(Message message, @Nullable byte[] arg1) {

                WsDistbuteEntity distbute = null;
                try {
                    byte[] data = message.getBody();
                    if (ObjectUtil.isEmpty(data)) {
                        return;
                    }
                    String json = new String(data, "UTF-8");
                    distbute = JsonUtil.toBean(WsDistbuteEntity.class, json);
                    if (!wsProperties.getNodeId().equals(distbute.getNodeId())) {
                        String room=distbute.getData().toString();
                        List<SocketIOClient> clients = wsClientManager.getLocal(distbute.getUserId());
                        for (SocketIOClient client : clients) {
                            client.leaveRoom(room);
                        }
                    }
                } catch (Exception e) {
                    log.error("广播房间离开事件异常,distbute:{}", distbute, e);
                }
            }
        });

    }

}
