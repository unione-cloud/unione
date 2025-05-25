package com.unione.cloud.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUser Dto
 * @描述	机构选择Dto
 * @作者	Unione Cloud
 * @日期	2025-04-16 08:32:02
 */
@Data
@Schema(title="机构选择Dto")
public class SelectorOrganDto extends TreeNodeDto{
    private static final long serialVersionUID = 1L;

    @Schema(title="机构编码",description="")
    private String sn;

    @Schema(title="机构类型",description="")
    private Integer otype;

    @Schema(title="机构描述",description="")
	private String descs;
}
