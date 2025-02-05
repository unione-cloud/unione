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
 * @标题 	SysPageWidget Entity
 * @描述	系统管理：页面组件
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:57:27
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPageWidget")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_widget")
public class SysPageWidget extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 591425380605644471L;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	private String title;
	/**
	* 名称，唯一
	*/
	@Schema(title="名称，唯一",description="长度为：30")
	private String name;
	/**
	* 类型，字典PORTLETTYPE：form表单组件， table表格，chartBar:柱状图，chartPie:饼图，layout布局
	*/
	@Schema(title="类型，字典PORTLETTYPE：form表单组件， table表格，chartBar:柱状图，chartPie:饼图，layout布局",description="长度为：30")
	private String types;
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
	* 基础组件，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="基础组件，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isBase;
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
	* 属性定义，json结构｛｝
	*/
	@Schema(title="属性定义，json结构｛｝",description="长度为：2147483647")
	private String props;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
