package com.unione.cloud.portal.common.model;
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
 * @标题 	DocDir Entity
 * @描述	文档管理：文档目录
 * @作者	Unione Cloud CodeGen
 * @日期	2024-11-22 23:32:02
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.DocDir")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="doc_dir")
public class DocDir extends Pojo {
	/**
	* 上级目录ID
	*/
	@Schema(title="上级目录ID",description="长度为：19")
	private Long parentId;
	/**
	* 应用编码
	*/
	@Schema(title="应用编码",description="长度为：20")
	private String appCode;
	/**
	* 名称
	*/
	@Schema(title="名称",description="长度为：50")
	private String name;
	/**
	* 目录密码
	*/
	@Schema(title="目录密码",description="长度为：200")
	private String pwd;
	/**
	* 类型 字典DOCDIRTYPE 
	*/
	@Schema(title="类型 字典DOCDIRTYPE ",description="长度为：10")
	private Integer type;
	/**
	* 图标文件url
	*/
	@Schema(title="图标文件url",description="长度为：250")
	private String iconPic;
	/**
	* 业务KEY,在某些业务场景下进行业务隔离
	*/
	@Schema(title="业务KEY,在某些业务场景下进行业务隔离",description="长度为：50")
	private String busyKey;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 显示状态：1:显示,2:隐藏
	*/
	@Schema(title="显示状态：1:显示,2:隐藏",description="长度为：10")
	private Integer isShow;
	/**
	* 公开状态：0:不公开,1:公开
	*/
	@Schema(title="公开状态：0:不公开,1:公开",description="长度为：10")
	private Integer isPublic;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：500")
	private String descs;

}
