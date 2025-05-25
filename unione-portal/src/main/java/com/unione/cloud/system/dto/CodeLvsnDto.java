package com.unione.cloud.system.dto;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.system.model.SysCodeLvsn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SqlResource("system.CodeLvsnDto")
@Table(name="sys_code_lvsn")
public class CodeLvsnDto extends SysCodeLvsn{

    @Schema(title="应用名称",description="")
    private String appName;

    @Schema(title="树名称",description="")
    private String treeTitle;

    @Schema(title="租户名称",description="")
    private String tenantName;

    @Schema(title="机构名称",description="")
    private String orgName;

}
