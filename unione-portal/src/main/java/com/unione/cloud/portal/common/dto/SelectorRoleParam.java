package com.unione.cloud.portal.common.dto;

import java.util.List;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="用户选择查询对象")
@SqlResource("system.selector.roleSelectorDto")
public class SelectorRoleParam extends SelectorParam{

    @Schema(title="角色类型",description="小于等于0:全部,大于0:指定类型")
    private Integer rtype;

    @JsonIgnore
    @Schema(title="主键集合")
    private List<Long> ids;

}
