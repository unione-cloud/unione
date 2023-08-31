package com.unione.cloud.gateway.util;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.exception.RemoteException;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.gateway.feign.UniOneLogsApi;
import com.unione.cloud.gateway.feign.UniOneLogsApi.UniOneLogs;

import lombok.extern.slf4j.Slf4j;


/**
 * 业务日志工具类
 * @author Jeking Yang
 * @Date   2020-07-31
 */
@Slf4j
@Component
@RefreshScope
public class LogsUtil {
	
	private static UniOneLogsApi logsApi;
	
	
	private static Long                ipThreshold;	// ip token越权数量阀值（超出该阀值记录日志并重新计数）
	private static Cache<String, Long> ipCache;		// ip token越权次数
	/**
	 * 	token签名验证失败记录器缓存时间，默认60s
	 *  token sign failure cache timeout
	 * @param timeout
	 */
	@Value("${security.token.sfic.timeout:60}")
	public void setIpCacheTime(int timeout) {
		LogsUtil.ipCache=CacheBuilder.newBuilder()
				.expireAfterWrite(timeout, TimeUnit.SECONDS)
				.build();
	}
	/**
	 * 	token签名验证失败计数器阀值（超出该阀值记录日志并重新计数），默认：100
	 * @param iptd
	 */
	@Value("${security.token.sfic.iptd:100}")
	public void setIpThreshold(Long iptd) {
		LogsUtil.ipThreshold=iptd;
	}
	
	@Lazy
	@Autowired
	public void setLogsApi(UniOneLogsApi logsApi) {
		LogsUtil.logsApi = logsApi;
	}

	// 日志记录对象
	private static ThreadLocal<UniOneLogs> entry=new ThreadLocal<>();
	// 日志内容对象
	private static ThreadLocal<StringBuffer> contents=new ThreadLocal<>();
	
	// 操作类型
	public static enum LogType{
		Token("token"),
		Login("login"),Logout("logout"),ResetPwd("resetpwd");
		
		private String value;
		LogType(String value){
			this.value=value;
		}
		public String value() {
			return value;
		}
	}
	
	
	/**
	 * 获得日志对象
	 * @return
	 */
	public static UniOneLogs getEntry() {
		UniOneLogs ent=entry.get();
		if(ent==null) {
			contents.set(null);
			ent=new UniOneLogs();
			ent.setCreated(new Date());
			ent.setStartTime(new Date());
			entry.set(ent);
		}
		return ent;
	} 
	
	/**
	 * 	设置用户信息
	 * @param principal
	 */
	public static void setPrincipal(UserPrincipal principal) {
		UniOneLogs ent=getEntry();
		ent.setTenantId(principal.getTenantId());
		ent.setOrgId(principal.getOrgId());
		ent.setUserId(principal.getSid());
		ent.setUserName(principal.getRealName());
		ent.setCreatedBy(principal.getSid());
		ent.setLastUpdatedBy(principal.getSid());
	}
	
	/**
	 * 获得日志内容对象
	 * @return
	 */
	private static StringBuffer getContents() {
		StringBuffer ent=contents.get();
		if(ent==null) {
			ent=new StringBuffer();
			contents.set(ent);
		}
		return ent;
	}
	
	/**
	 * 设置日志信息
	 * @param type				操作类型
	 * @param title				操作标题
	 */
	public static void set(LogType type,String title) {
		UniOneLogs log=getEntry();
		log.setTypes(type.value);
		log.setTitle(title);
	}
	
	
	/**
	 * 设置日志信息
	 * @param type				操作类型
	 * @param title				操作标题
	 * @param targetId			目标ID
	 */
	public static void set(LogType type,String title,Long targetId) {
		set(type, title);
		setTarget(targetId);
	}
	
	
	
	/**
	 * 设置扩展数据
	 * @param extData	标准的JSON对象字符串
	 */
	public static void setExtData(String extData) {
		UniOneLogs log=getEntry();
		log.setExtData(extData);
	}
	
	/**
	 * 	设置ip
	 * @param ip
	 */
	public static void setIp(String ip) {
		UniOneLogs log=getEntry();
		log.setIp(ip);
	}
	
	/**
	 * 设置服务名称
	 * @param appCode
	 */
	public static void setAppCode(String appCode) {
		UniOneLogs log=getEntry();
		log.setAppCode(appCode);
	}
	
	/**
	 * 设置目标信息
	 * @param targetId			目标ID
	 * @param targetTitle		目标标题
	 */
	public static void setTarget(Long targetId) {
		UniOneLogs log=getEntry();
		log.setTargetId(targetId);
	}
	
