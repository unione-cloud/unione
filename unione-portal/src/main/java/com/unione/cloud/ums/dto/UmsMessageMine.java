package com.unione.cloud.ums.dto;

import java.util.Date;

import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.ums.model.UmsMessage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SqlResource("ums.UmsMessageMine")
public class UmsMessageMine extends UmsMessage{

	@Schema(title="我的消息id",description="ums_message_status id")
	private Long mineId;
	@Schema(title="查阅状态，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer viewSts;
	@Schema(title="查阅时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date viewTime;
	@Schema(title="回复内容",description="长度为：500")
	private String replyInfo;
	@Schema(title="消息确认状态 0:未确认 1:已确认",description="长度为：10")
	private Integer confirmStatus;
	@Schema(title="确认结果,1：接收，2：拒绝",description="长度为：10")
	private Integer confirmResult;
	@Schema(title="确认时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date confirmDate;

	@Schema(title="开始时间",description="消息发送开始时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeBegine;
	@Schema(title="截止时间",description="消息发送截止时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeEnd;

}
