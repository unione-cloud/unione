package com.unione.cloud.common.model;
import java.util.Date;

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
 * @标题 	CommVisitItem Entity
 * @描述	通用：访问登记-明细
 * @作者	Unione Cloud CodeGen
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("common.CommVisitItem")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="comm_visit_item")
public class CommVisitItem extends Pojo {
	/**
	* 应用ID/站点id
	*/
	@Schema(title="应用ID/站点id",description="长度为：19")
	private Long appId;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;
	/**
	* 浏览器
	*/
	@Schema(title="浏览器",description="长度为：50")
	private String explorer;
	/**
	* 操作系统
	*/
	@Schema(title="操作系统",description="长度为：50")
	private String osname;
	/**
	* 访问时间
	*/
	@Schema(title="访问时间",description="长度为：19")
	private Date visitTime;
	/**
	* 访问年份
	*/
	@Schema(title="访问年份",description="长度为：10")
	private Integer visitYear;
	/**
	* 访问季度
	*/
	@Schema(title="访问季度",description="长度为：10")
	private Integer visitQuar;
	/**
	* 访问月份
	*/
	@Schema(title="访问月份",description="长度为：10")
	private Integer visitMonth;
	/**
	* 访问周
	*/
	@Schema(title="访问周",description="长度为：10")
	private Integer visitWeek;
	/**
	* 访问日
	*/
	@Schema(title="访问日",description="长度为：10")
	private Integer visitDay;
	/**
	* 访问IP
	*/
	@Schema(title="访问IP",description="长度为：50")
	private String visitIp;
	/**
	* 所在国家
	*/
	@Schema(title="所在国家",description="长度为：200")
	private String country;
	/**
	* 所在省份
	*/
	@Schema(title="所在省份",description="长度为：100")
	private String province;
	/**
	* 所在城市
	*/
	@Schema(title="所在城市",description="长度为：100")
	private String city;

}
