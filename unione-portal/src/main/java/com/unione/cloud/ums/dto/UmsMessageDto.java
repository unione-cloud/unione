package com.unione.cloud.ums.dto;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.ums.model.UmsMessage;

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

}
