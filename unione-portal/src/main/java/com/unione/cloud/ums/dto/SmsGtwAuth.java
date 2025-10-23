package com.unione.cloud.ums.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "短信网关认证参数")
public class SmsGtwAuth {
    @Schema(title = "认证类型",description = "认证类型，uname:用户名密码认证，token:token认证,api:接口认证")
    private String type;

    @Schema(title = "用户名",description = "用户名")
    private String username;

    @Schema(title = "密码",description = "密码")
    private String password;
    
    @Schema(title = "token",description = "token")
    private String token;

}
