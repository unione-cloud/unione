package com.unione.cloud.portal.common.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SelectorParam implements Serializable{

    @Schema(title="上级主键ID",description="长度为：19")
    private Long pid;

    @Schema(title="目标类型",description="organ：机构，role：角色，group：分组，post：岗位")
    private String targetType;

    @Schema(title="目标ID",description="长度为：19")
    private Long targetId;
    
}
