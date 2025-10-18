package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.beetsql.annotation.QueryIgnore.QueryType;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysOnlineDocItem Entity
 * @描述	系统管理：在线文档内容
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-18 18:43:24
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysOnlineDocItem")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_online_doc_item")
public class SysOnlineDocItem extends Pojo {
	/**
	* 应用ID
	*/
	@NotNull(message = "应用ID不能为空",groups = {Validator.save.class})
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 文档ID
	*/
	@NotNull(message = "文档ID不能为空",groups = {Validator.save.class})
	@Schema(title="文档ID",description="长度为：19")
	private Long docId;
	/**
	* 上级ID(根节点为-1)
	*/
	@JsonProperty("pid")
	@Schema(title="上级ID(根节点为-1)",description="长度为：19")
	private Long parentId;
	/**
	* 标题
	*/
	@KeyWords
	@NotNull(message = "标题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class})
	@Schema(title="标题",description="长度为：100")
	private String title;
	/**
	* 图标（字体图标）
	*/
	@Schema(title="图标（字体图标）",description="长度为：100")
	private String icon;
	/**
	* 大图标(图片图标)
	*/
	@Schema(title="大图标(图片图标)",description="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@Schema(title="中图标(图片图标)",description="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@Schema(title="小图标(图片图标)",description="长度为：250")
	private String picMix;
	/**
	* 文档内容
	*/
	@KeyWords
	// @QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="文档内容",description="长度为：2147483647")
	private String contents;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否叶子节点，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
