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
 * @标题 	SysCodeLvsn Entity
 * @描述	系统管理：层级编码
 * @作者	Unione Cloud CodeGen
 * @日期	2025-03-12 08:13:20
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysCodeLvsn")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_code_lvsn")
public class SysCodeLvsn extends Pojo {
	/**
	* 树ID
	*/
	@Schema(title="树ID",description="长度为：19")
	private Long treeId;
	/**
	* 树编码
	*/
	@Schema(title="树编码",description="长度为：20")
	private String treeSn;
	/**
	* 编码长度，默认3位，最小3位
	*/
	@Schema(title="编码长度，默认3位，最小3位",description="长度为：10")
	private Integer lvLen;
	/**
	* 当前最大层级
	*/
	@Schema(title="当前最大层级",description="长度为：10")
	private Integer currentMaxLv;
	/**
	* 当前编码值
	*/
	@Schema(title="当前编码值",description="长度为：200")
	private String currentLvsn;

}
