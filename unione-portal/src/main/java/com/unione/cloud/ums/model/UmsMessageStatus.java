package com.unione.cloud.ums.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	UmsMessageStatus Entity
 * @描述	统一消息：消息状态
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsMessageStatus")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_message_status")
public class UmsMessageStatus extends Pojo {
	/**
	* 消息ID
	*/
	@Schema(title="消息ID",description="长度为：19")
	private Long messageId;
	/**
	* 查阅状态，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="查阅状态，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer viewSts;
	/**
	* 查阅时间
	*/
	@Schema(title="查阅时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date viewTime;
	/**
	* 回复内容
	*/
	@Schema(title="回复内容",description="长度为：500")
	private String replyInfo;
	/**
	* 消息确认状态 0:未确认 1:已确认
	*/
	@Schema(title="消息确认状态 0:未确认 1:已确认",description="长度为：10")
	private Integer confirmStatus;
	/**
	* 确认结果,1：接收，2：拒绝
	*/
	@Schema(title="确认结果,1：接收，2：拒绝",description="长度为：10")
	private Integer confirmResult;
	/**
	* 确认时间
	*/
	@Schema(title="确认时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date confirmDate;
	/**
	* 发送日志
	*/
	@Schema(title="发送日志",description="长度为：65535")
	private String sendLog;

}
