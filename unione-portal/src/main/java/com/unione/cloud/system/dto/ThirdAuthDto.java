package com.unione.cloud.system.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "第三方系统认证信息")
public class ThirdAuthDto {

    @Schema(title = "认证URL")
    private String url;

    @Schema(title = "账号")
    private String account;
    @Schema(title = "密码")
    private String password;

    @Schema(title = "令牌")
    private String token;

    @Schema(title = "应用ID")
    private String appId;
    @Schema(title = "应用密钥")
    private String appSecret;
    @Schema(title = "应用密钥")
    private String appKey;

    @Schema(title = "API密钥")
    private String apiKey;
    @Schema(title = "API密钥")
    private String apiSecret;

    @Schema(title = "客户端ID")
    private String clientId;
    @Schema(title = "客户端密钥")
    private String clientSecret;
    @Schema(title = "重定向URI")
    private String redirectUri;
    @Schema(title = "认证类型")
    private String grantType;

    @Schema(title = "其他数据")
    private Map<String,Object> data;

}
