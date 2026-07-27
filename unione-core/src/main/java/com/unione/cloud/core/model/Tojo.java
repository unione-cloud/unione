package com.unione.cloud.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title="系统树形结构持久化对象基类")
public class Tojo extends Pojo{

    @JsonProperty("pid")
    @Schema(title="父节点ID")
    private Long parentId;

	@Schema(title="所在层级",description="长度为：10")
	private Integer level;
	
	@Schema(title="层级编码",description="长度为：20")
	private String lvsn;

	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;

    @Schema(title="是否叶子节点",description="长度为：10")
    private Integer isLeaf;

    @Schema(title="子节点列表")
    private List<Tojo> children;


}
