package com.unione.cloud.system.dto;

import org.beetl.sql.annotation.entity.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.unione.cloud.system.model.SysTenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_tenant")
public class TenantInfoDto extends SysTenant{

    @JsonProperty(access = Access.WRITE_ONLY)
    @Schema(title = "登录密码", description = "租户管理员登录密码")
    private String password;

    @Schema(title = "角色列表", description = "租户管理员角色列表")
    private String roleList;


}
