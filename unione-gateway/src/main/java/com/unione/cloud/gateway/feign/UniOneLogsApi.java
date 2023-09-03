package com.unione.cloud.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.model.Pojo;

import lombok.Data;

/**
 * @描述 日志api
 * @作者	Jeking Yang
 * @版本	1.0.0
 */
@FeignClient(name="${unione.cloud.server.name:unione-portal}", contextId="uniOneLogs",
	path = "/api/logs",
	url="${unione.cloud.server.ip:}${unione.cloud.server.url:}",
	fallbackFactory = UniOneLogsHystrix.class)
public interface UniOneLogsApi {
	
	@Data
	public static class UniOneLogs extends Pojo{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5275339601520100578L;
		// fields start
		/**
		 * 服务名称
		 */
		private String appCode;
		/**
		 * 操作用户姓名
		 */
		private String userName;
		/**
		 * 	业务操作ID
		 */
		private Long actionId;
		/**
		 * 	请求ID
		 */
		private Long requestId;
		/**
		 * 	前置请求ID（调用链）
		 */
		private Long prequestId;
		/**
		 * 日志标题
		 */
		private String title;
		/**
		 * 日志内容
		 */
		private String contents;
		/**
		 * 操作类别，字典LOGTYPE query查询，insert新增，update修改，delete删除
		 */
		private String types;
		/**
		 * 操作状态，字典LOGSTATUS 1成功，2失败，3异常
		 */
		private Integer status;
		/**
		 * 操作目标ID
		 */
		private Long targetId;
		/**
		 * 操作开始时间
		 */
		private Long startTime;
		/**
		 * 操作完成时间
		 */
		private Long endTime;
		
		/**
		 * 操作IP
		 */
		private String ip;
		/**
		 * 扩展数据，标准json对象存储{}
		 */
		private String extData;
		/**
		 * 操作异常消息
		 */
		private String errorMessage;
		/**
		 * 操作异常代码
		 */
		private String errorCode;
		// fields end

	}

	
	/**
	 * 保存日志
	 * @param logs
	 * @return
	 */
	@PostMapping("/save")
	public Results<Long> save(@RequestBody UniOneLogs logs);
	
	
}

