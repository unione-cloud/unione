package com.unione.cloud.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.common.dto.TreeNodeDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title="资源树节点Dto")
public class ResTreeNodeDto extends TreeNodeDto{

    @Schema(title="应用ID",description="长度为：19")
	private Long appId;

    @Schema(title="资源名称/编码，唯一",description="长度为：100")
	private String name;

    @Schema(title="路由path",description="长度为：100")
    private String path;

    @Schema(title="资源别名（授权树区别重复菜单名称）",description="长度为：100")
	private String alias;

    @Schema(title="引用ID",description="长度为：19")
    private Long refId;

    @Schema(title="平台类型",description="pc:PC端，app:移动端")
    private String platform;

    @Schema(title="资源设置,JSON存储{}",description="长度为：1000")
	private String configs;

    @Schema(title="是否可传递授权，1是，0否",description="角色授权，添加用户时，是否可传递授权，1是，0否")
	private Integer enDilivery;

}
