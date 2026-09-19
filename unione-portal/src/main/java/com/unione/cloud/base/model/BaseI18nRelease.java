package com.unione.cloud.base.model;

import java.util.Date;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 界面语言包不可变发布版本。
 */
@Data
@Builder
@DataPermis
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "base_i18n_release")
@Schema(title = "界面语言包发布版本")
public class BaseI18nRelease extends Pojo {

    private static final long serialVersionUID = 1L;

    @Schema(title = "语言包ID")
    private Long bundleId;

    @Schema(title = "版本号")
    private Integer versionNo;

    @Schema(title = "版本说明")
    private String versionDesc;

    @Schema(title = "发布快照JSON")
    private String snapshotData;

    @Schema(title = "快照SHA-256校验值")
    private String checksum;

    @Schema(title = "回滚来源版本ID")
    private Long rollbackFromId;

    @Schema(title = "发布时间")
    private Date released;

    @Schema(title = "发布人")
    private Long releasedBy;
}
