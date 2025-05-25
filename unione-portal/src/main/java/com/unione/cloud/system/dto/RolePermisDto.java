package com.unione.cloud.system.dto;

import java.util.ArrayList;
import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.system.model.SysRolePermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Table(name="sys_role_permis")
@SqlResource("system.rolePermisDto")
public class RolePermisDto extends SysRolePermis{

	@Schema(title="机构名称")
	private String orgName;
   
	@Schema(title="名称",description="长度为：100")
	private String name;
	
	@Schema(title="编码",description="长度为：20")
	private String sn;
	
	@Schema(title="类型，字典ROLETYPE 1平台，2租户，3机构",description="长度为：10")
	private Integer types;
	
	@Schema(title="状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

	@Schema(title="新增资源权限列表",description="")
	private List<SysRolePermis> addPermis=new ArrayList<>();

	@Schema(title="删除资源权限列表",description="")
	private List<Long> delPermis=new ArrayList<>();

	@Schema(title="修改资源权限列表",description="")
	private List<SysRolePermis> editPermis=new ArrayList<>();
}
