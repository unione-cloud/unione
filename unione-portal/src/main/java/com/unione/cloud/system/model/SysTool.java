package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysTool Entity
 * @描述	系统管理：常用工具
 * @作者	Unione Cloud CodeGen
 * @日期	2026-08-09 08:43:28
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("system.SysTool")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_tool")
public class SysTool extends Pojo {
	/**
	* 系统ID
	*/
	@Schema(title="系统ID",description="长度为：19")
	private Long sysId;
	/**
	* 资源ID
	*/
	@Schema(title="资源ID",description="长度为：19")
	private Long resId;
	/**
	* 分组名称
	*/
	@Schema(title="分组名称",description="长度为：50")
	private String gname;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	@NotNull(message="标题不能为空",groups = {Validator.save.class})
	@NotEmpty(message="标题不能为空",groups = {Validator.save.class})
	private String title;
	/**
	* URL
	*/
	@Schema(title="URL",description="长度为：250")
	private String url;
	/**
	* 是否全局（平台显示）
	*/
	@Schema(title="是否全局（平台显示）",description="长度为：10")
	private Integer isGlobal;
	/**
	* 是否默认（租户显示）
	*/
	@Schema(title="是否默认（租户显示）",description="长度为：10")
	private Integer isDefualt;
	/**
	* 是否私有
	*/
	@Schema(title="是否私有",description="长度为：10")
	private Integer isPrivate;
	/**
	* 类型，字典TOOLTYPE  1应用，2工具
	*/
	@Schema(title="类型，字典TOOLTYPE  1应用，2工具",description="长度为：10")
	@NotNull(message="类型不能为空",groups = {Validator.save.class})
	private Integer types;
	/**
	* 响应方式，字典ANSWERWAY dialog对话框，drawer抽屉，router路由，page页面（浏览器新tab页）
	*/
	@Schema(title="响应方式，字典ANSWERWAY dialog对话框，drawer抽屉，router路由，page页面（浏览器新tab页）",description="长度为：30")
	private String answerWay;
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
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;
	/**
	* 配置，json格式存储
	*/
	@Schema(title="配置，json格式存储",description="长度为：2147483647")
	private String configs;

}
