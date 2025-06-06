package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

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
 * @标题 	SysConfigDefine Entity
 * @描述	系统管理：统一配置
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-04 08:29:01
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysConfigDefine")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_config_define")
public class SysConfigDefine extends Pojo {
	/**
	* 上级ID
	*/
	@Schema(title="上级ID",description="长度为：19")
	private Long parentId;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	// @NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 应用名称
	*/
	@Schema(title="应用名称",description="长度为：50")
	@NotNull(message="应用名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message="应用名称不能为空",groups = {Validator.save.class,Validator.update.class})
	private String appName;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	@NotNull(message="标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message="标题不能为空",groups = {Validator.save.class,Validator.update.class})
	private String title;
	/**
	* 名称
	*/
	@Schema(title="名称",description="长度为：20")
	@NotNull(message="名称不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message="名称不能为空",groups = {Validator.save.class,Validator.update.class})
	private String name;
	/**
	* 配置编码，从配置根节点的NAM逐层拼接到当前配置NAME,使用“.”分割
	*/
	@Schema(title="配置编码，从配置根节点的NAM逐层拼接到当前配置NAME,使用“.”分割",description="长度为：250")
	private String sn;
	/**
	* 类型，字典IUCONFTYPE 0全局，1租户级，2机构级，3用户级
	*/
	@Schema(title="类型，字典IUCONFTYPE 0全局，1租户级，2机构级，3用户级",description="长度为：10")
	private Integer types;
	/**
	* 默认值
	*/
	@Schema(title="默认值",description="长度为：1000")
	private String valueDefault;
	/**
	* 设置值
	*/
	@Schema(title="设置值",description="长度为：1000")
	private String valueUsed;
	/**
	* 层级编码
	*/
	@Schema(title="层级编码",description="长度为：40")
	private String lvsn;
	/**
	* 所在层级
	*/
	@Schema(title="所在层级",description="长度为：10")
	private Integer level;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 是否需要授权，字典TUREORFALSE 1是，0否，对于非平台的配置，如果设置成需要权限，则没有权限的帐号不能进行配置操作
	*/
	@Schema(title="是否需要授权，字典TUREORFALSE 1是，0否，对于非平台的配置，如果设置成需要权限，则没有权限的帐号不能进行配置操作",description="长度为：10")
	private Integer isAuth;
	/**
	* 说明
	*/
	@Schema(title="说明",description="长度为：200")
	private String descs;

}
