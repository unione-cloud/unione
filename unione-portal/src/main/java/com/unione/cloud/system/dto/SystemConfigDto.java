package com.unione.cloud.system.dto;

import java.io.Serializable;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "系统配置", description = "系统配置")
public class SystemConfigDto implements Serializable {

    /**
     * 系统欢迎语
     */
    @Schema(title = "系统欢迎语", description = "")
    private String welcome;

       /**
     * 登录配置
     */
    @Schema(title = "登录配置", description = "登录配置")
    private LoginConfigDto login;

    /**
     * logo配置
     */
    @Schema(title = "logo配置", description = "logo配置")
    private LogoDto logo;
    

    @Data
    @Schema(title = "logo配置", description = "logo配置")
    public static class LogoDto implements Serializable {
        /**
         * 系统名称
         */
        @Schema(title = "系统名称", description = "")
        private String text;
        /**
         * 系统缩写
         */
        @Schema(title = "系统缩写", description = "")
        private String side;
        /**
         * logo样式
         */
        @Schema(title = "logo样式", description = "")
        private Map<String, String> css;
    }


    @Data
    @Schema(title = "登录配置", description = "登录配置")
    public static class LoginConfigDto implements Serializable {
        /**
         * 登录欢迎语
         */
        @Schema(title = "登录欢迎语", description = "")
        private String welcome;
        /**
         * 是否开启验证码
         */
        @Schema(title = "是否开启验证码", description = "")
        private Boolean captchaEnabled;
        /**
         * 是否开启短信验证码
         */
        @Schema(title = "是否开启短信验证码", description = "")
        private Boolean smsEnabled;
        /**
         * 登录广告图片
         */
        @Schema(title = "登录广告图片", description = "")
        private String adImage;
    }

}
