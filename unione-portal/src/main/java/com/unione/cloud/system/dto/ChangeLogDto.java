package com.unione.cloud.system.dto;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.system.model.SysChangeLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_change_log")
public class ChangeLogDto extends SysChangeLog {

    private String appName;

    private Integer appStatus;

    private List<String> appCategorys;

}