	/**
	 * 	添加操作内容
	 * @param content
	 */
	public static void add(String content) {
		StringBuffer buf=getContents();
		buf.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.S"))
		   .append("\t").append(content).append("\n");
	}
	
	/**
	 * 	添加操作内容
	 * @param content
	 * @param params
	 */
	public static void add(String content,Object... params) {
		StringBuffer buf=getContents();
		buf.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.S"))
		   .append("\t").append(String.format(content, params)).append("\n");
	}
	
	/**
	 * 	（根据条件）添加操作内容
	 * @param flag	true:添加，false：忽略
	 * @param content
	 */
	public static void add(boolean flag,String content) {
		if(flag) {
			add(content);
		}
	}
	
	/**
	 * 	（根据条件）添加操作内容
	 * @param flag	true:添加，false：忽略
	 * @param content
	 * @param params
	 */
	public static void add(boolean flag,String content,Object... params) {
		if(flag) {
			add(content,params);
		}
	}
	
	/**
	 * 保存日志信息	
	 * @param isSuccess		true：成功/false：失败
	 */
	public static void save(boolean isSuccess) {
		UniOneLogs log=getEntry();
		log.setStatus(isSuccess?1:2);
		log.setEndTime(new Date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	
	 * @param isSuccess		true：成功/false：失败
	 * @param targetId
	 * @param targetTitle
	 */
	public static void save(boolean isSuccess, Long targetId) {
		UniOneLogs log=getEntry();
		setTarget(targetId);
		log.setStatus(isSuccess?1:2);
		log.setEndTime(new Date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	【成功】
	 */
	public static void success() {
		UniOneLogs log=getEntry();
		log.setStatus(1);
		log.setEndTime(new Date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	【成功】
	 * @param targetId
	 * @param targetTitle
	 */
	public static void success(Long targetId) {
		UniOneLogs log=getEntry();
		setTarget(targetId);
		log.setStatus(1);
		log.setEndTime(new Date());
		doSave(log);
	}
	
	/**
	 * 	保存日志信息		【失败】
	 * @param e			异常对象
	 */
	public static void failure(Exception e) {
		add(e.getMessage());
		UniOneLogs logs=getEntry();
		logs.setStatus(2);
		logs.setEndTime(new Date());
		if(e instanceof ServiceException) {
			ServiceException ee=(ServiceException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof RemoteException) {
			RemoteException ee=(RemoteException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof DataBaseException) {
			DataBaseException ee=(DataBaseException)e;
			logs.setErrorCode(ee.getErrorCode());
		}
		logs.setErrorMessage(e.getLocalizedMessage());
		doSave(logs);
		log.error(e.getMessage(),e);
	}
	
	/**
	 * 	保存日志
	 */
	public static void failure() {
		UniOneLogs logs=getEntry();
		logs.setStatus(2);
		logs.setEndTime(new Date());
		doSave(logs);
	}
	
	/**
	 * 	保存日志信息		【失败】
	 * @param errorMessage
	 * @param e
	 */
	public static void failure(String errorMessage,Exception e) {
		add(errorMessage);
		UniOneLogs logs=getEntry();
		logs.setErrorMessage(errorMessage);
		logs.setStatus(2);
		logs.setEndTime(new Date());
		if(e instanceof ServiceException) {
			ServiceException ee=(ServiceException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof RemoteException) {
			RemoteException ee=(RemoteException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof DataBaseException) {
			DataBaseException ee=(DataBaseException)e;
			logs.setErrorCode(ee.getErrorCode());
		}
		doSave(logs);
		log.error(errorMessage,e);
	}
	
	/**
	 * 保存日志信息				【失败】
	 * @param errorCode			错误代码
	 * @param errorMessage		错误消息
	 */
	public static void failure(String errorCode,String errorMessage) {
		UniOneLogs log=getEntry();
		log.setStatus(2);
		log.setEndTime(new Date());
		log.setErrorCode(errorCode);
		log.setErrorMessage(errorMessage);
		doSave(log);
	}
	
	
	/**
	 * 	保存日志信息				操作异常
	 * @param errorCode			错误代码
	 * @param errorMessage		错误消息
	 */
	public static void error(String errorCode,String errorMessage) {
		UniOneLogs log=getEntry();
		log.setStatus(3);
		log.setEndTime(new Date());
		log.setErrorCode(errorCode);
		log.setErrorMessage(errorMessage);
		doSave(log);
	}
	
	/**
	 * 	保存异信息
	 * @param e		异常信息
	 */
	public static void error(Exception e) {
		add(e.getMessage());
		UniOneLogs logs=getEntry();
		logs.setStatus(3);
		logs.setEndTime(new Date());
		if(e instanceof ServiceException) {
			ServiceException ee=(ServiceException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof RemoteException) {
			RemoteException ee=(RemoteException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof DataBaseException) {
			DataBaseException ee=(DataBaseException)e;
			logs.setErrorCode(ee.getErrorCode());
		}
		
		StringBuffer emsg=new StringBuffer();
		emsg.append("LocalizedMessage:").append(e.getLocalizedMessage()).append("\n").append("\n");
		emsg.append("StackTrace:").append("\n");
		for(StackTraceElement stack:e.getStackTrace()) {
			emsg.append(stack.toString()).append("\n").append("\n");
		}
		logs.setErrorMessage(emsg.toString());
		
		doSave(logs);
		log.error(e.getMessage(),e);
	}
	
	/**
	 * 	保存异信息
	 * @param errorMessage
	 * @param e
	 */
	public static void error(String errorMessage,Exception e) {
		add(errorMessage);
		UniOneLogs logs=getEntry();
		logs.setStatus(3);
		logs.setEndTime(new Date());
		if(e instanceof ServiceException) {
			ServiceException ee=(ServiceException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof RemoteException) {
			RemoteException ee=(RemoteException)e;
			logs.setErrorCode(ee.getErrorCode());
		}else if(e instanceof DataBaseException) {
			DataBaseException ee=(DataBaseException)e;
			logs.setErrorCode(ee.getErrorCode());
		}
		
		StringBuffer emsg=new StringBuffer();
		emsg.append("LocalizedMessage:").append(e.getLocalizedMessage()).append("\n").append("\n");
		emsg.append("StackTrace:").append("\n");
		for(StackTraceElement stack:e.getStackTrace()) {
			emsg.append(stack.toString()).append("\n").append("\n");
		}
		logs.setErrorMessage(emsg.toString());
		
		doSave(logs);
		log.error(errorMessage,e);
	}
	
	
	/**
	 * 	保存日志信息	【异步】
	 * @param log	日志对象
	 */
	private static void doSave(UniOneLogs logs) {
		log.debug("进入：保存日志信息	【异步】方法,logs:{}",logs);
		
		// token 签名验证失败日志
		if(LogType.Token.value.equals(logs.getTypes()) && StringUtils.isNotEmpty(logs.getIp())) {
			Long iptd = ipCache.getIfPresent(logs.getIp());
			ipCache.put(logs.getIp(), iptd!=null?(iptd+1):1);
			if(iptd!=null && iptd%ipThreshold>0) {
				if(iptd>(ipThreshold*500)) {
					// 如果超出阀值500倍，还出现类似的情况，输出warn日志给出提示
					log.warn("token 签名验证失败日志不做存储，未超过日志阀值,ip:{},iptd:{},value:{}",logs.getIp(),ipThreshold,iptd);
				}else if(iptd>(ipThreshold*50)) {
					// 如果超出阀值50倍，还出现类似的情况，输出info日志给出提示
					log.info("token 签名验证失败日志不做存储，未超过日志阀值,ip:{},iptd:{},value:{}",logs.getIp(),ipThreshold,iptd);
				}else {
					log.debug("token 签名验证失败日志不做存储，未超过日志阀值,ip:{},iptd:{},value:{}",logs.getIp(),ipThreshold,iptd);
				}
				entry.set(null);
				contents.set(null);
				return;
			}
			add("token 签名验证失败日志不做存储，未超过日志阀值,ip:%s,iptd:%s,value:%s",logs.getIp(),ipThreshold,iptd);
		}
		
		// 设置日志操作内容
		StringBuffer buf=getContents();
		if(buf!=null) {
			logs.setContents(buf.toString());
		}
		if(StringUtils.isEmpty(logs.getIp())) {
			log.warn("保存日志信息：操作IP为空");
		}
		
		// 保存日志
		entry.set(null);
		contents.set(null);
		try {
			CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					Results<Long> results=logsApi.save(logs);
					log.debug("业务日志保存结果,result:{},log id:{}",results.isSuccess(),results.getBody());
				}
			});
		} catch (Exception e) {
			log.error("保存业务日志失败,title:{},content:{},ip:{}",logs.getTitle(),logs.getContents(),logs.getIp(),e);
		}
		
	}
	
	
	
}
