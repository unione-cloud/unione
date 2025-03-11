package com.unione.cloud.web.logs;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.beetl.sql.clazz.kit.BeetlSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.exception.RemoteException;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.security.UserRoles.Roles;
import com.unione.cloud.core.util.JsonUtil;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 异常统一处理：业务日志
 */
@Slf4j
@Aspect
@Component
public class LogsHandler {

    @Autowired
    private SessionService sessionService;
    
    @Around("@annotation(com.unione.cloud.core.annotation.Action)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint){
		// 获得Action注解
		Action action = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Action.class);
		LogsUtil.set(action.type(), action.title());
		LogsUtil.enable(!action.nolog());

		// 获得方法名称
		String methodName = String.format("%s.%s", joinPoint.getTarget().getClass().getName(),
				joinPoint.getSignature().getName());
		log.debug("========= 方法:{} 开始执行 ==========", methodName);
		LogsUtil.add("方法:%s 开始执行", methodName);

      	// 获取请求对象
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		if (request != null) {
			log.debug("请求URL: {}", request.getRequestURL().toString());
			log.debug("请求方法: {}", request.getMethod());
			log.debug("请求参数: {}", request.getQueryString());
			log.debug("请求Agent: {}", request.getHeader("User-Agent"));

			LogsUtil.add("请求URL: %s", request.getRequestURL().toString());
			LogsUtil.add("请求方法: %s", request.getMethod());
			LogsUtil.add("请求参数: %s", request.getQueryString());
			LogsUtil.add("请求Agent: %s", request.getHeader("User-Agent"));
		}
		
		// 操作权限验证
		if (ObjectUtil.isNotEmpty(action.roles())) {
			boolean flag = false;
			for (String o : action.roles()) {
				if (sessionService.getUserRoles().contains(o)) {
					flag = true;
					break;
				}
			}
			if(!flag){
				for (String o : action.roles()) {
					Roles prole = UserRoles.fromCode(o);
					if(prole!=null){
						for(String r:sessionService.getUserRoles()){
							Roles arole = UserRoles.fromCode(r);
							if(arole!=null && arole.level()<prole.level()){
								flag = true;
								break;	
							}
						}
					}else if(sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN) || 
						sessionService.getUserRoles().contains(UserRoles.SYSOPSUSER) ||
						sessionService.getUserRoles().contains(UserRoles.TENANTADMIN)){
						flag = true;
						break;
					}
				}	
			}
			if (!flag) {
				LogsUtil.add("无操作权限");
				LogsUtil.add("需要角色: %s", JsonUtil.toJson(action.roles()));
				
				// 无操作权限
				Results<?> res = new Results<>();
				res.setCode(500);
				res.setMessage("无操作权限");
				LogsUtil.error("500","无操作权限");
				return res;
			}
		}
		// 操作权限验证 END
    	
		try {
			// 开始处理
			Object result = joinPoint.proceed();
			// 处理结果
			if (result instanceof Results) {
				Results<?> res = (Results<?>) result;
				if (Objects.equals(res.getCode(), 200)) {
					log.debug("响应状态: {}", res.getCode());
					LogsUtil.add("响应状态: %s",res.getCode());
					 // 获取方法名称
			        LogsUtil.add("方法:%s 执行结束",methodName);
			        log.debug("========= 方法:{} 执行结束 ==========", methodName);
			        
					LogsUtil.success();
				} else {
					log.debug("响应状态: {}", res.getCode());
					LogsUtil.add("响应状态: %s",res.getCode());
					 // 获取方法名称
			        LogsUtil.add("方法:%s 执行结束",methodName);
			        log.debug("========= 方法:{} 执行结束 ==========", methodName);
			        
					LogsUtil.error(String.format("%s", res.getCode()), res.getMessage());
				}
			} else {
				// 获取响应对象
				HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
				if(response!=null){
					log.debug("响应状态: {}", response.getStatus());
					LogsUtil.add("响应状态: %s",response.getStatus());
				}
				// 获取方法名称
		        LogsUtil.add("方法:%s 执行结",methodName);
		        log.debug("========= 方法:{} 执行结束 ==========", methodName);
		        
				LogsUtil.success();
			}
	        
			return result;
		} catch (Throwable e) {
			log.error("处理异常",e);
			Results<?> result = new Results<>();
			result.setCode(500);
			result.setMessage("系统异常");
			if (e instanceof BeetlSQLException) {
	            BeetlSQLException beetException = (BeetlSQLException) e;
	            switch (beetException.code) {
	                case BeetlSQLException.UNIQUE_EXCEPT_ERROR:
	                	result.setMessage("记录未找到");
	                    break;
	                case BeetlSQLException.NOT_UNIQUE_ERROR:
	                	result.setMessage("记录不唯一");
	                    break;
	                default:
	                    break;
	            }
	        } else if(e instanceof ServiceException){
	        	result.setMessage(e.getMessage());
	        } else if(e instanceof RemoteException){
	        	result.setMessage(e.getMessage());
	        } else if(e instanceof DataBaseException){
	        	result.setMessage(e.getMessage());
	        }
	      
			log.debug("响应状态: {}", result.getCode());
            LogsUtil.add("响应状态: %s",result.getCode());
            
	        // 获取方法名称
	        LogsUtil.add("方法:%s 执行失败",methodName);
	        log.debug("========= 方法:{} 执行失败 ==========", methodName,e);
	
	        //保存日志
	        LogsUtil.error(e);
	        return result;
		}
		
    }
    


}
