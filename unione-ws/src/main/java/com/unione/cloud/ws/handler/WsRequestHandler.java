package com.unione.cloud.ws.handler;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.unione.cloud.ws.model.WsRequest;
import com.unione.cloud.ws.model.WsResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket消息处理器:demo
 */
@Slf4j
@Component
public class WsRequestHandler extends WsAbstractHandler<WsRequest> {

    @Override
    public String getName() {
       return "request";
    }
   
    @Override
    public void onData(SocketIOClient client, WsRequest request, AckRequest ackSender) throws Exception {
        
        if (!isAuthed(client)) {
            ackSender.sendAckData(WsResponse.error("未认证"));
            return;
        }

        handleRequest(client, request, ackSender);

    }

    public void handleRequest(SocketIOClient client, WsRequest request, AckRequest ackSender) throws Exception {
        log.debug("收到请求:{}", request);
        ackSender.sendAckData(WsResponse.success("收到请求"));
    }

   
}
