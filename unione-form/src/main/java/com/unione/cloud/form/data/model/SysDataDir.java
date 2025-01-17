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
	@Schema(title="应用ID",description="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 上级ID
	*/
	@Schema(title="上级ID",description="长度为：19")
	private Long parentId;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：200")
	private String title;
	/**
	* 图标_字体
	*/
	@Schema(title="图标_字体",description="长度为：50")
	private String icon;
	/**
	* 图标_图片
	*/
	@Schema(title="图标_图片",description="长度为：200")
	private String iconPic;
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
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：1000")
	private String descs;

}
