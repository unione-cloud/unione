package com.unione.cloud.common.dto;

import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unione.cloud.common.model.CommVisitStat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Table(name="comm_visit_stat")
@SqlResource("common.CommVisitStatDto")
public class CommVisitStatDto extends CommVisitStat{

    @Schema(title="开始时间",description = "时间格式:yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeBegin;

	@Schema(title="截止时间",description = "时间格式:yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeEnd;

	@JsonIgnore
	private Date time;
}
