package com.unione.cloud.portal.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.portal.common.dto.SelectorRoleDto;
import com.unione.cloud.portal.common.dto.SelectorUserDto;
import com.unione.cloud.portal.system.model.SysUserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_user_role")
@SqlResource("system.userRoleDto")
public class UserRoleDto extends SysUserRole {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7139264761199115507L;

	@Schema(title="角色名称")
	private String roleName;
	
	@Schema(title="角色编码")
	private String roleSn;
	
	@Schema(title="角色描述")
	private String roleDescs;
	
	@Schema(title="角色状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer roleStatus;
	
	@Schema(title="用户帐号")
	private String username;
	
	@Schema(title="用户姓名")
	private String realName;
	
	@Schema(title="性别，字典SEX 1女，2男",description="长度为：10")
	private Integer sex;
	
	@Schema(title="用户头像")
	private String avatar;
	
	@Schema(title="机构名称")
	private String orgName;
	
	@Schema(title="用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定",description="长度为：10")
	private Integer status;

	@Schema(title="用户列表",description="批量添加用户,参数roleId不能为空")
	private List<SelectorUserDto> users;

	@Schema(title="用户列表",description="批量分配角色，参数userId不能为空")
	private List<SelectorRoleDto> roles;
}
