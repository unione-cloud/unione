package com.unione.cloud.form.page.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysPageRelease Entity
 * @描述	系统管理：页面发布
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:44:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPageRelease")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_release")
public class SysPageRelease extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6393555894966621876L;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 模版ID
	*/
	@Schema(title="模版ID",description="长度为：19")
	private Long tmplId;
	/**
	* 页面组件
	*/
	@Schema(title="页面组件",description="长度为：100")
	private String component;
	/**
	* 页面标题
	*/
	@Schema(title="页面标题",description="长度为：100")
	private String title;
	/**
	* 页面编码，唯一
	*/
	@Schema(title="页面编码，唯一",description="长度为：50")
	private String sn;
	/**
	* 版本号
	*/
	@Schema(title="版本号",description="长度为：10")
	private Integer vers;
	/**
	* 概要信息
	*/
	@Schema(title="概要信息",description="长度为：250")
	private String summary;
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
	* 页面分类，字典PAGETYPE  code:编码型页面，setting：配置型页面，design：设计型页面
	*/
	@Schema(title="页面分类，字典PAGETYPE  code:编码型页面，setting：配置型页面，design：设计型页面",description="长度为：20")
	private String types;
	/**
	* 行业分类，多个逗号分隔
	*/
	@Schema(title="行业分类，多个逗号分隔",description="长度为：500")
	private String trades;
	/**
	* 预览图片url
	*/
	@Schema(title="预览图片url",description="长度为：250")
	private String reviewPic;
	/**
	* 页面定义，json结构
            ｛
            
            
            ｝
	*/
	@Schema(title="页面定义，json结构｛｝",description="长度为：2147483647")
	private String configs;
	/**
	* 页面签名
	*/
	@Schema(title="页面签名",description="页面签名，（configs等）字段hash运算后的值,长度为：100")
	private String signature;
	/**
	* 是否模版
	*/
	@Schema(title="是否模版",description="长度为：10")
	private Integer isTmpl;
	/**
	* 平台页面，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="平台页面，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isGlobal;
	/**
	* 页面状态，字典PAGESTATUS 1新建，2发布，3修改，4撤回
	*/
	@Schema(title="页面状态，字典PAGESTATUS 1新建，2发布，3修改，4撤回",description="长度为：10")
	private Integer status;
	/**
	* 发布日期
	*/
	@Schema(title="发布日期",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date publishDate;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：200")
	private String descs;

}
