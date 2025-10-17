package com.unione.cloud.system.model;
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
 * @标题 	SysContactUs Entity
 * @描述	系统管理：联系我们
 * @作者	Unione Cloud CodeGen
 * @日期	2025-10-17 15:10:27
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysContactUs")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_contact_us")
public class SysContactUs extends Pojo {
	/**
	* 公司名称
	*/
	@KeyWords
	@NotNull(message = "公司名称不能为空",groups = {Validator.save.class})
	@NotBlank(message = "公司名称不能为空",groups = {Validator.save.class})
	@Schema(title="公司名称",description="长度为：200")
	private String company;
	/**
	* 姓名
	*/
	@KeyWords
	@NotNull(message = "姓名不能为空",groups = {Validator.save.class})
	@NotBlank(message = "姓名不能为空",groups = {Validator.save.class})
	@Schema(title="姓名",description="长度为：50")
	private String name;
	/**
	* 职务
	*/
	@Schema(title="职务",description="长度为：50")
	private String post;
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
	@NotNull(message = "电话不能为空",groups = {Validator.save.class})
	@NotBlank(message = "电话不能为空",groups = {Validator.save.class})
	@Schema(title="电话",description="长度为：50")
	private String tel;
	/**
	* 主题，字典SUBJECTLIST general:一般咨询，support：技术支持，feedback：建议反馈，partnership：商务合作，other：其他
	*/
	@NotNull(message = "主题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "主题不能为空",groups = {Validator.save.class})
	@Schema(title="主题，字典SUBJECTLIST general:一般咨询，support：技术支持，feedback：建议反馈，partnership：商务合作，other：其他",description="长度为：20")
	private String subject;
	/**
	* 信息
	*/
	@KeyWords
	@NotNull(message = "信息不能为空",groups = {Validator.save.class})
	@NotBlank(message = "信息不能为空",groups = {Validator.save.class})
	@Schema(title="信息",description="长度为：500")
	private String message;
	/**
	* 注册状态，字典TUREORFLASE 1是，0否
	*/
	@Schema(title="注册状态，字典TUREORFLASE 1是，0否",description="长度为：10")
	private Integer regSts;
	/**
	* 注册时间，开通帐号时间
	*/
	@Schema(title="注册时间，开通帐号时间",description="长度为：19")
	private Date regTime;
	/**
	* 跟进状态，字典CUSTRAILSTS 1：待跟进，2：跟进中，3：已处理
	*/
	@Schema(title="跟进状态，字典CUSTRAILSTS 1：待跟进，2：跟进中，3：已处理",description="长度为：10")
	private Integer trailSts;
	/**
	* 跟进人ID
	*/
	@Schema(title="跟进人ID",description="长度为：19")
	private Long trailUid;
	/**
	* 跟进人姓名
	*/
	@Schema(title="跟进人姓名",description="长度为：50")
	private String trailUname;
	/**
	* 跟进情况
	*/
	@Schema(title="跟进情况",description="长度为：2147483647")
	private String trailInfos;
	/**
	* 说明
	*/
	@KeyWords
	@Schema(title="说明",description="长度为：200")
	private String descs;

}
