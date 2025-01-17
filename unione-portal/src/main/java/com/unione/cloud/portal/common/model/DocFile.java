package com.unione.cloud.portal.common.model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	DocFile Entity
 * @描述	文档管理：文档信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-11-22 22:53:09
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.DocFile")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="doc_file")
public class DocFile extends Pojo {
	/**
	* 文档目录ID
	*/
	@Schema(title="文档目录ID",description="长度为：19")
	private Long dirId;
	/**
	* 附件所有者ID
	*/
	@Schema(title="附件所有者ID",description="长度为：19")
	private Long ownerId;
	/**
	* 应用编码
	*/
	@Schema(title="应用编码",description="长度为：20")
	private String appCode;
	/**
	* 附件标题
	*/
	@Schema(title="附件标题",description="长度为：400")
	private String title;
	/**
	* 附件名称（表单中的名称）
	*/
	@Schema(title="附件名称（表单中的名称）",description="长度为：50")
	private String name;
	/**
	* 附件大小(字节)
	*/
	@Schema(title="附件大小(字节)",description="长度为：12")
	private Long size;
	/**
	* 附件类型:jpg,doc,png
	*/
	@Schema(title="附件类型:jpg,doc,png",description="长度为：10")
	private String type;
	/**
	* 附件路径
	*/
	@Schema(title="附件路径",description="长度为：250")
	private String path;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 附件状态
	*/
	@Schema(title="附件状态",description="长度为：10")
	private Integer status;
	/**
	* 公开状态：0:不公开,1:公开
	*/
	@Schema(title="公开状态：0:不公开,1:公开",description="长度为：10")
	private Integer isPublic;
	/**
	* 审核状态，公开或共享时的审核状态，字典DOCFILEAUDITSTS 1待审，2通过，3拒绝
	*/
	@Schema(title="审核状态，公开或共享时的审核状态，字典DOCFILEAUDITSTS 1待审，2通过，3拒绝",description="长度为：10")
	private Integer auditStatus;
	/**
	* 扩展信息，json存储{}
	*/
	@Schema(title="扩展信息，json存储{}",description="长度为：4000")
	private String extData;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：500")
	private String descs;

	///////////////////
	//
	
	@JsonIgnore
	public String getRealPath() {
		if(this.isPublic!=null && this.isPublic==1 && this.path!=null) {
			return "/public"+this.path;
		}
		return this.path;
	}
	
	@Default
	@Schema(title="文档权限集合")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private List<DocPermis> permis=new ArrayList<>();
	@Schema(title="文档拥有者",description="如果是文档拥有则，则为true否则为false")
	private boolean isOwner;
	@JsonIgnore
	@Schema(title="共享文档",description="如果设置成true，则查询有分配权限或公开的文档")
	private boolean shared;
	@Schema(title="附件所有者ID集合")
	private List<Long> ownerIds;
	@JsonIgnore
	private boolean incPublic;		// 包含公开文件
	@JsonIgnore
	private boolean incDel;			// 包含删除文件
	@JsonIgnore
	private Long unUserId;			// 非当前用户,过滤掉当前用户
	@JsonIgnore
	private boolean permisEnable;						// 文档查询权限开关
	@JsonIgnore
	private Long permisUser;							// 文档权限查询，当前查询用户ID
	@JsonIgnore
	private Long permisOrg;								// 文档权限查询，当前查询用户所属机构ID
	@JsonIgnore
	@Default
	private List<Long> permisOwners=new ArrayList<>();	// 文档权限查询，权限归属ID集合
	@Default
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Schema(title="包含types集合",description="通过types进行文件类型过滤")
	private List<String> incTypes=new ArrayList<>();
	@Default
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Schema(title="不包含types集合",description="通过types进行文件类型过滤")
	private List<String> ninTypes=new ArrayList<>();
}
