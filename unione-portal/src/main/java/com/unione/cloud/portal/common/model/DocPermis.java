package com.unione.cloud.portal.common.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	DocPermis Entity
 * @描述	文档管理：文档权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-11-22 23:32:02
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.DocPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="doc_permis")
public class DocPermis extends Pojo {
	/**
	* 文档ID
	*/
	@ApiModelProperty(value="文档ID",notes="长度为：19")
	private Long fileId;
	/**
	* 文件标题
	*/
	@ApiModelProperty(value="文件标题",notes="长度为：400")
	private String fileTitle;
	/**
	* 文件名称
	*/
	@ApiModelProperty(value="文件名称",notes="长度为：50")
	private String fileName;
	/**
	* 文件类型
	*/
	@ApiModelProperty(value="文件类型",notes="长度为：10")
	private String fileType;
	/**
	* 权限集合，[view,dowland]
	*/
	@ApiModelProperty(value="权限集合，[view,dowland]",notes="长度为：200")
	private String list;
	/**
	* 权限拥有者类别,字典DOCPERMISTYPE user：用户，role：角色，org：机构，public：公开
	*/
	@ApiModelProperty(value="权限拥有者类别,字典DOCPERMISTYPE user：用户，role：角色，org：机构，public：公开",notes="长度为：20")
	private String ownerType;
	/**
	* 权限拥有者ID
	*/
	@ApiModelProperty(value="权限拥有者ID",notes="长度为：19")
	private Long ownerId;
	/**
	* 权限拥有者标题
	*/
	@ApiModelProperty(value="权限拥有者标题",notes="长度为：200")
	private String ownerTitle;
	/**
	* 审核类型，字典DOCAUDITTYPE 1公开审核，2共享审核
	*/
	@ApiModelProperty(value="审核类型，字典DOCAUDITTYPE 1公开审核，2共享审核",notes="长度为：10")
	private Integer auditType;
	/**
	* 审核人员ID
	*/
	@ApiModelProperty(value="审核人员ID",notes="长度为：19")
	private Long auditUserid;
	/**
	* 审核人员姓名
	*/
	@ApiModelProperty(value="审核人员姓名",notes="长度为：64")
	private String auditUsername;
	/**
	* 审核结果 字典DOCFILEAUDITSTS 1待审，2通过，3拒绝
	*/
	@ApiModelProperty(value="审核结果 字典DOCFILEAUDITSTS 1待审，2通过，3拒绝",notes="长度为：10")
	private Integer auditResult;
	/**
	* 审核说明
	*/
	@ApiModelProperty(value="审核说明",notes="长度为：400")
	private String auditOpinion;
	/**
	* 审核时间
	*/
	@ApiModelProperty(value="审核时间",notes="长度为：19")
	private Date auditTime;

}
