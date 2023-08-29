package com.unione.cloud.web.dao;

import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.web.model.SysLogs;

@SqlResource("sysLogs") 
public interface SysLogsDao extends DataBaseDao<SysLogs> {

}
