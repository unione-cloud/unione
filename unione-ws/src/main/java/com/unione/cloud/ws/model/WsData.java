package com.unione.cloud.ws.model;

import lombok.Data;

@Data
public class WsData extends WsEvent{
    
    private static final long serialVersionUID = 1L;

    /**
     * 时间戳
     */
    private Long timestamp;

    public WsData(){
        this.timestamp=System.currentTimeMillis();
    }

}
