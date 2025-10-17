package com.unione.cloud.system.model;
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
 * @标题 	SysTrendRss Entity
 * @描述	系统管理：动态订阅
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 14:46:39
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysTrendRss")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_trend_rss")
public class SysTrendRss extends Pojo {
	/**
	* 姓名
	*/
	@KeyWords
	@Schema(title="姓名",description="长度为：50")
	private String name;
	/**
	* 邮箱
	*/
	@KeyWords
	@Schema(title="邮箱",description="长度为：200")
	private String email;
	/**
	* 电话
	*/
	@KeyWords
	@Schema(title="电话",description="长度为：50")
	private String tel;
	/**
	* 订阅主题，多个逗号分隔，字典RSSSUBJECTS all：所有
	*/
	@NotNull(message = "订阅主题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "订阅主题不能为空",groups = {Validator.save.class})
	@Schema(title="订阅主题，多个逗号分隔，字典RSSSUBJECTS all：所有",description="长度为：20")
	private String subject;
	/**
	* 说明
	*/
	@KeyWords
	@Schema(title="说明",description="长度为：200")
	private String descs;

}
