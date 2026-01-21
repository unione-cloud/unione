package com.unione.cloud.ws.constants;

/**
 * WebSocket常量类
 * 定义WebSocket相关的常量，如事件名称、错误代码等
 */
public class WsConstants {
    
    // ==================== 事件名称 ====================
    
    /**
     * 连接事件
     */
    public static final String EVENT_CONNECT = "connect";
    
    /**
     * 断开连接事件
     */
    public static final String EVENT_DISCONNECT = "disconnect";
    
    /**
     * 认证事件
     */
    public static final String EVENT_AUTH = "auth";
    
    /**
     * 认证成功事件
     */
    public static final String EVENT_AUTH_SUCCESS = "auth_success";
    
    /**
     * 认证失败事件
     */
    public static final String EVENT_AUTH_FAIL = "auth_fail";
    
    /**
     * 消息事件
     */
    public static final String EVENT_MESSAGE = "message";
    
    /**
     * 响应事件
     */
    public static final String EVENT_RESPONSE = "response";
    
    /**
     * 错误事件
     */
    public static final String EVENT_ERROR = "error";
    
    /**
     * 心跳事件
     */
    public static final String EVENT_PING = "ping";
    
    /**
     * 心跳响应事件
     */
    public static final String EVENT_PONG = "pong";
    
    // ==================== 消息类型 ====================
    
    /**
     * 请求消息类型
     */
    public static final String MESSAGE_TYPE_REQUEST = "request";
    
    /**
     * 响应消息类型
     */
    public static final String MESSAGE_TYPE_RESPONSE = "response";
    
    /**
     * 事件消息类型
     */
    public static final String MESSAGE_TYPE_EVENT = "event";
    
    // ==================== 错误代码 ====================
    
    /**
     * 成功
     */
    public static final int ERROR_CODE_SUCCESS = 200;
    
    /**
     * 认证失败
     */
    public static final int ERROR_CODE_AUTH_FAIL = 1001;
    
    /**
     * 鉴权失败
     */
    public static final int ERROR_CODE_AUTHORIZE_FAIL = 1002;
    
    /**
     * 消息格式错误
     */
    public static final int ERROR_CODE_MESSAGE_FORMAT_ERROR = 1003;
    
    /**
     * 消息处理失败
     */
    public static final int ERROR_CODE_MESSAGE_HANDLE_ERROR = 1004;
    
    /**
     * 连接已关闭
     */
    public static final int ERROR_CODE_CONNECTION_CLOSED = 1005;
    
    /**
     * 服务器内部错误
     */
    public static final int ERROR_CODE_SERVER_ERROR = 1006;
    
    // ==================== 连接属性 ====================
    
    /**
     * 用户ID属性
     */
    public static final String CONNECT_ATTR_USER_ID = "userId";
    
    /**
     * 认证令牌属性
     */
    public static final String CONNECT_ATTR_TOKEN = "token";
    
    /**
     * 连接时间属性
     */
    public static final String CONNECT_ATTR_CONNECT_TIME = "connectTime";
    
    /**
     * 最后活跃时间属性
     */
    public static final String CONNECT_ATTR_LAST_ACTIVE_TIME = "lastActiveTime";
    
    // ==================== 配置键 ====================
    
    /**
     * WebSocket配置前缀
     */
    public static final String CONFIG_PREFIX = "unione.ws";
    
    /**
     * 启用配置键
     */
    public static final String CONFIG_KEY_ENABLED = "enabled";
    
    // ==================== 其他常量 ====================
    
    /**
     * 默认连接超时时间（毫秒）
     */
    public static final long DEFAULT_CONNECTION_TIMEOUT = 300000;
    
    /**
     * 默认心跳间隔（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_INTERVAL = 25000;
    
    /**
     * 最大连接数
     */
    public static final int MAX_CONNECTIONS_PER_USER = 5;

    // ==================== ws redis 消息队列名称 ====================
    public static final String WS_QUEUE_USER_DATA = "unione:wsqueue:user:data";
    public static final String WS_QUEUE_USER_RESPONSE = "unione:wsqueue:response";
    public static final String WS_QUEUE_USER_EVENT = "unione:wsqueue:user";

    public static final String WS_QUEUE_ROOM_DATA = "unione:wsqueue:room:data";
    public static final String WS_QUEUE_ROOM_EVENT = "unione:wsqueue:room:event";
    public static final String WS_QUEUE_ROOM_JOIN = "unione:wsqueue:room:join";
    public static final String WS_QUEUE_ROOM_LEAVE = "unione:wsqueue:room:leave";

    public static final String WS_QUEUE_BROADCAST_EVENT = "unione:wsqueue:broadcast:event";

    
    
}
