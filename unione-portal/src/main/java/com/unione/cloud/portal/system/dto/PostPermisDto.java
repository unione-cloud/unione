package com.unione.cloud.portal.system.dto;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.portal.system.model.SysPostPermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_post_permis")
@SqlResource("system.postPermisDto")
public class PostPermisDto extends SysPostPermis{

    @Schema(title="机构名称")
	private String orgName;

	@Schema(title="岗位名称",description="长度为：200")
	private String name;
	
	@Schema(title="岗位编码",description="长度为：50")
	private String sn;

    @Schema(title="岗位类型，字典POSTTYPES 9其他",description="长度为：10")
	private Integer types;

    @Schema(title="岗位状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
