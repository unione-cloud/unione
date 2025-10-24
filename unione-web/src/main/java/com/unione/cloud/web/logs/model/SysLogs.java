package com.unione.cloud.web.logs.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysLogs Entity
 * @描述	系统管理：操作日志
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("system.SysLogs")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_logs")
public class SysLogs extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1061181560553691314L;
	/**
	* 应用编码
	*/
	@Schema(title="应用编码",description="长度为：50")
	private String appSn;
	/**
	* 用户姓名
	*/
	@Schema(title="用户姓名",description="长度为：50")
	private String userName;
	/**
	* 业务操作ID
	*/
	@Schema(title="业务操作ID",description="长度为：19")
	private Long actionId;
	/**
	* 业务请求ID
	*/
	@Schema(title="业务请求ID",description="长度为：19")
	private Long requestId;
	/**
	* 前置请求ID
	*/
	@Schema(title="前置请求ID",description="长度为：19")
	private Long prequestId;
	/**
	* 操作标题
	*/
	@Schema(title="操作标题",description="长度为：50")
	private String title;
	/**
	* 操作内容
	*/
	@Schema(title="操作内容",description="长度为：65535")
	private String contents;
	/**
	* 操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除,login登录，logout注销，resetpwd修改密码
	*/
	@Schema(title="操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除,login登录，logout注销，resetpwd修改密码",description="长度为：10")
	private String types;
	/**
	* 操作状态，字典LOGSTATUS 1成功，2失败，3异常
	*/
	@Schema(title="操作状态，字典LOGSTATUS 1成功，2失败，3异常",description="长度为：10")
	private Integer status;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;
	/**
	* 目标标题
	*/
	@Schema(title="目标标题",description="长度为：19")
	private String targetTitle;
	/**
	* 开始时间
	*/
	@Schema(title="开始时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date startTime;
	/**
	* 完成时间
	*/
	@Schema(title="完成时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date endTime;
	/**
	* 操作IP
	*/
	@Schema(title="操作IP",description="长度为：50")
	private String ip;
	/**
	* 扩展信息，标准json对象存储{}
	*/
	@Schema(title="扩展信息，标准json对象存储{}",description="长度为：2147483647")
	private String extData;
	/**
	* 异常消息
	*/
	@Schema(title="异常消息",description="长度为：2147483647")
	private String errorMessage;
	/**
	* 异常代码
	*/
	@Schema(title="异常代码",description="长度为：10")
	private String errorCode;

	
	///////////////
	// 非持久化属性
	@Schema(title="开始时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date timeBegin;
	
	@Schema(title="截止时间",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date timeEnd;
	
}
