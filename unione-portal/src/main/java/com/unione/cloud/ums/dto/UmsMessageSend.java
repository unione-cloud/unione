package com.unione.cloud.ums.dto;

import java.util.List;

import com.unione.cloud.ums.model.UmsMessage;
import com.unione.cloud.ums.model.UmsMessageTarget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UmsMessageSend extends UmsMessage{

    @Schema(title="通知目标集合",description="")
    private List<UmsMessageTarget> targets;

    @Schema(title="通知方式集合",description="")
    private List<String> ways;

}
