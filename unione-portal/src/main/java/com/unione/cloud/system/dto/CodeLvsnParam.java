
package com.unione.cloud.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CodeLvsnParam {

    @Schema(title = "code tree sn")
    private String sn;

    @Schema(title = "parent code")
    private String parent;

    @Schema(title = "code level")
    private int    lv;

}
