package com.unione.cloud.web.logs.model;

import java.util.List;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.beetsql.annotation.UniDataPermis.DataPermis;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @类名 <p>SysLogs
 * 
 * @描述 SysLogs类属性
 * 		<p>1.Long sid		
 * 		<p>2.Long appCode		应用编码
 * 		<p>3.Long tenantId		租户ID
 * 		<p>4.Long orgId		机构ID
 * 		<p>5.Long userId		用户ID
 * 		<p>6.String userName		用户姓名
 * 		<p>7.Long requestId		业务请求ID
 * 		<p>8.Long preActionId		前置请求ID
 * 		<p>9.String title		操作标题
 * 		<p>10.String contents		操作内容
 * 		<p>11.String types		操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除,login登录，logout注销，resetpwd修改密码
 * 		<p>12.Integer status		操作状态，字典LOGSTATUS 1成功，2失败，3异常
 * 		<p>13.Long targetId		目标ID
 * 		<p>14.Date startTime		开始时间
 * 		<p>15.Date endTime		完成时间
 * 		<p>16.String ip		操作IP
 * 		<p>17.String extData		扩展信息，标准json对象存储{}
 * 		<p>18.String errorMessage		异常消息
 * 		<p>19.String erroroCode		异常代码
 * 		<p>20.Date created		
 * 		<p>21.Long createdBy		
 *      
 * @数据库表名称:		SYS_LOGS
 * @数据库表备注:	 	系统管理：操作日志
 * 
 * @作者	Jeking Yang
 * @日期	2023年8月29日 下午11:29:27
 * @版本	1.0.0
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="SYS_LOGS")
@UniDataPermis(DataPermis.TENANTID)
public class SysLogs extends Pojo{
	
	private static final long serialVersionUID = 9176487311413735522L;
	// fields start
	/**
	 * 应用编码
	 */
	@UniQueryKeyWord
	@ApiModelProperty(value="应用编码",notes="字符长度为：20")
	private String appCode;
	/**
	 * 用户姓名
	 */
	@UniQueryKeyWord
	@ApiModelProperty(value="用户姓名",notes="字符长度为：50")
	private String userName;
	/**
	 * 	业务操作ID
	 */
	@ApiModelProperty(value="业务操作ID",notes="")
	private Long actionId;
	/**
	 * 	请求ID
	 */
	@ApiModelProperty(value="请求ID",notes="")
	private Long requestId;
	/**
	 * 	前置请求ID（调用链）
	 */
	@ApiModelProperty(value="前置请求ID",notes="")
	private Long prequestId;
	/**
	 * 操作标题
	 */
	@UniQueryKeyWord
	@ApiModelProperty(value="操作标题",notes="字符长度为：50")
	private String title;
	/**
	 * 操作内容
	 */
	@ApiModelProperty(value="操作内容",notes="字符长度为：65,535")
	private String contents;
	/**
	 * 操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除,login登录，logout注销，resetpwd修改密码
	 */
	@ApiModelProperty(value="操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除,login登录，logout注销，resetpwd修改密码",notes="字符长度为：10")
	private String types;
	/**
	 * 操作状态，字典LOGSTATUS 1成功，2失败，3异常
	 */
	@ApiModelProperty(value="操作状态，字典LOGSTATUS 1成功，2失败，3异常",notes="字符长度为：10")
	private Integer status;
	/**
	 * 目标ID
	 */
	@ApiModelProperty(value="目标ID",notes="字符长度为：19")
	private Long targetId;
	/**
	 * 开始时间
	 */
	@ApiModelProperty(value="开始时间",notes="字符长度为：26")
	private Long startTime;
	/**
	 * 完成时间
	 */
	@ApiModelProperty(value="完成时间",notes="字符长度为：26")
	private Long endTime;
	/**
	 * 操作IP
	 */
	@ApiModelProperty(value="操作IP",notes="字符长度为：50")
	private String ip;
	/**
	 * 扩展信息，标准json对象存储{}
	 */
	@ApiModelProperty(value="扩展信息，标准json对象存储{}",notes="字符长度为：2,147,483,647")
	private String extData;
	/**
	 * 异常消息
	 */
	@ApiModelProperty(value="异常消息",notes="字符长度为：2,147,483,647")
	private String errorMessage;
	/**
	 * 异常代码
	 */
	@ApiModelProperty(value="异常代码",notes="字符长度为：10")
	private String errorCode;
	// fields end

	/**
	 * 非持久化属性
	 */
	@ApiModelProperty("主键集合")
	private List<Long> ids;
	@ApiModelProperty("搜索关键字")
	private String keywords;
	
	@ApiModelProperty("查询：时间开始")
	private Long timeBegin;
	@ApiModelProperty("查询：时间")
	private Long timeEnd;
}
