package com.unione.cloud.system.dto;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.system.model.SysUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_user")
@SqlResource("system.userDto")
public class UserInfoDto extends SysUser{

    @Schema(title="机构名称")
	private String orgName;

    
}
