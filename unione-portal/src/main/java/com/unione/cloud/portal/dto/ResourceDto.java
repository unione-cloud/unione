package com.unione.cloud.portal.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResourceDto {
	@Schema(title="资源id",description="")
	private Long id;
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	@JsonProperty("pid")
	@Schema(title="上级菜单ID(根节点为-1)",description="长度为：19")
	private Long parentId;
	@Schema(title="资源名称/编码，唯一",description="长度为：100")
	private String name;
	@Schema(title="资源标题",description="长度为：100")
	private String title;
	@Schema(title="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",description="长度为：20")
	private String types;
	@Schema(title="资源PATH",description="长度为：100")
	private String path;
	@Schema(title="资源URL地址",description="长度为：250")
	private String url;
	@Schema(title="是否iframe打开，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isIframe;
	@Schema(title="是否外部链接，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isExternal;
	@Schema(title="是否隐藏，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isHide;
	@Schema(title="是否叶子节点，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
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
	@Schema(title="状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	@Schema(title="描述",description="长度为：500")
	private String descs;
	@Schema(title="资源设置,JSON存储{}",description="长度为：1000")
	private String configs;
	
	@Schema(title="二级资源列表",description="")
	private List<ResourceDto> children=new ArrayList<>();
}
