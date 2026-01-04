package com.unione.cloud.common.dto;

import java.util.ArrayList;
import java.util.List;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.common.model.DocPermis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SqlResource("common.DocFileDto")
public class DocFileDto extends DocFile{
	
	@Schema(title="文档权限集合")
	private List<DocPermis> permis=new ArrayList<>();

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(title="文档拥有者",description="如果是文档拥有则，则为true否则为false")
	private boolean isOwner;

	@JsonIgnore
	@Schema(title="附件ID集合")
	private List<Long> ids;

	
	@JsonIgnore
	private boolean permisEnable;						// 文档查询权限开关
	@JsonIgnore
	private Long permisUser;							// 文档权限查询，当前查询用户ID
	@JsonIgnore
	private Long permisOrg;								// 文档权限查询，当前查询用户所属机构ID

	@JsonIgnore
	private List<String> permisTypes=new ArrayList<>();	// 文档权限验证类型 view,download,edit
	@JsonIgnore
	private List<Long> permisOwners=new ArrayList<>();	// 文档权限查询，权限归属ID集合
	@JsonIgnore
	private List<String> permisRoles=new ArrayList<>();	// 文档权限查询，权限角色集合

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Schema(title="包含types集合",description="通过types进行文件类型过滤")
	private List<String> incTypes=new ArrayList<>();
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Schema(title="不包含types集合",description="通过types进行文件类型过滤")
	private List<String> ninTypes=new ArrayList<>();

}
