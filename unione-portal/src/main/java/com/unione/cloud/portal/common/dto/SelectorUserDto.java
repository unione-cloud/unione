package com.unione.cloud.portal.common.dto;

import java.io.Serializable;

import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUser Dto
 * @描述	用户查询Dto
 * @作者	Unione Cloud
 * @日期	2024-11-22 23:32:02
 */
@Data
@Schema(title="用户选择Dto")
@SqlResource("system.userSelectorDto")
public class SelectorUserDto extends TreeNodeDto{
    private static final long serialVersionUID = 1L;

    @Schema(title="用户数量",description="当前节点，包括的用户数量")
    private Integer userCount;

    @Schema(title="机构ID",description="长度为：10")
    private Long orgId;

    @Schema(title="机构名称",description="")
    private String orgName;

    @Schema(title="用户帐号",description="长度为：100")
	private String username;

    @Schema(title="用户头像",description="长度为：300")
	private String avatar;

    @Schema(title="用户性别，字典SEX 1女，2男",description="长度为：10")
    private Integer sex;

    @Schema(title="用户联系电话",description="长度为：30")
    private String tel;

    @Schema(title="用户邮箱",description="长度为：200")
	private String email;

    @Schema(title="是否可传递授权，1是，0否",description="角色授权，添加用户时，是否可传递授权，1是，0否")
	private Integer enDilivery;
    
}
