package com.unione.cloud.portal.system.dto;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.portal.system.model.SysCodeLvsn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_code_lvsn")
public class CodeLvsnDto extends SysCodeLvsn {


    @Schema(name = "缓存key")
    private String cacheKey;

}
