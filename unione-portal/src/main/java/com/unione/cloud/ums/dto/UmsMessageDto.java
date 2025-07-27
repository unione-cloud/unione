package com.unione.cloud.ums.dto;

import java.util.List;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.ums.model.UmsMessage;
import com.unione.cloud.ums.model.UmsMessageTarget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@SqlResource("ums.UmsMessage")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UmsMessageDto extends UmsMessage{

    @Schema(title="消息分类名称",description="")
	private String categoryName;

    @Schema(title="通知目标集合",description="")
    private List<UmsMessageTarget> targets;

    @Schema(title="通知方式集合",description="")
    private List<String> ways;

}
