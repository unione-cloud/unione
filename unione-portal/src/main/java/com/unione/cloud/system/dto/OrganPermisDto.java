package com.unione.cloud.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.system.model.SysOrganPermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_organ_permis")
@SqlResource("system.organPermisDto")
public class OrganPermisDto extends SysOrganPermis{

 	@Schema(title="企业/机构名称",description="长度为：250")
	private String name;
	
	@Schema(title="企业/机构别名",description="长度为：250")
	private String alias;
	
	@Schema(title="类型：字典ORGTYPES 1企业，2机构，3部门",description="长度为：10")
	private Integer types;
		
	@Schema(title="机构编码",description="机构编码，不能为空，唯一，长度为：100")
	private String sn;

    @Schema(title="状态",description="长度为：10")
	private Integer status;
    
	@Schema(title="新增资源权限列表",description="")
	private List<SysOrganPermis> addPermis;

	@Schema(title="删除资源权限列表",description="")
	private List<Long> delPermis;

	@Schema(title="修改资源权限列表",description="")
	private List<SysOrganPermis> editPermis;

}
