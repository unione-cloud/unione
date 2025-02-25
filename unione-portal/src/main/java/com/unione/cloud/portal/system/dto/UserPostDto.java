package com.unione.cloud.portal.system.dto;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.portal.system.model.SysUserPost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SqlResource("system.userPostDto")
public class UserPostDto extends SysUserPost{

    @Schema(title="用户帐号")
	private String username;
	
	@Schema(title="用户姓名")
	private String realName;
	
	@Schema(title="性别，字典SEX 1女，2男",description="长度为：10")
	private Integer sex;
	
	@Schema(title="用户头像")
	private String avatar;

	@Schema(title="用户状态",description="长度为：10")
	private Integer userSts;

}
