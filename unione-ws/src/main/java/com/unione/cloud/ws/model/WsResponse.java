package com.unione.cloud.ws.model;

import java.io.Serializable;

import com.unione.cloud.ws.constants.WsConstants;

import lombok.Data;

/**
 * WebSocket响应消息
 * 用于服务端向客户端发送响应
 */
@Data
public class WsResponse implements Serializable{
    private static final long serialVersionUID = 1L;
    
    /**
     * 代码
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 响应数据
     */
    private Object body;
    
    /**
     * 请求消息ID
     */
    private Long reqId;

    public WsResponse(){
        this.timestamp=System.currentTimeMillis();
    }

    public boolean isSuccess(){
        return code==WsConstants.ERROR_CODE_SUCCESS;
    }

    public static WsResponse error(String message){
        WsResponse response=new WsResponse();
        response.setCode(WsConstants.ERROR_CODE_MESSAGE_HANDLE_ERROR);
        response.setMessage(message);
        return response;
    }

     public static WsResponse error(Long requestId,String message){
        WsResponse response=new WsResponse();
        response.setCode(WsConstants.ERROR_CODE_MESSAGE_HANDLE_ERROR);
        response.setMessage(message);
        response.setReqId(requestId);
        return response;
     }

    public static WsResponse success(Object body){
        WsResponse response=new WsResponse();
        response.setCode(WsConstants.ERROR_CODE_SUCCESS);
        response.setMessage("success");
        response.setBody(body);
        return response;
    }

    public static WsResponse success(Long requestId,Object body){
        WsResponse response=new WsResponse();
        response.setCode(WsConstants.ERROR_CODE_SUCCESS);
        response.setMessage("success");
        response.setBody(body);
        response.setReqId(requestId);
        return response;
    }

}
