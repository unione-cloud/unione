package com.unione.cloud.portal.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysRole Entity
 * @描述	系统管理：角色信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysRole")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_role")
public class SysRole extends Pojo {
	/**
	* 名称
	*/
	@KeyWords
	@Schema(title="名称",description="长度为：100")
	private String name;
	/**
	* 编码
	*/
	@KeyWords
	@Schema(title="编码",description="长度为：20")
	private String sn;
	/**
	* 类型，字典ROLETYPE 9其他
	*/
	@Schema(title="类型，字典ROLETYPE 9其他",description="长度为：10")
	private Integer types;
	/**
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@KeyWords
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
