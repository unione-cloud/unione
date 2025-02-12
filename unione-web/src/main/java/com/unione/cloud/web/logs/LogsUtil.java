package com.unione.cloud.web.logs;

import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.exception.RemoteException;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.web.logs.model.SysLogs;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class LogsUtil {
	
	private static String     appCode;
	private static DataBaseDao dataBaseDao;
	private static LogsApi     logsApi;
	private static boolean    IS_CLOUD_MODEL;
	
	@Autowired
	public void setDataBaseDao(DataBaseDao dataBaseDao) {
		LogsUtil.dataBaseDao = dataBaseDao;
	}
	
	@Value("${spring.application.name}")
	public void setAppSn(String appCode) {
		LogsUtil.appCode=appCode;
	}
	
	@Value("${unione.cloud.model:false}")
	public void setIsCloudModel(boolean flag) {
		LogsUtil.IS_CLOUD_MODEL=flag;
	}
	
	@Autowired(required = false)
	public void setLogsApi(LogsApi logsApi) {
		LogsUtil.logsApi=logsApi;
	}
	
	/**
	 * 	用户会话对象
	 */
	private static SessionService sessionService;
	@Autowired
	public void setSessionService(SessionService sessionService) {
		LogsUtil.sessionService = sessionService;
	}
	
	/**
	 * 	日志开启状态，默认true
	 */
	private static boolean OPEN_STATUS;
	@Value("${ibls.server.log.open:true}")
	public void setOpenStatus(boolean flag) {
		LogsUtil.OPEN_STATUS=flag;
	}
	
	/**
	 * 	是否过滤掉查询日志，默认false
	 */
	private static boolean NO_QUERY_LOG;
	@Value("${ibls.server.log.noquery:false}")
	public void setNoQueryLog(boolean flag) {
		LogsUtil.NO_QUERY_LOG=flag;
	}
	
	/**
	 * 	不保存的操作表达式：操作名称正则表达式,eg:查询|浏览
	 */
	private static Pattern executionsPattern;
	@Value("${ibls.server.save.executions:}")
	public void setOptExecutions(String executions) {
		executionsPattern=null;
		if(!StringUtils.isEmpty(executions)) {
			executionsPattern=Pattern.compile(executions);
		}
	}
	
	private static ThreadPoolExecutor executor;
	@Value("${ibls.server.threadpool.size:10}")
	public void setThreadPoolSize(int size) {
		executor = new ThreadPoolExecutor(size, 20, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
	}
	
	
	// 日志记录对象
	private static ThreadLocal<SysLogs> entry=new ThreadLocal<>();
	// 日志内容对象
	private static ThreadLocal<StringBuffer> contents=new ThreadLocal<>();
	
	/**
	 * 	获得请求对象
	 * @return
	 */
	private static HttpServletRequest getRequest() {
		HttpServletRequest req=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		return req;
	}
	
	/**
	 * 	获得响应对象
	 * @return
	 */
	private static HttpServletResponse getResponse() {
		HttpServletResponse res=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
		return res;
	}
	
	/**
	 * 	设置响应cookie
	 * @param name
	 * @param value
	 */
	private static void setCookie(String name,String value) {
		HttpServletResponse res=getResponse();
		if(res==null) {
			return;
		}
		Cookie ck=new Cookie(name,value);
		res.addCookie(ck);
	}
	
	private static String getCookie(String name) {
		HttpServletRequest req=getRequest();
		if(req==null || req.getCookies()==null) {
			return null;
		}
		for(Cookie ck:req.getCookies()) {
			if(name.equals(ck.getName())) {
				return ck.getValue();
			}
		}
		return null;
	}
	
	/**
	 * 	获得请求 IP 地址
	 * @author YangGuangJian <br>
	 * @return
	 */
	public static String getClientIp() {
		HttpServletRequest req=getRequest();
		if(req==null) {
			return null;
		}
		
		String ip = null;
		Enumeration<String> enu = req.getHeaderNames();
		while (enu.hasMoreElements()) {
			String name = enu.nextElement();
			if (name.equalsIgnoreCase("X-Forwarded-For") || name.equalsIgnoreCase("X-Real-IP") || 
					name.equalsIgnoreCase("Proxy-Client-IP") || name.equalsIgnoreCase("WL-Proxy-Client-IP") || 
					name.equalsIgnoreCase("RemoteAddress")) {
				ip = req.getHeader(name);
			}
			if (ip != null && ip.length() >= 0 && !"unknown".equalsIgnoreCase(ip)) {
				break;
			}
		}
		if (ip == null || ip.length() == 0) {
			ip = req.getRemoteAddr();
		}
		if (StringUtils.isNotEmpty(ip)) {
			ip = ip.split(",")[0];
		}
		log.debug("退出方法:获得请求 IP 地址:{}",ip);
		return ip;
	}
	
	/**
	 * 获得日志对象
	 * @return
	 */
	public static SysLogs getEntry() {
		SysLogs ent=entry.get();
		if(ent==null) {
			contents.set(null);
			ent=new SysLogs();
			ent.setAppSn(appCode);
			ent.setCreated(DateUtil.date());
			ent.setStartTime(DateUtil.date());
			if(sessionService.getPrincipal()!=null) {
				ent.setTenantId(sessionService.getTenantId());
				ent.setOrgId(sessionService.getOrgId());
				ent.setUserId(sessionService.getUserId());
				ent.setUserName(sessionService.getRealname());
				ent.setCreatedBy(sessionService.getUserId());
				ent.setLastUpdatedBy(sessionService.getUserId());
			}
			entry.set(ent);
			
			// 从request中获取请求ID
			HttpServletRequest req=getRequest();
			if(req!=null) {
				String actionid=req.getHeader("_unione_actionid");
				if(StringUtils.isEmpty(actionid)) {
					actionid=getCookie("_unione_actionid");
				}
				if(!StringUtils.isEmpty(actionid)) {
					ent.setActionId(Long.parseLong(actionid));
				}else {
					ent.setActionId(IdGenHolder.generate());
				}
				setCookie("_unione_actionid", ent.getActionId()+"");
				
				String requestid=req.getHeader("_unione_requestid");
				if(StringUtils.isEmpty(requestid)) {
					requestid=getCookie("_unione_requestid");
				}
				if(!StringUtils.isEmpty(requestid)) {
					ent.setPrequestId(Long.parseLong(requestid));
				}
				ent.setRequestId(IdGenHolder.generate());
				setCookie("_unione_requestid", ent.getRequestId()+"");
			}else {
				ent.setRequestId(IdGenHolder.generate());
				ent.setActionId(IdGenHolder.generate());
			}
			
			// 保存请求信息到session
			sessionService.setVar("_unione_actionid", ent.getActionId());
			sessionService.setVar("_unione_requestid", ent.getRequestId());
		}
		return ent;
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
	public static void set(ActionType type,String title) {
		SysLogs log=getEntry();
		log.setTypes(type.value());
		log.setTitle(title);
	}
	
	
	/**
	 * 设置日志信息
	 * @param type				操作类型
	 * @param title				操作标题
	 * @param targetId			目标ID
	 */
	public static void set(ActionType type,String title,Long targetId) {
		set(type, title);
		setTarget(targetId);
	}
	
	public static void setUsername(String username) {
		SysLogs log=getEntry();
		log.setUserName(username);
	}
	
	/**
	 * 设置扩展数据
	 * @param extData	标准的JSON对象字符串
	 */
	public static void setExtData(String extData) {
		SysLogs log=getEntry();
		log.setExtData(extData);
	}
	
	
	/**
	 * 设置目标信息
	 * @param targetId			目标ID
	 * @param targetTitle		目标标题
	 */
	public static void setTarget(Long targetId,String... targetTitle) {
		SysLogs log=getEntry();
		log.setTargetId(targetId);
		if(targetTitle.length>0) {
			log.setTargetTitle(targetTitle[0]);
		}
	}
	
	/**
	 * 	添加操作内容
	 * @param content
	 */
	public static void add(String content) {
		StringBuffer buf=getContents();
		buf.append(DateUtil.format(DateUtil.date(), "YYYY-MM-dd HH:mm:ss.S"))
		   .append("\t").append(content).append("\n");
	}
	
	/**
	 * 	添加操作内容
	 * @param content
	 * @param params
	 */
	public static void add(String content,Object... params) {
		StringBuffer buf=getContents();
		buf.append(DateUtil.format(DateUtil.date(), "YYYY-MM-dd HH:mm:ss.S"))
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
		SysLogs log=getEntry();
		log.setStatus(isSuccess?1:2);
		log.setEndTime(DateUtil.date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	
	 * @param isSuccess		true：成功/false：失败
	 * @param targetId
	 * @param targetTitle
	 */
	public static void save(boolean isSuccess, Long targetId) {
		SysLogs log=getEntry();
		setTarget(targetId);
		log.setStatus(isSuccess?1:2);
		log.setEndTime(DateUtil.date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	【成功】
	 */
	public static void success() {
		SysLogs log=getEntry();
		log.setStatus(1);
		log.setEndTime(DateUtil.date());
		doSave(log);
	}
	
	/**
	 * 保存日志信息	【成功】
	 * @param targetId
	 */
	public static void success(Long targetId) {
		SysLogs log=getEntry();
		setTarget(targetId);
		log.setStatus(1);
		log.setEndTime(DateUtil.date());
		doSave(log);
	}
	
	/**
	 * 	保存日志信息		【失败】
	 * @param e			异常对象
	 */
	public static void failure(Exception e) {
		add(e.getMessage());
		SysLogs logs=getEntry();
		logs.setStatus(2);
		logs.setEndTime(DateUtil.date());
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
	 * 	保存日志信息		【失败】
	 * @param errorMessage
	 * @param e
	 */
	public static void failure(String errorMessage,Exception e) {
		add(errorMessage);
		SysLogs logs=getEntry();
		logs.setErrorMessage(errorMessage);
		logs.setStatus(2);
		logs.setEndTime(DateUtil.date());
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
		SysLogs log=getEntry();
		log.setStatus(2);
		log.setEndTime(DateUtil.date());
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
		SysLogs log=getEntry();
		log.setStatus(3);
		log.setEndTime(DateUtil.date());
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
		SysLogs logs=getEntry();
		logs.setStatus(3);
		logs.setEndTime(DateUtil.date());
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
		SysLogs logs=getEntry();
		logs.setStatus(3);
		logs.setEndTime(DateUtil.date());
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
	private static void doSave(SysLogs logs) {
		log.debug("进入：保存日志信息	【异步】方法,logs:{}",logs);
		// 设置日志操作内容
		StringBuffer buf=getContents();
		if(buf!=null) {
			logs.setContents(buf.toString());
		}
		// 设置ip地址
		if(StringUtils.isEmpty(logs.getIp())) {
			HttpServletRequest req=getRequest();
			if(req!=null) {
				// 获取IP
				String ip=getClientIp();
				logs.setIp(ip);
			}
		}
		if(StringUtils.isEmpty(logs.getIp())) {
			log.warn("保存日志信息：操作IP为空");
		}
		if(logs.getUserId()==null) {
			logs.setUserId(sessionService.getUserId());
			logs.setUserName(sessionService.getUsername());
		}
		
		// 异步保存日志
		entry.remove();
		try {
			String token=sessionService.getToken();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					log.debug("异步保存日志信息线程启动成功,thread:{}",this);
					log.debug("异步保存日志信息开始>>");
					if(IS_CLOUD_MODEL) {
						HystrixRequestContext.initializeContext();
						SessionHolder.setToken(token);
						Results<Long> results = logsApi.save(logs);
						logs.setId(results.getBody());
					}else {
						dataBaseDao.insert(logs);
					}
					log.debug("异步保存日志信息完成>>log id:{}",logs.getId());
				}
			};
			
			// 判断是否要保存日志
			if(OPEN_STATUS && (NO_QUERY_LOG==false || 
					!ActionType.Query.value().equals(logs.getTypes()))) {
				if(executionsPattern!=null) {
					// 如果设置了 忽略表达式，则验证当前操作名称是否满足忽略条件
					Matcher m = executionsPattern.matcher(logs.getTitle());
					if(!m.find()) {
						executor.execute(runnable);
					}else {
						log.info("设置了 忽略表达式:{},group:{},日志不提交保存",executionsPattern,m.group());
					}
				}else {
					executor.execute(runnable);
				}
			}
		} catch (Exception e) {
			log.error("异步保存日志失败",e);
		}
		
		log.info("业务日志json：{}",JSONUtil.toJsonStr(logs));
		log.debug("退出：保存日志信息	【异步】方法,logs:{},ThreadQueue:{}",logs,executor.getQueue().size());
	}
	
	
	
	
}
