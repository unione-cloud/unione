package com.unione.cloud.ws.handler;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.unione.cloud.ws.model.WsEvent;
import com.unione.cloud.ws.model.WsResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket消息处理器:demo
 */
@Slf4j
@Component
public class WsEventHandler extends WsAbstractHandler<WsEvent> {

    @Override
    public String getName() {
       return "event";
    }
   
    @Override
    public void onData(SocketIOClient client, WsEvent event, AckRequest ackSender) throws Exception {
        
        if (!isAuthed(client)) {
            ackSender.sendAckData(WsResponse.error("未认证"));
            return;
        }

        handleEvent(client, event, ackSender);

    }

    public void handleEvent(SocketIOClient client, WsEvent event, AckRequest ackSender) throws Exception {
        log.debug("收到事件:{}", event);
        ackSender.sendAckData(WsResponse.success("收到事件"));
    }

   
}
