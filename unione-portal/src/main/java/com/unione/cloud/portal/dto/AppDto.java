package com.unione.cloud.portal.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AppDto {
	@Schema(title="应用id",description="")
	private Long id;
	@Schema(title="应用名称",description="长度为：100")
	private String name;
	@Schema(title="应用编码，唯一",description="长度为：50")
	private String sn;
	@Schema(title="是否微应用",description="长度为：10")
	private Integer isMp;
	@Schema(title="应用URL",description="长度为：250")
	private String url;
	@Schema(title="首页URL",description="长度为：250")
	private String welcome;
	@Schema(title="版本号",description="长度为：30")
	private String versNo;
	@Schema(title="版本说明",description="长度为：500")
	private String versDesc;
	@Schema(title="图标（字体图标）",description="长度为：100")
	private String icon;
	@Schema(title="大图标(图片图标)",description="长度为：250")
	private String picMax;
	@Schema(title="中图标(图片图标)",description="长度为：250")
	private String picMid;
	@Schema(title="小图标(图片图标)",description="长度为：250")
	private String picMix;
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	@Schema(title="描述",description="长度为：500")
	private String descs;
	
	@Schema(title="应用菜单列表",description="")
	private List<ResourceDto> menus;
	
	@Schema(title="应用工具列表",description="")
	private List<ResourceDto> tools;
	
}
