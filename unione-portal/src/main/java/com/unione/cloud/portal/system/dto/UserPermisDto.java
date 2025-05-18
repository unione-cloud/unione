package com.unione.cloud.portal.system.dto;

import java.util.List;
import java.util.ArrayList;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.portal.system.model.SysUserPermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Table(name="sys_user_permis")
@SqlResource("system.userPermisDto")
public class UserPermisDto extends SysUserPermis{

    @Schema(title="机构名称")
	private String orgName;

    @Schema(title="登录帐号",description="长度为：100")
	private String username;

	@Schema(title="真实姓名",description="长度为：50")
	private String realName;

    @Schema(title="用户类型，字典USERTYPE 1管理员，2普通用户，9其他",description="长度为：10")
	private Integer userType;

    @Schema(title="用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定",description="长度为：10")
	private Integer status;

	@Schema(title="新增资源权限列表",description="")
	private List<SysUserPermis> addPermis=new ArrayList<>();

	@Schema(title="删除资源权限列表",description="")
	private List<Long> delPermis=new ArrayList<>();

	@Schema(title="修改资源权限列表",description="")
	private List<SysUserPermis> editPermis=new ArrayList<>();
}
