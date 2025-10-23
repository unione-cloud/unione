package com.unione.cloud.ums.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name="sms 消息模型")
public class SmsEntity {
	
	@Schema(title="sms网关名称",description="如果为空则使用默认sms服务网关")
	private String gateway;

	@Schema(title="场景编码",required=true,description="")
	private String scene;

	@Schema(title="业务ID",description="")
	private Long bizId;
	
	@Schema(title="模板编码",description="如果该字段不为空则使用模板消息")
	private String tmpl;

	@Schema(title="手机号集合",required=true,description="")
	private List<String> tels=new ArrayList<>();
	
	@Schema(title="消息内容",required=true,description="如果使用了模板，则消息内容也作为模板中的一个变量")
	private String contents;
	
	@Schema(title="消息变量",description="可应用于模板中和消息内容中")
	private Map<String, String> vars=new HashMap<>();
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title="发送时间",description="如果该字段不为空，则为定时发送消息，如果为空则立刻发送！")
	private Date timeSend;
}

