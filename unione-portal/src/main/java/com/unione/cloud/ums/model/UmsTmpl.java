package com.unione.cloud.ums.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	UmsTmpl Entity
 * @描述	统一消息:模版，根据模版编码，通知方式自动发送各种消息
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsTmpl")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_tmpl")
public class UmsTmpl extends Pojo {
	/**
	* 模版分类，字典UMSTMPLTYPE workflow：工作流，other：其他
	*/
	@Schema(title="模版分类，字典UMSTMPLTYPE workflow：流程，normal：通用，other：其他",description="长度为：20")
	@NotNull(message = "分类不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "分类不能为空",groups = {Validator.save.class,Validator.update.class})
	private String types;
	/**
	* 模版标题
	*/
	@KeyWords
	@Schema(title="模版标题",description="长度为：200")
	@NotNull(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	private String title;
	/**
	* 模版编码，唯一验证
	*/
	@KeyWords
	@Schema(title="模版编码，唯一验证",description="长度为：50")
	private String sn;
	/**
	* HTML模版，用于站内信，邮件等富文本场景
	*/
	@KeyWords
	@Schema(title="HTML模版，用于站内信，邮件等富文本场景",description="长度为：65535")
	private String bodyHtml;
	/**
	* SMS模版，用于短信，推送等场景
	*/
	@KeyWords
	@Schema(title="SMS模版，用于短信，推送等场景",description="长度为：65535")
	private String bodySms;
	/**
	* 模版变量,json数组存储[{name,title,dataType,defaultValue}]
	*/
	@Schema(title="模版变量,json数组存储[{name,title,dataType,defaultValue}]",description="长度为：65535")
	private String vars;
	/**
	* 使用级别，字典UMSTMPLUSEL 1：全局，2：租户，3：机构
	*/
	@Schema(title="使用级别，字典UMSTMPLUSEL 1：全局，2：租户，3：机构",description="长度为：10")
	private Integer usel;
	/**
	* 通知方式，多个逗号分隔，默认站内信，字典UMSMESSAGEWAY sms：短信，email：邮件，site：站内信
	*/
	@Schema(title="通知方式，多个逗号分隔，默认站内信，字典UMSMESSAGEWAY sms：短信，email：邮件，site：站内信",description="长度为：20")
	private String ways;
	/**
	* 模版状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="模版状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 备注
	*/
	@KeyWords
	@Schema(title="备注",description="长度为：500")
	private String remark;
}
