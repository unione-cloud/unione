package com.unione.cloud.common.model;
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
 * @标题 	CommVisitTarget Entity
 * @描述	通用：访问登记-目标
 * @作者	Unione Cloud CodeGen
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.CommVisitTarget")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="comm_visit_target")
public class CommVisitTarget extends Pojo {
	/**
	* 应用ID/站点ID
	*/
	@Schema(title="应用ID/站点ID",description="长度为：19")
	private Long appId;
	/**
	* 上级ID
	*/
	@Schema(title="上级ID",description="长度为：19")
	private Long parentId;
	/**
	* 目标类型，字典COMMVISITTARGETTYPE menu：菜单页面，site：网站页面
	*/
	@Schema(title="目标类型，字典COMMVISITTARGETTYPE menu：菜单页面，site：网站页面",description="长度为：20")
	private String targetType;
	/**
	* 目标标题
	*/
	@Schema(title="目标标题",description="长度为：200")
	private String targetTitle;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;
	/**
	* 目标url
	*/
	@Schema(title="目标url",description="长度为：200")
	private String targetUrl;
	/**
	* 封面图片id
	*/
	@Schema(title="封面图片id",description="长度为：19")
	private Long cover;
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
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;

}
