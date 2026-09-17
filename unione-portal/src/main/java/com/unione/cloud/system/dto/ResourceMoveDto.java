package com.unione.cloud.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "资源移动参数")
public class ResourceMoveDto {

    @Schema(title = "资源ID")
    private Long id;

    @Schema(title = "系统ID")
    private Long sysId;

    @Schema(title = "目标父节点ID")
    private Long parentId;

    @Schema(title = "目标顺序")
    private Integer ordered;
}
