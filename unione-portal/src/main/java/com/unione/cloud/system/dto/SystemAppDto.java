package com.unione.cloud.system.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统应用")
public class SystemAppDto implements Serializable {
    /**
     * 主键
     */
    @Schema(title = "主键", description = "长度为：10")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(title = "应用名称", description = "长度为：100")
    private String name;

    /**
     * 应用标识
     */
    @Schema(title = "应用标识", description = "长度为：100")
    private String sn;
}
