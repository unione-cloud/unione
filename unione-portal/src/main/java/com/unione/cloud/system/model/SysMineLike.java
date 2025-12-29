package com.unione.cloud.system.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysMineLike Entity
 * @描述	系统管理：我的收藏
 * @作者	Unione Cloud CodeGen
 * @日期	2025-12-29 08:45:26
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysMineLike")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_mine_like")
public class SysMineLike extends Pojo {
	/**
	* 目标类型
	*/
	@Schema(title="目标类型",description="长度为：20")
	private String targetType;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;
	/**
	* 配置，json格式存储
	*/
	@Schema(title="配置，json格式存储",description="长度为：5000")
	private String configs;

}
