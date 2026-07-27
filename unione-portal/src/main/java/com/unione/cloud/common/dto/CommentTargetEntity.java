package com.unione.cloud.common.dto;

import java.util.List;

import com.unione.cloud.common.model.CommCommentItem;
import com.unione.cloud.common.model.CommCommentSetting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommentTargetEntity {

    @Schema(title = "评论设置")
    private CommCommentSetting setting;

    @Schema(title = "评论列表")
    private List<CommCommentItem> items;

}
