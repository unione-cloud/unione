package com.unione.cloud.portal.system.model;
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
 * @标题 	SysAppInfo Entity
 * @描述	系统管理：应用信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysAppInfo")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_app_info")
public class SysAppInfo extends Pojo {
	/**
	* 应用名称
	*/
	@ApiModelProperty(value="应用名称",notes="长度为：100")
	private String name;
	/**
	* 应用编码
	*/
	@ApiModelProperty(value="应用编码",notes="长度为：50")
	private String codes;
	/**
	* 是否微应用
	*/
	@ApiModelProperty(value="是否微应用",notes="长度为：10")
	private Integer isMp;
	/**
	* 应用URL
	*/
	@ApiModelProperty(value="应用URL",notes="长度为：250")
	private String url;
	/**
	* 首页URL
	*/
	@ApiModelProperty(value="首页URL",notes="长度为：250")
	private String welcome;
	/**
	* 版本号
	*/
	@ApiModelProperty(value="版本号",notes="长度为：30")
	private String versNo;
	/**
	* 版本说明
	*/
	@ApiModelProperty(value="版本说明",notes="长度为：500")
	private String versDesc;
	/**
	* 图标（字体图标）
	*/
	@ApiModelProperty(value="图标（字体图标）",notes="长度为：100")
	private String icon;
	/**
	* 大图标(图片图标)
	*/
	@ApiModelProperty(value="大图标(图片图标)",notes="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@ApiModelProperty(value="中图标(图片图标)",notes="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@ApiModelProperty(value="小图标(图片图标)",notes="长度为：250")
	private String picMix;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="状态，字典，使用状态 USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：500")
	private String descs;

}
