package com.unione.cloud.portal.system.model;
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
 * @标题 	SysCodeTree Entity
 * @描述	系统管理：层级树
 * @作者	Unione Cloud CodeGen
 * @日期	2025-03-12 08:13:20
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysCodeTree")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_code_tree")
public class SysCodeTree extends Pojo {
	/**
	* 应用名称
	*/
	@Schema(title="应用名称",description="长度为：50")
	private String appName;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	private String title;
	/**
	* 编码
	*/
	@Schema(title="编码",description="长度为：20")
	private String sn;
	/**
	* 类型，字典CODETREETYPE 0全局，1租户级，2机构级
	*/
	@Schema(title="类型，字典CODETREETYPE 0全局，1租户级，2机构级",description="长度为：10")
	private Integer types;
	/**
	* 编码长度，默认3位，最小3位
	*/
	@Schema(title="编码长度，默认3位，最小3位",description="长度为：10")
	private Integer lvLen;
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
