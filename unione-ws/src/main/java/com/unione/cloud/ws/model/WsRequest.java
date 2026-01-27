package com.unione.cloud.ws.model;

import java.io.Serializable;

import com.unione.cloud.core.generator.IdGenHolder;

import lombok.Data;

/**
 * WebSocket请求消息
 * 用于客户端向服务端发送请求
 */
@Data
public class WsRequest extends WsEvent{
    
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     */
    private Long id;

    /**
     * 时间戳
     */
    private Long timestamp;

    public WsRequest(){
        this.timestamp = System.currentTimeMillis();
        this.id=IdGenHolder.generate();
    }


}
