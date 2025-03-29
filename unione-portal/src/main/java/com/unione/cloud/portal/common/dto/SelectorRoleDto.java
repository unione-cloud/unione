package com.unione.cloud.portal.common.dto;

import java.io.Serializable;

import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUser Dto
 * @描述	用户查询Dto
 * @作者	Unione Cloud
 * @日期	2024-11-22 23:32:02
 */
@Data
@Schema(title="角色选择Dto")
@SqlResource("system.roleSelectorDto")
public class SelectorRoleDto extends TreeNodeDto{
    private static final long serialVersionUID = 1L;

    @Schema(title="角色编码",description="")
    private String sn;

    @Schema(title="角色类型",description="")
    private Integer rtype;

    @Schema(title="角色描述",description="")
	private String descs;

    @Schema(title="是否可传递授权，1是，0否",description="用户角色分配时，是否可传递授权，1是，0否")
	private Integer enDilivery;
    
}
