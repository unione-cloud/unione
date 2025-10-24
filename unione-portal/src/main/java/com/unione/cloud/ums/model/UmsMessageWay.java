package com.unione.cloud.ums.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	UmsMessageWay Entity
 * @描述	统一消息：通知方式
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:47
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("ums.UmsMessageWay")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_message_way")
public class UmsMessageWay extends Pojo {
	/**
	* 消息ID
	*/
	@Schema(title="消息ID",description="长度为：19")
	private Long messageId;
	/**
	* 通知方式，字典UMSMESSAGEWAY sms：短信，email：邮件，site：站内信
	*/
	@Schema(title="通知方式，字典UMSMESSAGEWAY sms：短信，email：邮件，site：站内信",description="长度为：10")
	private String way;

}
