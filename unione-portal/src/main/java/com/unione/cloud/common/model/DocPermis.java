package com.unione.cloud.common.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	DocPermis Entity
 * @描述	文档管理：文档权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-11-22 23:32:02
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("common.DocPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="doc_permis")
public class DocPermis extends Pojo {
	/**
	* 文档ID
	*/
	@Schema(title="文档ID",description="长度为：19")
	private Long fileId;
	/**
	* 文件标题
	*/
	@KeyWords
	@Schema(title="文件标题",description="长度为：400")
	private String fileTitle;
	/**
	* 文件名称
	*/
	@Schema(title="文件名称",description="长度为：50")
	private String fileName;
	/**
	* 文件层级编码
	*/
	@Schema(title="文件层级编码",description="长度为：50")
	private String fileLvsn;
	/**
	* 文件类型
	*/
	@Schema(title="文件类型",description="长度为：10")
	private String fileType;
	/**
	* 权限集合,字典：DOCPERMISLIST:view-查看,dowland-下载，edit：编辑
	*/
	@Schema(title="权限集合,字典：DOCPERMISLIST:view-查看,dowland-下载，edit：编辑",description="长度为：200")
	private String list;
	/**
	* 权限拥有者类别,字典DOCPERMISTYPE user：用户，role：角色，organ：机构，public：公开
	*/
	@Schema(title="权限拥有者类别,字典DOCPERMISTYPE user：用户，role：角色，organ：机构，public：公开",description="长度为：20")
	private String ownerType;
	/**
	* 权限拥有者ID
	*/
	@Schema(title="权限拥有者ID",description="长度为：19")
	private Long ownerId;
	/**
	* 权限拥有者名称
	*/
	@Schema(title="权限拥有者名称",description="长度为：50")
	private String ownerName;
	/**
	* 权限拥有者标题
	*/
	@KeyWords
	@Schema(title="权限拥有者标题",description="长度为：200")
	private String ownerTitle;
	/**
	* 审核类型，字典DOCAUDITTYPE 1公开审核，2共享审核
	*/
	@Schema(title="审核类型，字典DOCAUDITTYPE 1公开审核，2共享审核",description="长度为：10")
	private Integer auditType;
	/**
	* 审核人员ID
	*/
	@Schema(title="审核人员ID",description="长度为：19")
	private Long auditUserid;
	/**
	* 审核人员姓名
	*/
	@Schema(title="审核人员姓名",description="长度为：64")
	private String auditUsername;
	/**
	* 审核结果 字典DOCFILEAUDITSTS 1待审，2通过，3拒绝
	*/
	@Schema(title="审核结果 字典DOCFILEAUDITSTS 1待审，2通过，3拒绝",description="长度为：10")
	private Integer auditResult;
	/**
	* 审核说明
	*/
	@Schema(title="审核说明",description="长度为：400")
	private String auditOpinion;
	/**
	* 审核时间
	*/
	@Schema(title="审核时间",description="长度为：19")
	private Date auditTime;

}
