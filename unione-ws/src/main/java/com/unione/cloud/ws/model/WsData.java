package com.unione.cloud.ws.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class WsData implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据名称
     */
    private String name;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 数据内容
     */
    private Object data;

    public WsData(){
        this.timestamp=System.currentTimeMillis();
    }

}
