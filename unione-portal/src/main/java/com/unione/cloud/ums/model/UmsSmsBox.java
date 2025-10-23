package com.unione.cloud.ums.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	UmsSmsBox Entity
 * @描述	统一消息:短信箱
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-22 12:52:46
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsSmsBox")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_sms_box")
public class UmsSmsBox extends Pojo {
	/**
	* 网关ID
	*/
	@Schema(title="网关ID",description="长度为：19")
	private Long gtwId;
	/**
	* 模版ID
	*/
	@Schema(title="模版ID",description="长度为：19")
	private Long tmplId;
	/**
	* 回复消息ID
	*/
	@Schema(title="回复消息ID",description="长度为：19")
	private Long replyMsgId;
	/**
	* 批次ID，短信批量发送批次id
	*/
	@Schema(title="批次ID，短信批量发送批次id",description="长度为：19")
	private Long batchId;
	/**
	* 场景编码，字典SMSSCENE login:登录，register：注册
	*/
	@NotNull(message = "场景编码不能为空",groups = {Validator.save.class})
	@NotBlank(message = "场景编码不能为空",groups = {Validator.save.class})
	@Schema(title="场景编码，字典SMSSCENE login:登录，register：注册",description="长度为：20")
	private String scene;
	/**
	* 业务ID
	*/
	@Schema(title="业务ID",description="长度为：19")
	private Long bizId;
	/**
	* 端口号
	*/
	@Schema(title="端口号",description="长度为：10")
	private String ports;
	/**
	* 手机号
	*/
	@KeyWords
	@NotNull(message = "手机号不能为空",groups = {Validator.save.class})
	@NotBlank(message = "手机号不能为空",groups = {Validator.save.class})
	@Schema(title="手机号",description="长度为：20")
	private String tel;
	/**
	* 类型，字典SMSBOXTYPE send：发送，receive：接收
	*/
	@Schema(title="类型，字典SMSBOXTYPE send：发送，receive：接收",description="长度为：10")
	private String types;
	/**
	* 短信内容
	*/
	@KeyWords
	@NotNull(message = "短信内容不能为空",groups = {Validator.save.class})
	@NotBlank(message = "短信内容不能为空",groups = {Validator.save.class})
	@Schema(title="短信内容",description="长度为：500")
	private String contents;
	/**
	* 发送时间
	*/
	@Schema(title="发送时间",description="长度为：19")
	private Date sendTime;
	/**
	* 发送状态，字典SMSSENDSTS 1待发送，2：发送成功，3发送失败
	*/
	@Schema(title="发送状态，字典SMSSENDSTS 1待发送，2：发送成功，3发送失败",description="长度为：10")
	private Integer sendSts;
	/**
	* 发送日志
	*/
	@Schema(title="发送日志",description="长度为：65535")
	private String sendLog;
	/**
	* 查阅状态，字典SMSVIEWSTS 1未读，2已读，3已回
	*/
	@Schema(title="查阅状态，字典SMSVIEWSTS 1未读，2已读，3已回",description="长度为：10")
	private Integer viewSts;

}
