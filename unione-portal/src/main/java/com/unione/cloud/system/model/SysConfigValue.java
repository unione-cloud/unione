package com.unione.cloud.system.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysConfigValue Entity
 * @描述	系统管理：统一配置内容
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-04 08:29:01
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysConfigValue")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_config_value")
public class SysConfigValue extends Pojo {
	/**
	* 配置ID
	*/
	@Schema(title="配置ID",description="长度为：19")
	private Long configId;
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
	* 设置值
	*/
	@Schema(title="设置值",description="长度为：1000")
	private String valueUsed;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@Schema(title="说明",description="长度为：200")
	private String descs;

}
