package com.unione.cloud.ws.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "WS消息分发数据")
public class WsDistbuteEntity {

    @Schema(title = "节点ID")
    private String nodeId;
    
    @Schema(title = "用户ID")
    private Long userId;

    @Schema(title = "房间ID")
    private String roomId;

    @Schema(title = "用户ID列表")
    private List<Long> userIds;

    @Schema(title = "数据")
    private Object data;

}
