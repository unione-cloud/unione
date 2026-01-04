package com.unione.cloud.common.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.QueryAction;
import com.unione.cloud.beetsql.builder.SqlAction;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@DataPermis
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
	* 层级编码：自动生成
	*/
	@QueryAction(SqlAction.LIKER)
	@Schema(title="层级编码",description="层级编码：自动生成，长度为：100")
	private String lvSn;
	/**
	* 所在层级
	*/
	@Schema(title="所在层级",description="长度为：10")
	private Integer lvNo;
	/**
	* 附件大小(字节)
	*/
	@Schema(title="附件大小(字节)",description="长度为：12")
	private Long size;
	/**
	* 附件类型:jpg,doc,png,dir:文件夹
	*/
	@Schema(title="附件类型:jpg,doc,png,dir:文件夹",description="长度为：10")
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
	* 共享状态，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="共享状态，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isShare;
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
	
}
