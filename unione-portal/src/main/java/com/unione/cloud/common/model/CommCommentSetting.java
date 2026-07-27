package com.unione.cloud.common.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	CommCommentSetting Entity
 * @描述	通用：评论配置，具体目标个性化设置，如，是否关闭评论，是否开启点赞等等
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-27 17:04:51
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("common.CommCommentSetting")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="comm_comment_setting")
public class CommCommentSetting extends Pojo {
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
	@NotEmpty(message = "目标ID不能为空",groups = {Validator.save.class})
	private Long targetId;
	/**
	* 是否关闭评论，字典TUREORFALSE 1是，0否，默认开启
	*/
	@Schema(title="是否关闭评论，字典TUREORFALSE 1是，0否，默认开启",description="长度为：10")
	private Integer closeFlag;
	/**
	* 引用评论层级，默认4级
	*/
	@Schema(title="引用评论层级，默认4级",description="长度为：10")
	private Integer refLevel;
	/**
	* 是否同步加载，字典TUREORFALSE 1是，0否，默认同步
	*/
	@Schema(title="是否同步加载，字典TUREORFALSE 1是，0否，默认同步",description="长度为：10")
	private Integer asyncFlag;
	/**
	* 是否开启点赞，字典TUREORFALSE 1是，0否，默认开启
	*/
	@Schema(title="是否开启点赞，字典TUREORFALSE 1是，0否，默认开启",description="长度为：10")
	private Integer likeEnable;
	/**
	* 是否开启附件，字典TUREORFALSE 1是，0否，默认开启
	*/
	@Schema(title="是否开启附件，字典TUREORFALSE 1是，0否，默认开启",description="长度为：10")
	private Integer fileEnable;
	/**
	* 附件数量，默认2个
	*/
	@Schema(title="附件数量，默认2个",description="长度为：10")
	private Integer fileCount;
	/**
	* 附件类型，多个','号分隔，默认所有
	*/
	@Schema(title="附件类型，多个','号分隔，默认所有",description="长度为：50")
	private String fileType;

}
