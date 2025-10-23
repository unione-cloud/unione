package com.unione.cloud.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="模版变量",description="长度为：65535")
public class UmsTmplVar {
    /**
     * 变量名称
     */
    @Schema(title="变量名称",description="长度为：50")
    private String name;
    /**
     * 变量标题
     */
    @Schema(title="变量标题",description="长度为：100")
    private String title;
    /**
     * 变量数据类型
     */
    @Schema(title="变量数据类型",description="java 数据类型,如：String,Integer,Boolean等")
    private String dataType;
    /**
     * 变量默认值
     */
    @Schema(title="变量默认值",description="长度为：100")
    private String defaultValue;

    /**
     * 是否必填
     */
    @Schema(title="是否必填",description="长度为：10")
    private Boolean required;
}
