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
 * 界面语言包草稿条目。
 */
@Data
@Builder
@DataPermis
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "base_i18n_entry")
@Schema(title = "界面语言条目")
public class BaseI18nEntry extends Pojo {

    private static final long serialVersionUID = 1L;

    @Schema(title = "语言包ID")
    private Long bundleId;

    @Schema(title = "语言代码或方言代码")
    private String localeCode;

    @KeyWords
    @Schema(title = "文案键")
    private String entryKey;

    @Schema(title = "文案值")
    private String entryValue;

    @Schema(title = "翻译来源，manual或ai")
    private String sourceType;

    @Schema(title = "修订号")
    private Integer revisionNo;

    @Schema(title = "状态，1启用，0停用")
    private Integer status;
}
