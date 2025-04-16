package com.unione.cloud.portal.common.dto;

import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @标题 	SelectorUser Dto
 * @描述	岗位选择Dto
 * @作者	Unione Cloud
 * @日期	2025-04-16 08:32:02
 */
@Data
@Schema(title="岗位选择Dto")
@SqlResource("system.selector.postSelectorDto")
public class SelectorPostDto extends TreeNodeDto{
    private static final long serialVersionUID = 1L;

   
}
