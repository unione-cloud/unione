package com.unione.cloud.form.page.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysPageMine Entity
 * @描述	系统管理：我的页面
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:44:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPageMine")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_mine")
public class SysPageMine extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3656715671161863908L;
	/**
	* 页面ID
	*/
	@Schema(title="页面ID",description="长度为：19")
	private Long pageId;
	/**
	* 页面设置，json结构{}
	*/
	@Schema(title="页面设置，json结构{}",description="长度为：2147483647")
	private String configs;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：200")
	private String descs;

}
