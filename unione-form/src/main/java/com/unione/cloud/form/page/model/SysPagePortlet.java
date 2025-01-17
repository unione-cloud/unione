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
 * @标题 	SysPagePortlet Entity
 * @描述	系统管理：信息组件，使用页面组件配置的具体信息组件，比如：通知公告
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:57:27
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPagePortlet")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_portlet")
public class SysPagePortlet extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1199033771607257590L;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	private String title;
	/**
	* 组件名称
	*/
	@Schema(title="组件名称",description="长度为：30")
	private String widget;
	/**
	* 预览图片url
	*/
	@Schema(title="预览图片url",description="长度为：250")
	private String reviewPic;
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
	* 行业分类，多个逗号分隔
	*/
	@Schema(title="行业分类，多个逗号分隔",description="长度为：500")
	private String trades;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 组件定义，json结构｛｝
	*/
	@Schema(title="组件定义，json结构｛｝",description="长度为：2147483647")
	private String configs;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
