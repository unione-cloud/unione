package com.unione.cloud.ws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.ObjectUtil;

/**
 * WebSocket配置属性类
 * 用于从application.yml中读取WebSocket相关配置
 */
@Component
@ConfigurationProperties(prefix = "unione.ws")
public class WsProperties {
    
    /**
     * WebSocket是否启用
     */
    private boolean enabled = true;
    
    /**
     * WebSocket服务端口
     */
    private int port = 6789;
    
    /**
     * WebSocket服务主机名
     */
    private String hostname = "0.0.0.0";
    
    /**
     * 最大帧长度
     */
    private int maxFramePayloadLength = 1024 * 1024;
    
    /**
     * 最大HTTP内容长度
     */
    private int maxHttpContentLength = 1024 * 1024;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int bossThreads = 1;
    
    /**
     * 工作线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * 心跳间隔（毫秒）
     */
    private int pingInterval = 25000;
    
    /**
     * 心跳超时时间（毫秒）
     */
    private int pingTimeout = 60000;
    
    /**
     * 协议升级超时时间（毫秒）
     */
    private int upgradeTimeout = 10000;
    
    
    // ==================== 高可用配置 ====================
    
    /**
     * 高可用是否启用
     */
    private boolean haEnabled = true;
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 会话超时时间（毫秒）
     */
    private long sessionTimeout = 3600000;
    
    /**
     * 连接清理间隔（毫秒）
     */
    private long connectionCleanupInterval = 60000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getMaxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    public void setMaxFramePayloadLength(int maxFramePayloadLength) {
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    public int getMaxHttpContentLength() {
        return maxHttpContentLength;
    }

    public void setMaxHttpContentLength(int maxHttpContentLength) {
        this.maxHttpContentLength = maxHttpContentLength;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public int getUpgradeTimeout() {
        return upgradeTimeout;
    }

    public void setUpgradeTimeout(int upgradeTimeout) {
        this.upgradeTimeout = upgradeTimeout;
    }


    // ==================== 高可用配置 getter/setter ====================
    
    public boolean isHaEnabled() {
        return haEnabled;
    }

    public void setHaEnabled(boolean haEnabled) {
        this.haEnabled = haEnabled;
    }

    public String getNodeId() {
        if(ObjectUtil.isEmpty(nodeId)){
            nodeId = String.format("node-%s-%s",this.hostname,System.currentTimeMillis());
        }
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public long getConnectionCleanupInterval() {
        return connectionCleanupInterval;
    }

    public void setConnectionCleanupInterval(long connectionCleanupInterval) {
        this.connectionCleanupInterval = connectionCleanupInterval;
    }
}
