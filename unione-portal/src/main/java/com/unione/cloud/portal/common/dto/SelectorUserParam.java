package com.unione.cloud.portal.common.dto;

import java.util.List;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@SqlResource("system.selector.userSelectorDto")
public class SelectorUserParam extends SelectorParam{
    
    @Schema(title="节点类型",description="organ：机构，role：角色，group：分组，post：岗位，user：用户,permis:权限")
    private String ntype;
    
    @JsonIgnore
    @Schema(title="主键集合")
    private List<Long> ids;
}
