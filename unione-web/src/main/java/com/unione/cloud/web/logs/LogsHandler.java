package com.unione.cloud.web.logs;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.beetl.sql.clazz.kit.BeetlSQLException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.unione.cloud.core.dto.Results;

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

    @Pointcut("@annotation(com.unione.cloud.core.annotation.Action)")
    public void logPointcut() {}

    @Before("logPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("========= 方法:{}.{}开始执行 ==========", joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
		HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if(request!=null){
			log.info("请求URL: {}", request.getRequestURL().toString());
			log.info("请求方法: {}", request.getMethod());
			log.info("请求参数: {}", request.getQueryString());
			log.info("请求头: {}", request.getHeader("User-Agent"));
		}
    }

    @After("logPointcut()")
    public void logAfter(JoinPoint joinPoint) {
		HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
		if(response!=null){
			log.info("响应状态: {}", response.getStatus());
		}
		log.info("========= 方法:{}.{}执行完毕 ==========", joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("========= 方法执行成功: {} ==========", joinPoint.getSignature().toShortString());
        log.info("返回结果: {}", result);
        LogsUtil.success();
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Exception e) {
        log.error("========= 系统异常:500 ==========");
        log.error(e.getMessage(), e);
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
        }

        LogsUtil.error(e);
    }


}
