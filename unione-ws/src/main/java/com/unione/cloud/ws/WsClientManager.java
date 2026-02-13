package com.unione.cloud.ws;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.constants.WsConstants;
import com.unione.cloud.ws.model.WsClientEntity;
import com.unione.cloud.ws.model.WsResponse;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式WebSocket连接管理器
 * 使用Redis实现跨节点连接管理
 */
@Slf4j
@Service
public class WsClientManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private WsProperties wsProperties;

    @Autowired
    private TokenService tokenService;

    /**
     * 本地连接缓存
     */
    private final Map<String, SocketIOClient> localConnectionMap = new ConcurrentHashMap<>();

    /**
     * 本地用户连接映射
     */
    private final Map<Long, List<SocketIOClient>> localUserConnectionMap = new ConcurrentHashMap<>();

    /**
     * 连接信息键前缀
     */
    private static final String CLIENT_KEY_PREFIX = "unione:ws:client:%s";

    /**
     * 用户连接键前缀
     */
    private static final String USER_KEY_PREFIX = "unione:ws:user:%s";

    /**
     * 添加本地连接
     * 
     * @param client SocketIOClient
     */
    public void add(SocketIOClient client) {

        String token = client.getHandshakeData().getHttpHeaders().get("token");
        if (ObjectUtil.isEmpty(token)) {
            token=client.getHandshakeData().getSingleUrlParam("token");
        }
        if (ObjectUtil.isEmpty(token)) {
            log.error("websocket 连接失败，token不能为空");
            WsResponse error= WsResponse.error("token不能为空");
            error.setCode(WsConstants.ERROR_CODE_AUTH_FAIL);
            client.sendEvent("error",error);
            client.disconnect();
            return;
        }
        UserPrincipal principal = tokenService.toPrincipal(token);
        if (principal == null || principal.getId() == null) {
            log.error("websocket 连接失败，token无效");
            WsResponse error= WsResponse.error("token无效");
            error.setCode(WsConstants.ERROR_CODE_AUTH_FAIL);
            client.sendEvent("error",error);
            client.disconnect();
            return;
        }

        // 绑定用户ID到连接
        this.bindUser(client, principal.getId());
    }

    /**
     * 绑定用户ID到连接
     * 
     * @param client SocketIOClient
     * @param userId 用户ID
     */
    public void bindUser(SocketIOClient client, long userId) {
        String sessionId = client.getSessionId().toString();
        localConnectionMap.put(sessionId, client);

        client.set(WsConstants.CONNECT_ATTR_USER_ID, userId);
        client.set(WsConstants.CONNECT_ATTR_CONNECT_TIME, System.currentTimeMillis());
        client.set(WsConstants.CONNECT_ATTR_LAST_ACTIVE_TIME, System.currentTimeMillis());

        // 添加到本地用户连接映射
        localUserConnectionMap.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(client);

        // 同步到Redis
        String connectionKey = String.format(CLIENT_KEY_PREFIX, sessionId);

        // 存储连接信息
        WsClientEntity clientEntity = redisService.getObj(connectionKey);
        if (clientEntity == null) {
            clientEntity = new WsClientEntity();
            clientEntity.setNodeId(wsProperties.getNodeId());
            clientEntity.setClientId(sessionId);
            clientEntity.setConnectTime(System.currentTimeMillis());
        }else{
            if(!ObjectUtil.isEmpty(clientEntity.getRooms())){
                client.joinRooms(clientEntity.getRooms());
            }
        }
        clientEntity.setUserId(userId);
        clientEntity.setLastActiveTime(System.currentTimeMillis());
        String remoteAddress = client.getRemoteAddress().toString();
        String ip = client.getHandshakeData().getHttpHeaders().get("X-Real-IP");
        if(!ObjectUtil.isEmpty(ip)){
            clientEntity.setIp(ip);
        }else if(!ObjectUtil.isEmpty(remoteAddress)){
            if(remoteAddress.indexOf("[")>-1){
                clientEntity.setIp(remoteAddress.substring(remoteAddress.indexOf("[")+1, remoteAddress.indexOf("]")));
            }else if(remoteAddress.contains(":")){
                clientEntity.setIp(remoteAddress.substring(1, remoteAddress.indexOf(":")));
            }else{
                clientEntity.setIp(remoteAddress);
            }
        }
        redisService.put(connectionKey, clientEntity, Duration.ofMillis(wsProperties.getSessionTimeout()));

        // 存储用户-连接映射
        String userConnectionsKey = String.format(USER_KEY_PREFIX, userId);
        redisService.putSet(userConnectionsKey, sessionId);
        redisService.setExpire(userConnectionsKey, Duration.ofMillis(wsProperties.getSessionTimeout()));
    }

    /**
     * 根据会话ID获取连接
     * 
     * @param clientId 会话ID
     * @return SocketIOClient
     */
    public SocketIOClient get(String clientId) {
        return localConnectionMap.get(clientId);
    }

    /**
     * 移除连接
     * 
     * @param client SocketIOClient
     */
    public void remove(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();

        // 从本地映射中移除
        localConnectionMap.remove(sessionId);

        // 从本地用户连接映射中移除
        Long userId = (Long) client.get(WsConstants.CONNECT_ATTR_USER_ID);
        if (userId != null) {
            List<SocketIOClient> clientList = localUserConnectionMap.get(userId);
            if (clientList != null) {
                clientList.remove(client);
                if (clientList.isEmpty()) {
                    localUserConnectionMap.remove(userId);
                }
            }

            // 从Redis中移除
            String connectionKey = String.format(CLIENT_KEY_PREFIX, sessionId);
            redisService.delete(connectionKey);

            String userConnectionsKey = String.format(USER_KEY_PREFIX, userId);
            redisService.deleteSetValue(userConnectionsKey, sessionId);
        }
    }

    /**
     * 获取本地用户的所有连接
     * 
     * @param userId 用户ID
     * @return 连接列表
     */
    public List<SocketIOClient> getLocal(Long userId) {
        return localUserConnectionMap.getOrDefault(userId, Collections.emptyList());
    }

    /**
     * 获取所有本地连接
     * 
     * @return 连接列表
     */
    public List<SocketIOClient> getLocal() {
        return new ArrayList<>(localConnectionMap.values());
    }

    public void updateLastActiveTime(SocketIOClient client) {
        client.set(WsConstants.CONNECT_ATTR_LAST_ACTIVE_TIME, System.currentTimeMillis());
    }

    /**
     * 检查用户是否在线（在任何节点）
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isOnline(Long userId) {
        String userConnectionsKey = String.format(USER_KEY_PREFIX, userId);
        Set<String> sessionIds = redisService.getSet(userConnectionsKey);
        return !ObjectUtil.isEmpty(sessionIds);
    }

    /**
     * 获取用户ID
     * 
     * @param client SocketIOClient
     * @return 用户ID
     */
    public Long getUserId(SocketIOClient client) {
        return client.get(WsConstants.CONNECT_ATTR_USER_ID);
    }

    /**
     * 获取连接数量
     * 
     * @return 连接数量
     */
    public int getClientCount() {
        return localConnectionMap.size();
    }

    /**
     * 获取用户数量
     * 
     * @return 用户数量
     */
    public int getUserCount() {
        return localUserConnectionMap.size();
    }

    /**
     * 清理过期连接
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        List<SocketIOClient> toRemove = new ArrayList<>();

        for (SocketIOClient client : localConnectionMap.values()) {
            Long lastActiveTime = (Long) client.get(WsConstants.CONNECT_ATTR_LAST_ACTIVE_TIME);
            if (lastActiveTime != null && (now - lastActiveTime) > wsProperties.getSessionTimeout()) {
                toRemove.add(client);
            }
        }

        for (SocketIOClient client : toRemove) {
            remove(client);
            client.disconnect();
        }
    }

}