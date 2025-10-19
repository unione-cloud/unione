package com.unione.cloud.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.common.dto.TreeNodeDto;
import com.unione.cloud.system.model.SysOnlineDoc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="sys_online_doc")
@Schema(title="在线文档详情")
public class OnlineDocDto extends SysOnlineDoc{

    @Schema(title="文档版本列表")
    private List<DocVersion> versions;

    @Schema(title="文档树")
    private List<TreeNodeDto> docTree;



    @Data
    @Schema(title="文档版本")
    public static class DocVersion{
        @Schema(title="文档版本ID")
        private Long versId;

        @Schema(title="版本号")
        private String versNo;

        @Schema(title="描述")
        private String descs;
    }

}
