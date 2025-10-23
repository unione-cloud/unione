package com.unione.cloud.ums.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
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
 * @标题 	UmsSmsGtw Entity
 * @描述	统一消息:短信网关
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-22 12:52:46
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("ums.UmsSmsGtw")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_sms_gtw")
public class UmsSmsGtw extends Pojo {
	/**
	* 标题
	*/
	@KeyWords
	@Schema(title="标题",description="长度为：200")
	@NotNull(message = "标题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class})
	private String title;
	/**
	* 编码，唯一验证
	*/
	@KeyWords
	@Schema(title="编码，唯一验证",description="长度为：50")
	@NotNull(message = "编码不能为空",groups = {Validator.save.class})
	@NotBlank(message = "编码不能为空",groups = {Validator.save.class})
	private String sn;
	/**
	* 服务IP
	*/
	@KeyWords
	@Schema(title="服务IP",description="长度为：50")
	@NotNull(message = "服务IP不能为空",groups = {Validator.save.class})
	@NotBlank(message = "服务IP不能为空",groups = {Validator.save.class})
	private String ip;
	/**
	* 服务端口
	*/
	@Schema(title="服务端口",description="长度为：10")
	@NotNull(message = "服务端口不能为空",groups = {Validator.save.class})
	private Integer ports;
	/**
	* 服务URL
	*/
	@KeyWords
	@Schema(title="服务URL",description="长度为：250")
	private String url;
	/**
	* 认证信息，json存储
	*/
	@Schema(title="认证信息，json存储",description="长度为：5000")
	private String authInfo;
	/**
	* 认证接口
	*/
	@Schema(title="认证接口",description="长度为：250")
	private String authApi;
	/**
	* 认证脚本
	*/
	@Schema(title="认证脚本",description="长度为：65535")
	private String authScript;
	/**
	* 发送接口
	*/
	@Schema(title="发送接口",description="长度为：250")
	private String sendApi;
	/**
	* 发送脚本
	*/
	@Schema(title="发送脚本",description="长度为：65535")
	private String sendScript;
	/**
	* 收信接口
	*/
	@Schema(title="收信接口",description="长度为：250")
	private String receiveApi;
	/**
	* 收信脚本
	*/
	@Schema(title="收信脚本",description="长度为：65535")
	private String receiveScript;
	/**
	* 收信CRON
	*/
	@Schema(title="收信CRON",description="长度为：20")
	private String receiveCron;
	/**
	* 收信时间戳
	*/
	@Schema(title="收信时间戳",description="长度为：20")
	private Long receiveTimestamp;
	/**
	* 回执接口
	*/
	@Schema(title="回执接口",description="长度为：250")
	private String receiptApi;
	/**
	* 回执脚本
	*/
	@Schema(title="回执脚本",description="长度为：65535")
	private String receiptScript;
	/**
	* 回执CRON
	*/
	@Schema(title="回执CRON",description="长度为：20")
	private String receiptCron;
	/**
	* 回执时间戳
	*/
	@Schema(title="回执时间戳",description="长度为：20")
	private Long receiptTimestamp;
	/**
	* 说明
	*/
	@KeyWords
	@Schema(title="说明",description="长度为：500")
	private String descs;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;

}
