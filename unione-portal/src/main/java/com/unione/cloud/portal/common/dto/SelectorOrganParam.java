package com.unione.cloud.portal.common.dto;

import java.util.List;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUserParam
 * @描述	机构选择查询对象
 * @作者	Unione Cloud
 * @日期	2025-04-16 07:32:02
 */
@Data
@Schema(title="机构选择查询对象")
@SqlResource("system.selector.organSelectorDto")
public class SelectorOrganParam extends SelectorParam{
    
    @Schema(title="机构类型",description = "小于等于0：全部，大于0：指定类型")
    private Integer otype;

    @JsonIgnore
    @Schema(title="主键集合")
    private List<Long> ids;

}
