package com.unione.cloud.security.dto;


import java.util.Date;

import lombok.Data;

@Data
public class PrincipalSession {
    /**
     * 会话id
     */
    private String id;
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 机构id
     */
    private Long orgId;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 创建时间：毫秒
     */
    private Date times;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 设备信息
     */
    private String device;
    /**
     * ip地址
     */
    private String ipAddr;
    /**
     * ip城市
     */
    private String ipCity;
}
