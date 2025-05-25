package com.unione.cloud.system.model;
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
 * @标题 	SysAppInfo Entity
 * @描述	系统管理：应用信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
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
	@KeyWords
	@Schema(title="应用名称",description="长度为：100")
	private String name;
	/**
	* 应用编码，唯一
	*/
	@KeyWords
	@Schema(title="应用编码，唯一",description="长度为：50")
	private String sn;
	/**
	* 是否微应用
	*/
	@Schema(title="是否微应用",description="长度为：10")
	private Integer isMp;
	/**
	* 应用URL
	*/
	@Schema(title="应用URL",description="长度为：250")
	private String url;
	/**
	* 首页URL
	*/
	@Schema(title="首页URL",description="长度为：250")
	private String welcome;
	/**
	* 版本号
	*/
	@Schema(title="版本号",description="长度为：30")
	private String versNo;
	/**
	* 版本说明
	*/
	@Schema(title="版本说明",description="长度为：500")
	private String versDesc;
	/**
	* 图标（字体图标）
	*/
	@Schema(title="图标（字体图标）",description="长度为：100")
	private String icon;
	/**
	* 大图标(图片图标)
	*/
	@Schema(title="大图标(图片图标)",description="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@Schema(title="中图标(图片图标)",description="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@Schema(title="小图标(图片图标)",description="长度为：250")
	private String picMix;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 行业分类
	*/
	@Schema(title="行业分类",description="行业分类，多个逗号分隔")
	private String trades;
	/**
	* 应用类型 字典APPTYPES pc：PC，app：APP
	*/
	@Schema(title="应用类型",description="字典APPTYPES pc：PC，app：APP")
	private String types;
	/**
	* 是否平台应用，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否平台应用，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isPlatform;
	/**
	* 是否模版，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否模版，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isTmpl;
	/**
	* 状态，字典，使用状态 APPSTATUS 1新建，2内测，3发布，4撤销
	*/
	@Schema(title="状态，字典",description="使用状态 APPSTATUS 1新建，2内测，3发布，4撤销")
	private Integer status;
	/**
	* 描述
	*/
	@KeyWords
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
