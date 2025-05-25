package com.unione.cloud.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.system.model.SysGroupPermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_group_permis")
@SqlResource("system.groupPermisDto")
public class GroupPermisDto extends SysGroupPermis{

	@Schema(title="机构名称")
	private String orgName;

    @Schema(title="分组名称",description="长度为：200")
	private String name;
	
	@Schema(title="分组编码",description="长度为：30")
	private String sn;
		
	@Schema(title="分组类型，字典GROUPTYPES 1用户分组，9其他",description="长度为：10")
	private Integer types;

    @Schema(title="分组状态，字典UGROUPSTATUS 1正常，2解散",description="长度为：10")
	private Integer status;

	@Schema(title="新增资源权限列表",description="")
	private List<SysGroupPermis> addPermis;

	@Schema(title="删除资源权限列表",description="")
	private List<Long> delPermis;

	@Schema(title="修改资源权限列表",description="")
	private List<SysGroupPermis> editPermis;
}
