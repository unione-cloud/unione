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
 * @标题 	SysChangeLog Entity
 * @描述	系统管理：更新日志
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 14:46:39
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysChangeLog")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_change_log")
public class SysChangeLog extends Pojo {
	/**
	* 应用ID
	*/
	@NotNull(message = "应用ID不能为空",groups = {Validator.save.class})
	@NotBlank(message = "应用ID不能为空",groups = {Validator.save.class})
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 版本号
	*/
	@KeyWords
	@NotNull(message = "版本号不能为空",groups = {Validator.save.class})
	@NotBlank(message = "版本号不能为空",groups = {Validator.save.class})
	@Schema(title="版本号",description="长度为：30")
	private String versNo;
	/**
	* 版本类型，字典VERSTYPE release：发布，dev：开发，beta：内测
	*/
	@NotNull(message = "版本类型不能为空",groups = {Validator.save.class})
	@NotBlank(message = "版本类型不能为空",groups = {Validator.save.class})
	@Schema(title="版本类型，字典VERSTYPE release：发布，dev：开发，beta：内测",description="长度为：20")
	private String versType;
	/**
	* 版本说明
	*/
	@KeyWords
	@NotNull(message = "版本说明不能为空",groups = {Validator.save.class})
	@NotBlank(message = "版本说明不能为空",groups = {Validator.save.class})
	@Schema(title="版本说明",description="长度为：500")
	private String versDesc;
	/**
	* 更新类型，字典CHANGETYPE master：主要更新，minor：次要更新，pack：补丁更新
	*/
	@NotNull(message = "更新类型不能为空",groups = {Validator.save.class})
	@NotBlank(message = "更新类型不能为空",groups = {Validator.save.class})
	@Schema(title="更新类型，字典CHANGETYPE master：主要更新，minor：次要更新，pack：补丁更新",description="长度为：20")
	private String changeType;
	/**
	* 更新说明
	*/
	@KeyWords
	@QueryIgnore(QueryType.SELECT_LIST)
	@NotNull(message = "更新说明不能为空",groups = {Validator.save.class})
	@NotBlank(message = "更新说明不能为空",groups = {Validator.save.class})
	@Schema(title="更新说明",description="长度为：2147483647")
	private String changeTxt;
	/**
	* 升级指南
	*/
	@Schema(title="升级指南",description="长度为：65535")
	private String upgradeTips;
	/**
	* 发布时间
	*/
	@Schema(title="发布时间",description="长度为：19")
	private Date releaseTime;
	/**
	* 资源连接，json存储，eg：{win:'url1',linux:'url2',android:'url3',iso:'url4'}
	*/
	@Schema(title="资源连接，json存储，eg：{win:'url1',linux:'url2',android:'url3',iso:'url4'}",description="长度为：5000")
	private String resLink;

}
