package com.unione.cloud.form.page.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysPageRef Entity
 * @描述	系统管理：页面引用
 * @作者	Unione Cloud CodeGen
 * @日期	2024-07-22 22:23:18
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPageRef")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_ref")
public class SysPageRef extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1118380104238606721L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 页面ID
	*/
	@ApiModelProperty(value="页面ID",notes="长度为：19")
	private Long pageId;
	/**
	* 页面编码
	*/
	@ApiModelProperty(value="页面编码",notes="长度为：50")
	private String pageSn;
	/**
	* 组件名称
	*/
	@ApiModelProperty(value="组件名称",notes="长度为：50")
	private String widgetName;
	/**
	* 组件编码
	*/
	@ApiModelProperty(value="组件编码",notes="长度为：50")
	private String widgetWid;
	/**
	* 引用页面ID
	*/
	@ApiModelProperty(value="引用页面ID",notes="长度为：19")
	private Long refPageId;
	/**
	* 引用页面编码
	*/
	@ApiModelProperty(value="引用页面编码",notes="长度为：50")
	private String refPageSn;
	/**
	* 备注
	*/
	@ApiModelProperty(value="备注",notes="长度为：200")
	private String descs;

}
