package com.unione.cloud.portal.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.portal.common.dto.SelectorUserDto;
import com.unione.cloud.portal.system.model.SysUserOrgan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_user_organ")
@SqlResource("system.userOrganDto")
public class UserOrganDto extends SysUserOrgan {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7139264761199115507L;
	
	@Schema(title="用户机构")
	private String orgName;


	@Schema(title="用户列表",description="批量保存用户")
	private List<SelectorUserDto> users;
	
}
