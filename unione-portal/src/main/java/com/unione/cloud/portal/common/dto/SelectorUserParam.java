package com.unione.cloud.portal.common.dto;

import java.io.Serializable;

import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUserParam
 * @描述	用户选择查询对象
 * @作者	Unione Cloud
 * @日期	2024-11-22 23:32:02
 */
@Data
@Schema(title="用户选择查询对象")
@SqlResource("system.userSelectorDto")
public class SelectorUserParam implements Serializable{
    private static final long serialVersionUID = 1L;

    @Schema(title="上级主键ID",description="长度为：19")
    private Long pid;

    @Schema(title="节点类型",description="organ：机构，role：角色，group：分组，post：岗位，user：用户")
    private String ntype;

    @Schema(title="目标类型",description="organ：机构，role：角色，group：分组，post：岗位")
    private String targetType;

    @Schema(title="目标ID",description="长度为：19")
    private Long targetId;


}
