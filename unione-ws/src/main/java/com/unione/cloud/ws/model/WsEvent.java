package com.unione.cloud.ws.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class WsEvent implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件名称
     */
    private String name;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 事件数据
     */
    private Object data;

    public WsEvent(){
        this.timestamp=System.currentTimeMillis();
    }

}
