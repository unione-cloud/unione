package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysSystem Entity
 * @描述	系统管理：系统信息
 * @作者	Unione Cloud CodeGen
 * @日期	2026-04-26 21:20:41
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("system.SysSystem")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_system")
public class SysSystem extends Pojo {
	/**
	* 系统名称
	*/
	@Schema(title="系统名称",description="长度为：100")
	@NotNull(message = "系统名称不能为空",groups = Validator.save.class)
	@NotBlank(message = "系统名称不能为空",groups = Validator.save.class)
	private String name;
	/**
	* 系统简称
	*/
	@Schema(title="系统简称",description="长度为：50")
	private String alias;
	/**
	* 系统类型，字典APPTYPES pc:PC端，app:移动端
	*/
	@Schema(title="系统类型，字典APPTYPES pc:PC端，app:移动端",description="长度为：10")
	private String types;
	/**
	* 系统ctx
	*/
	@Schema(title="系统ctx",description="长度为：20")
	@NotNull(message = "系统ctx不能为空",groups = Validator.save.class)
	@NotBlank(message = "系统ctx不能为空",groups = Validator.save.class)
	private String ctx;
	/**
	* 系统logo大
	*/
	@Schema(title="系统logo大",description="长度为：100")
	private String logoLarge;
	/**
	* 系统logo小
	*/
	@Schema(title="系统logo小",description="长度为：100")
	private String logoSmall;
	/**
	* 系统主题
	*/
	@Schema(title="系统主题",description="长度为：20")
	private String themeName;
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
	* 系统秘钥，令牌加密秘钥，用于实现不同系统之间token隔离
	*/
	@Schema(title="系统秘钥，令牌加密秘钥，用于实现不同系统之间token隔离",description="长度为：50")
	private String secret;
	/**
	* 底部信息
	*/
	@Schema(title="底部信息",description="长度为：200")
	private String footer;
	/**
	* 系统配置,json对象存储，{}
	*/
	@Schema(title="系统配置,json对象存储，{}",description="长度为：65535")
	private String configs;
	/**
	* 应用列表,json数组存储，[{title,name,id}]
	*/
	@Schema(title="应用列表,json数组存储，[{title,name,id}]",description="长度为：65535")
	private String appList;
	/**
	 * 导航配置，导航条组件，json存储{
   	 * barStyle:{},//导航条样式
   	 * itemStyle:{},   //导航项样式
   	 * activeStyle:{},  // 活动项样式
   	 * itemList:[{title,name,icon,iconActive,enable,roles}]  //导航项
	 * }
	 */
	@Schema(title="导航配置",description="长度为：65535")
	private String navList;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 系统状态，字典SYSSTATUS 1开发，2内测，3发布，4撤销
	*/
	@Schema(title="系统状态，字典SYSSTATUS 1开发，2内测，3发布，4撤销",description="长度为：10")
	private Integer status;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：200")
	private String descs;

}
