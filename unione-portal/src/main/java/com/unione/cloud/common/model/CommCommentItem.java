package com.unione.cloud.common.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Tojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	CommCommentItem Entity
 * @描述	通用：评论明细，通用评论
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-27 13:51:29
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.CommCommentItem")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="comm_comment_item")
public class CommCommentItem extends Tojo {
	/**
	* 目标类型，字典COMMENTTARGET 
	*/
	@Schema(title="目标类型，字典COMMENTTARGET ",description="长度为：20")
	@NotNull(message = "目标类型不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "目标类型不能为空",groups = {Validator.save.class})
	private String targetType;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	@NotNull(message = "目标ID不能为空",groups = {Validator.save.class})
	private Long targetId;
	/**
	* 评论内容
	*/
	@Schema(title="评论内容",description="长度为：2147483647")
	@NotNull(message = "评论内容不能为空",groups = {Validator.save.class})
	@NotEmpty(message = "评论内容不能为空",groups = {Validator.save.class})
	private String contents;
	/**
	* 附件列表
	*/
	@Schema(title="附件列表",description="长度为：65535")
	private String files;
	/**
	* 点赞数量
	*/
	@Schema(title="点赞数量",description="长度为：10")
	private Integer likeCount;
	/**
	* 是否置顶，字典：TRUEORFALSE 1是，0否
	*/
	@Schema(title="是否置顶，字典：TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isTop;
	/**
	* 评论状态，字典COMMENTSTS 1正常，2待审，3拒绝
	*/
	@Schema(title="评论状态，字典COMMENTSTS 1正常，2待审，3拒绝",description="长度为：10")
	private Integer status;
	

}
