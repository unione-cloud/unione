package com.unione.cloud.system.model;
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
 * @标题 	SysPostPermis Entity
 * @描述	岗位权限
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysPostPermis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_post_permis")
public class SysPostPermis extends Pojo {
	/**
	* 岗位ID
	*/
	@Schema(title="岗位ID",description="长度为：19")
	private Long postId;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 资源ID
	*/
	@Schema(title="资源ID",description="长度为：19")
	private Long resId;
	/**
	* 资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具
	*/
	@Schema(title="资源类型，字典SYSRESTYPE menu：菜单，btn：按钮，tool：工具",description="长度为：20")
	private String resType;
	/**
	* 是否可传递授权，1是，0否
	*/
	@Schema(title="是否可传递授权，1是，0否",description="长度为：10")
	private Integer enDilivery;

}
