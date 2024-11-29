package com.unione.cloud.form.data.model;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	SysDataDir Entity
 * @描述	系统管理：数据目录
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataDir")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_dir")
public class SysDataDir extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7813821804690062103L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 上级ID
	*/
	@ApiModelProperty(value="上级ID",notes="长度为：19")
	private Long parentId;
	/**
	* 标题
	*/
	@ApiModelProperty(value="标题",notes="长度为：200")
	private String title;
	/**
	* 图标_字体
	*/
	@ApiModelProperty(value="图标_字体",notes="长度为：50")
	private String icon;
	/**
	* 图标_图片
	*/
	@ApiModelProperty(value="图标_图片",notes="长度为：200")
	private String iconPic;
	/**
	* 层级编码
	*/
	@ApiModelProperty(value="层级编码",notes="长度为：40")
	private String lvsn;
	/**
	* 所在层级
	*/
	@ApiModelProperty(value="所在层级",notes="长度为：10")
	private Integer level;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：1000")
	private String descs;

}
