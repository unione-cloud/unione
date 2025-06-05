package com.unione.cloud.system.dto;

import com.unione.cloud.common.dto.TreeNodeDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="系统配置节点Dto")
public class SysConfigNodeDto extends TreeNodeDto{

    @Schema(title="配置编码",description="长度为：250")
	private String sn;

	@Schema(title="默认值",description="长度为：1000")
	private String valueDefault;
	
	@Schema(title="设置值",description="长度为：1000")
	private String valueUsed;

    @Schema(title="说明",description="长度为：200")
	private String descs;
}
