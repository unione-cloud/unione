package com.unione.cloud.system.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.beetsql.annotation.QueryIgnore.QueryType;
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
 * @标题 	SysOnlineDoc Entity
 * @描述	系统管理：在线文档
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-18 18:43:24
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysOnlineDoc")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_online_doc")
public class SysOnlineDoc extends Pojo {
	/**
	* 应用ID
	*/
	@NotNull(message = "应用ID不能为空",groups = {Validator.save.class})
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 标题
	*/
	@KeyWords
	@NotNull(message = "标题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class})
	@Schema(title="标题",description="长度为：100")
	private String title;
	/**
	* 版本号
	*/
	@NotNull(message = "版本号不能为空",groups = {Validator.save.class})
	@NotBlank(message = "版本号不能为空",groups = {Validator.save.class})
	@Schema(title="版本号",description="长度为：30")
	private String versNo;
	/**
	* 图标（字体图标）
	*/
	@Schema(title="图标（字体图标）",description="长度为：100")
	private String iconName;
	/**
	* 大图标(图片图标)
	*/
	@Schema(title="大图标(图片图标)",description="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@Schema(title="中图标(图片图标)",description="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@Schema(title="小图标(图片图标)",description="长度为：250")
	private String picMix;
	/**
	* 文档介绍
	*/
	// @QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="文档介绍",description="长度为：2147483647")
	private String profile;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典ONLINEDOCSTS 1：编制中，2：内审中，3：已发布，4：已归档
	*/
	@Schema(title="状态，字典ONLINEDOCSTS 1：编制中，2：内审中，3：已发布，4：已归档",description="长度为：10")
	private Integer status;
	/**
	* 发布日期
	*/
	@Schema(title="发布日期",description="长度为：19")
	private Date releaseTime;
	/**
	* 归档日期
	*/
	@Schema(title="归档日期",description="长度为：19")
	private Date archiveTime;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
