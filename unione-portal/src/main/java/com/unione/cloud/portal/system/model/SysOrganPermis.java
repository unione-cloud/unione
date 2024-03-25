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
 * @标题 	SysOrganPermis Entity
 * @描述	机构权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysOrganPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_organ_permis")
public class SysOrganPermis extends Pojo {
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 资源ID
	*/
	@ApiModelProperty(value="资源ID",notes="长度为：19")
	private Long resId;
	/**
	* 资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
	*/
	@ApiModelProperty(value="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",notes="长度为：20")
	private String resType;
	/**
	* 是否可传递授权，1是，0否
	*/
	@ApiModelProperty(value="是否可传递授权，1是，0否",notes="长度为：10")
	private Integer enDilivery;

}
