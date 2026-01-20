package com.unione.cloud.ws.handler;

import org.springframework.beans.factory.annotation.Autowired;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.unione.cloud.ws.WsClientManager;
import com.unione.cloud.ws.constants.WsConstants;

public abstract class WsAbstractHandler<T> implements DataListener<T> {
    
    @Autowired
    protected WsClientManager wsClientManager;

    /**
     * 获得处理名称
     * @return
     */
    public abstract String getName();

    /**
     * 判断当前连接是否已认证
     * @param client
     * @return
     */
    protected boolean isAuthed(SocketIOClient client) {
        return client.get(WsConstants.CONNECT_ATTR_USER_ID) != null;
    }

}
