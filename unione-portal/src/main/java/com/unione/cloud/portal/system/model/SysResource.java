package com.unione.cloud.portal.system.model;
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
 * @标题 	SysResource Entity
 * @描述	系统管理：系统资源
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysResource")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_resource")
public class SysResource extends Pojo {
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 上级菜单ID(根节点为-1)
	*/
	@Schema(title="上级菜单ID(根节点为-1)",description="长度为：19")
	private Long parentId;
	/**
	* 资源名称/编码，唯一
	*/
	@Schema(title="资源名称/编码，唯一",description="长度为：100")
	private String name;
	/**
	* 资源标题
	*/
	@Schema(title="资源标题",description="长度为：100")
	private String title;
	/**
	* 资源别名（授权树区别重复菜单名称）
	*/
	@Schema(title="资源别名（授权树区别重复菜单名称）",description="长度为：100")
	private String alias;
	/**
	* 资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
	*/
	@Schema(title="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",description="长度为：20")
	private String types;
	/**
	* 资源URL地址
	*/
	@Schema(title="资源URL地址",description="长度为：250")
	private String url;
	/**
	* 是否iframe打开，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="是否iframe打开，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isIframe;
	/**
	* 是否外部链接，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="是否外部链接，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isExternal;
	/**
	* 是否隐藏，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否隐藏，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isHide;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否叶子节点，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
	/**
	* 是否需要授权，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否需要授权，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isNeedPermis;
	/**
	* 是否平台资源，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否平台资源，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isPlatform;
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
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;
	/**
	* 资源设置,JSON存储{}
	*/
	@Schema(title="资源设置,JSON存储{}",description="长度为：1000")
	private String configs;

}
