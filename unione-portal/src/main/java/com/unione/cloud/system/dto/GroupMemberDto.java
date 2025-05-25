package com.unione.cloud.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.common.dto.SelectorUserDto;
import com.unione.cloud.system.model.SysGroupMember;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_group_member")
@SqlResource("system.groupMemberDto")
public class GroupMemberDto extends SysGroupMember{

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

	@Schema(title="用户列表",description="批量保存用户")
	private List<SelectorUserDto> users;
}
