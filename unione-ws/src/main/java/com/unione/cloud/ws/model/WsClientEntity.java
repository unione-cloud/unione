package com.unione.cloud.ws.model;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "WS连接客户端信息")
public class WsClientEntity {

    @Schema(title = "节点ID")
    private String nodeId;

    @Schema(title = "客户端ID")
    private String clientId;

    @Schema(title = "用户ID")
    private long userId;

    @Schema(title = "IP地址")
    private String ip;

    @Schema(title = "连接时间")
    private long connectTime;

    @Schema(title = "最后活跃时间")
    private long lastActiveTime;

    @Schema(title = "加入房间列表")
    private Set<String> rooms=new HashSet<>();

}
