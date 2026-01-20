package com.unione.cloud.ws.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "WS节点信息")
public class WsNodeEntity {

    @Schema(title = "节点id")
    private String id;

    @Schema(title = "主机名")
    private String hostname;

    @Schema(title = "端口号")
    private int    port;

    @Schema(title = "注册时间")
    private long registeTime;

    @Schema(title = "最后心跳时间")
    private long lastHeartbeat;

    @Schema(title = "连接数")
    private int clientCount;

    @Schema(title = "用户数")
    private int userCount;

}
