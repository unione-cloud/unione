package com.unione.cloud.ws;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.ws.config.WsProperties;
import com.unione.cloud.ws.model.WsNodeEntity;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket健康检查服务
 * 监控节点健康状态和连接状态
 */
@Slf4j
@Service
public class WsHealthChecker {
    
    @Autowired
    private WsProperties wsProperties;

    @Autowired
    private RedisService redisService;

    @Autowired
    private WsClientManager wsClientManager;

    private WsNodeEntity localNode;
    
    @Autowired(required = false)
    private SocketIOServer socketIOServer;

    private static final String NODE_LIST_KEY = "unione:ws:nodes:list";
    private static final String NODE_INFO_KEY = "unione:ws:nodes:%s";
    private static final String NODE_SYNC_KEY = "unione:ws:node:sync";
    
    /**
     * 服务健康状态
     */
    private final AtomicBoolean isHealthy = new AtomicBoolean(true);
    
    
    @Value("${unione.ws.cache.health-expire:600}")
    private long HEALTH_CACHE_EXPIRE;

    @Value("${unione.ws.cache.node-expire:300}")
    private long CACHE_NODE_TIMEOUT;


    /**
     * 注册节点信息
     */
    @PostConstruct
    public void postConstruct() {
        String nodeId = wsProperties.getNodeId();
        String nodeKey = String.format(NODE_INFO_KEY, nodeId);

        // 初始化节点信息
        this.localNode=new WsNodeEntity();
        this.localNode.setId(nodeId);
        this.localNode.setHostname(wsProperties.getHostname());
        this.localNode.setPort(wsProperties.getPort());
        this.localNode.setRegisteTime(System.currentTimeMillis());
        this.localNode.setLastHeartbeat(System.currentTimeMillis());

        // 添加到节点集合
        redisService.putSet(NODE_LIST_KEY, nodeId);
        redisService.put(nodeKey, this.localNode,Duration.ofSeconds(CACHE_NODE_TIMEOUT));

    }

    /**
     * 获得节点信息
     * @param nodeId
     * @return
     */
    public WsNodeEntity getNode(String nodeId){
        if(wsProperties.getNodeId().equals(nodeId)){
            return this.localNode;
        }
        String nodeKey = String.format(NODE_INFO_KEY, nodeId);
        return redisService.getObj(nodeKey);
    }

    /**
     * 设置节点信息
     * @param node
     */
    public void setNode(WsNodeEntity node){
        String nodeKey = String.format(NODE_INFO_KEY, node.getId());
        node.setLastHeartbeat(System.currentTimeMillis());
        redisService.put(nodeKey, node,Duration.ofSeconds(CACHE_NODE_TIMEOUT));
    }

    /**
     * 删除节点信息
     * @param nodeId
     */
    public void delNode(String nodeId){
        String nodeKey = String.format(NODE_INFO_KEY, nodeId);
        redisService.delete(nodeKey);
        redisService.deleteSetValue(NODE_LIST_KEY, nodeId);
    }

    /**
     * 获取所有节点
     * @return 节点ID列表
     */
    public List<WsNodeEntity> getNodes() {
        Set<String> nodeIds = redisService.getSet(NODE_LIST_KEY);
        List<WsNodeEntity> nodes = new ArrayList<>();
        Set<String> deadNodes = new HashSet<>();
        for (String nodeId : nodeIds) {
            String nodeKey = String.format(NODE_INFO_KEY, nodeId);
            WsNodeEntity node = redisService.getObj(nodeKey);
            if (node != null) {
                nodes.add(node);
            }else{
                deadNodes.add(nodeId);
            }
        }
        // 清理过期节点
        for (String deadNodeId : deadNodes) {
            delNode(deadNodeId);
        }
        return nodes;
    }

    
    /**
     * 执行健康检查
     * 每30秒检查一次
     */
    @Scheduled(cron = "${unione.ws.health-check.cron:0 */1 * * * ?}")
    public void checkHealth() {
        if (socketIOServer == null) {
            return;
        }
        try {
            
            // 检查本地连接状态
            int clientCount = wsClientManager.getClientCount();
            int userCount = wsClientManager.getUserCount();
            
            // 记录本地状态
            this.localNode.setClientCount(clientCount);
            this.localNode.setUserCount(userCount);
            this.setNode(this.localNode);
            
            wsClientManager.cleanExpired();
            
            isHealthy.set(true);
            log.info("健康检查，当前节节点正常，连接数：{}，用户数：{}", clientCount, userCount);

            boolean syncLock = redisService.putIfAbsent(NODE_SYNC_KEY, 1, Duration.ofSeconds(10));
            if(syncLock){
                List<WsNodeEntity> nodes = this.getNodes();
                log.info("健康检查，正常节点数：{}", nodes.size());
            }

        } catch (Exception e) {
        	log.warn("健康检查失败", e);
            isHealthy.set(false);
            this.delNode(wsProperties.getNodeId());
        }
    }
    
    
    /**
     * 获取服务健康状态
     * @return 健康状态
     */
    public boolean isHealthy() {
        return isHealthy.get();
    }
    
}