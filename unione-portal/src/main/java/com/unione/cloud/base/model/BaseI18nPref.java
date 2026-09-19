package com.unione.cloud.base.model;

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
 * 用户的界面、内容语言和方言偏好。
 */
@Data
@Builder
@DataPermis
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "base_i18n_pref")
@Schema(title = "用户语言偏好")
public class BaseI18nPref extends Pojo {

    private static final long serialVersionUID = 1L;

    @Schema(title = "界面语言")
    private String interfaceLocale;

    @Schema(title = "内容语言")
    private String contentLocale;

    @Schema(title = "方言偏好")
    private String dialectLocale;
}
