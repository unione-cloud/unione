package com.unione.cloud.ws.handler;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.protocol.EngineIOVersion;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketType;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.core.util.SpringCtxUtil;
import com.unione.cloud.ws.WsClientManager;
import com.unione.cloud.ws.model.WsEvent;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
public class UniWebSocketHandler extends AbstractWebSocketHandler {

    @Autowired
    private WsClientManager wsClientManager;

    @SuppressWarnings("rawtypes")
    private Map<String, Class> handlerTypeMap = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private Map<String, WsAbstractHandler> handlerBeanMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        SpringCtxUtil.getBeans(WsAbstractHandler.class).values().forEach(handler -> {
            Type type = handler.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();
            handlerBeanMap.put(handler.getName(), handler);
            handlerTypeMap.put(handler.getName(), (Class) types[0]);
        });
    }

    @SuppressWarnings({ "rawtypes", "null", "unchecked" })
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理文本消息
        SocketIOClient client = wsClientManager.get(session.getId());
        if (client != null) {
            WsEvent event = JsonUtil.toBean(WsEvent.class, message.getPayload());
            Packet packet = new Packet(PacketType.EVENT);
            packet.setName(event.getEvent());
            packet.setData(event.getData());
            packet.setAckId(event.getAckId());
            WsAbstractHandler handler = handlerBeanMap.get(packet.getName());
            if (handler != null) {
                Class mtype = handlerTypeMap.get(handler.getName());
                Object data = BeanUtils.toBean(event.getData(), mtype);
                AckRequest ackRequest = new AckRequest(packet, client);
                handler.onData(client, data, ackRequest);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SocketIOClient client = wsClientManager.get(session.getId());
        if (client != null) {
            wsClientManager.remove(client);
            client.disconnect();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UniWebSocketClient client = new UniWebSocketClient(session);
        wsClientManager.add(client);
    }

    @Slf4j
    public static class UniWebSocketClient implements SocketIOClient {

        private WebSocketSession session;
        private Set<String> rooms = new HashSet<>();

        public UniWebSocketClient(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void send(Packet packet) {
            try {
                session.sendMessage(new TextMessage(JsonUtil.toJson(packet)));
            } catch (IOException e) {
                log.error("ws send packet error", e);
            }
        }

        @Override
        public void disconnect() {
            try {
                session.close();
            } catch (IOException e) {
                log.error("ws disconnect error", e);
            }
        }

        @Override
        public void sendEvent(String name, Object... data) {
            try {
                Packet packet = new Packet(PacketType.EVENT);
                packet.setName(name);
                packet.setData(data);
                session.sendMessage(new TextMessage(JsonUtil.toJson(packet)));
            } catch (IOException e) {
                log.error("ws send event error", e);
            }
        }

        @Override
        public void set(String key, Object val) {
            session.getAttributes().put(key, val);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(String key) {
            return (T) session.getAttributes().get(key);
        }

        @Override
        public boolean has(String key) {
            return session.getAttributes().containsKey(key);
        }

        @Override
        public void del(String key) {
            session.getAttributes().remove(key);
        }

        @Override
        public HandshakeData getHandshakeData() {
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            session.getHandshakeHeaders().forEach((k, v) -> headers.add(k, v));
            String uri = session.getUri().toString();
            Map<String,List<String>> uriParams=parseQueryString(session.getUri().getQuery());

            InetSocketAddress remote = session.getRemoteAddress();
            InetSocketAddress local = session.getLocalAddress();
            HandshakeData data = new HandshakeData(headers, uriParams, remote, local, uri, true);
            return data;
        }

        private Map<String, List<String>> parseQueryString(String query) {
            Map<String, List<String>> map = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        map.put(keyValue[0], List.of(keyValue[1]));
                    }
                }
            }
            return map;
        }

        @Override
        public Transport getTransport() {
            return Transport.WEBSOCKET;
        }

        @Override
        public EngineIOVersion getEngineIOVersion() {
            return EngineIOVersion.V4;
        }

        @Override
        public boolean isWritable() {
            return session.isOpen();
        }

        @Override
        public void sendEvent(String name, AckCallback<?> ackCallback, Object... data) {
            try {
                Packet packet = new Packet(PacketType.EVENT);
                packet.setName(name);
                packet.setData(data);
                session.sendMessage(new TextMessage(JsonUtil.toJson(packet)));
            } catch (IOException e) {
                log.error("ws send event error", e);
                throw new ServiceException(e);
            }
        }

        @Override
        public void send(Packet packet, AckCallback<?> ackCallback) {
            try {
                session.sendMessage(new TextMessage(JsonUtil.toJson(packet)));
            } catch (IOException e) {
                log.error("ws send packet error", e);
                throw new ServiceException(e);
            }
        }

        @Override
        public SocketIONamespace getNamespace() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getNamespace'");
        }

        @Override
        public UUID getSessionId() {
            return UUID.fromString(session.getId());
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return session.getRemoteAddress();
        }

        @Override
        public boolean isChannelOpen() {
            return session.isOpen();
        }

        @Override
        public void joinRoom(String room) {
            this.rooms.add(room);
        }

        @Override
        public void joinRooms(Set<String> rooms) {
            this.rooms.addAll(rooms);
        }

        @Override
        public void leaveRoom(String room) {
            this.rooms.remove(room);
        }

        @Override
        public void leaveRooms(Set<String> rooms) {
            this.rooms.removeAll(rooms);
        }

        @Override
        public Set<String> getAllRooms() {
            return this.rooms;
        }

        @Override
        public int getCurrentRoomSize(String room) {
            return this.rooms.size();
        }

    }

}
