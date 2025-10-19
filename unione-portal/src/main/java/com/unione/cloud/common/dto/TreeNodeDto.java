package com.unione.cloud.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	TreeNode Dto
 * @描述	树节点Dto
 * @作者	Unione Cloud
 * @日期	2024-11-22 23:32:02
 */
@Data
@Schema(title="树节点Dto")
public class TreeNodeDto implements Serializable{
    private static final long serialVersionUID = 1L;

    @Schema(title="主键ID",description="长度为：19")
    private Long id;

    @Schema(title="上级主键ID",description="长度为：19")
    private Long pid;

    @Schema(title="节点类型",description="eg:organ：机构，role：角色，group：分组，post：岗位，user：用户")
    private String ntype;

    @Schema(title="节点标题",description="长度为：100")
    private String title;

    @Schema(title="图标名称",description="长度为：100")
    private String iconName;

    @Schema(title="是否选中",description="true：选中，false：未选中")
    private Boolean checked;

    @Schema(title="子节点列表")
    private List<TreeNodeDto> children=new ArrayList<>();
}
