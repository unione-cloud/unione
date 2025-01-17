package com.unione.cloud.form.data.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysDataPermis Entity
 * @描述	系统管理：数据权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_permis")
public class SysDataPermis extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2228469034937505383L;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 数据定义ID
	*/
	@Schema(title="数据定义ID",description="长度为：19")
	private Long defineId;
	/**
	* 权限名称
	*/
	@Schema(title="权限名称",description="长度为：50")
	private String name;
	/**
	* 是否需要授权
	*/
	@Schema(title="是否需要授权",description="长度为：10")
	private Integer needAuth;
	/**
	* 权限SQL表达式
	*/
	@Schema(title="权限SQL表达式",description="长度为：1000")
	private String express;
	/**
	* 权限配置，json存储{}
	*/
	@Schema(title="权限配置，json存储{}",description="长度为：65535")
	private String configs;
	/**
	* 权限描述
	*/
	@Schema(title="权限描述",description="长度为：250")
	private String descs;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="使用状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
