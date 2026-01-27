package com.unione.cloud.ws.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class WsEvent implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件名称
     */
    private String event;

    private Long ackId;

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

    public static WsEvent build(String name, Object data){
        WsEvent event=new WsEvent();
        event.setEvent(name);
        event.setData(data);
        return event;
    }

}
