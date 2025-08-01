package com.unione.cloud.common.dto;
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
 * @标题 	VisitEntry
 * @描述	通用：访问登记
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class VisitEntry {
	
	@Schema(title="应用ID/站点id",description="长度为：19")
	@NotNull(message="应用ID/站点id不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;

	@Schema(title="目标类型，字典COMMVISITTARGETTYPE menu：菜单页面，site：网站页面",description="长度为：20")
	@NotNull(message="目标类型不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message="目标类型不能为空",groups = {Validator.save.class,Validator.update.class})
	private String targetType;

	@Schema(title="目标标题",description="长度为：200")
	@NotBlank(message="目标标题不能为空",groups = {Validator.save.class,Validator.update.class})
	private String targetTitle;

	@Schema(title="目标ID",description="长度为：19")
	@NotNull(message="目标ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long targetId;

	@Schema(title="目标url",description="长度为：200")
	@NotNull(message="目标url不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message="目标url不能为空",groups = {Validator.save.class,Validator.update.class})
	private String targetUrl;
	
}
