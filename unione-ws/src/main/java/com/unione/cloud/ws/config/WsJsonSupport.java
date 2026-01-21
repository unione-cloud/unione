package com.unione.cloud.ws.config;

import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WsJsonSupport extends JacksonJsonSupport{

    public WsJsonSupport(){
        initObjectMapper(this.objectMapper);
    }

    @Override
    protected void init(ObjectMapper objectMapper) {
        super.init(objectMapper);
        initObjectMapper(objectMapper);
    }
    
    public void initObjectMapper(ObjectMapper objectMapper){
        objectMapper.configOverride(Long.class).setFormat(
            JsonFormat.Value.forShape(JsonFormat.Shape.STRING)
        );
    }

}
