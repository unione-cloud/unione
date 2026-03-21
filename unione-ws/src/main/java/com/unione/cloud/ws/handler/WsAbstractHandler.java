package com.unione.cloud.ws.handler;

import org.springframework.beans.factory.annotation.Autowired;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.ws.WsClientManager;
import com.unione.cloud.ws.WsPushService;
import com.unione.cloud.ws.constants.WsConstants;

import cn.hutool.core.util.ObjectUtil;

public abstract class WsAbstractHandler<T> implements DataListener<T> {
    
    @Autowired
    protected WsClientManager wsClientManager;

    @Autowired
    protected WsPushService wsPushService;

    @Autowired
    private TokenService tokenService;

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
        String token = client.getHandshakeData().getHttpHeaders().get("token");
        if (ObjectUtil.isEmpty(token)) {
            token=client.getHandshakeData().getSingleUrlParam("token");
        }
        if (ObjectUtil.isEmpty(token)) {
            return false;
        }
        UserPrincipal principal = tokenService.toPrincipal(token);
        if (principal == null || principal.getId() == null) {
            return false;
        }
        SessionHolder.setToken(token);
        SessionHolder.setUserPrincipal(principal);
        return true;
    }

}
