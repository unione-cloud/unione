package com.unione.cloud.base.model;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 界面语言包。
 */
@Data
@Builder
@DataPermis
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "base_i18n_bundle")
@Schema(title = "界面语言包")
public class BaseI18nBundle extends Pojo {

    private static final long serialVersionUID = 1L;

    @Schema(title = "是否全局语言包，1是，0否")
    private Integer isGlobal;

    @Schema(title = "关联的全局语言包ID，租户个性化包必填")
    private Long globalBundleId;

    @KeyWords
    @Schema(title = "语言包编码")
    private String bundleCode;

    @KeyWords
    @Schema(title = "语言包名称")
    private String bundleName;

    @Schema(title = "适用端，多个值以逗号分隔")
    private String clientScopes;

    @Schema(title = "是否默认语言，1是，0否")
    private Integer defaultLocale;

    @Schema(title = "当前发布版本ID")
    private Long currentReleaseId;

    @Schema(title = "说明")
    private String descs;

    @Schema(title = "状态，1启用，0停用")
    private Integer status;
}
