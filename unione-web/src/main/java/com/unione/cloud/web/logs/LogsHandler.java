package com.unione.cloud.web.logs;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.beetl.sql.clazz.kit.BeetlSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;

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

    @Pointcut("@annotation(com.unione.cloud.core.annotation.Action)")
    public void logPointcut() {}

    @Before("logPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        // 获得Action注解
        Action action = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(Action.class);
        LogsUtil.set(action.type(),action.title());

        // 获得方法名称
        String methodName = String.format("%s.%s", joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
        log.info("========= 方法:{} 开始执行 ==========", methodName);
        LogsUtil.add("方法:%s 开始执行",methodName);


        // 操作权限验证
        if(ObjectUtil.isNotEmpty(action.roles())){
        	AssertUtil.service().notIn(action.roles(),sessionService.getUserRoles(),"没有操作权限");
        }

        // 获取请求对象
		HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if(request!=null){
			log.info("请求URL: {}", request.getRequestURL().toString());
			log.info("请求方法: {}", request.getMethod());
			log.info("请求参数: {}", request.getQueryString());
			log.info("请求Agent: {}", request.getHeader("User-Agent"));

            LogsUtil.add("请求URL: %s",request.getRequestURL().toString());
            LogsUtil.add("请求方法: %s",request.getMethod());
            LogsUtil.add("请求参数: %s",request.getQueryString());
            LogsUtil.add("请求Agent: %s",request.getHeader("User-Agent"));
		}
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        // 获取响应对象
        HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
		if(response!=null){
            log.info("响应状态: {}", response.getStatus());
            LogsUtil.add("响应状态: %s",response.getStatus());
		}
        
        // 获取方法名称
        String methodName = String.format("%s.%s", joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
        LogsUtil.add("方法:%s 开始完毕",methodName);
		log.info("========= 方法:{} 执行完毕 ==========", methodName);

        //保存日志
        if(result instanceof Results){
            Results<?> res = (Results<?>)result;
            if(res.getCode()==200){
                LogsUtil.success();	
            }else{
                LogsUtil.error(String.format("%s",res.getCode()),res.getMessage());
           }	
        }else{
            LogsUtil.success();
        }
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Exception e) {
        
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

        // 获取响应对象
        HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        if(response!=null){
            log.info("响应状态: {}", response.getStatus());
            LogsUtil.add("响应状态: %s",response.getStatus());
        }
      
        // 获取方法名称
        String methodName = String.format("%s.%s", joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
        LogsUtil.add("方法:%s 执行失败",methodName);
        log.info("========= 方法:{} 执行失败 ==========", methodName,e);

        //保存日志
        LogsUtil.error(e);

        Object obj = joinPoint.getTarget();
        log.info("target", obj);
        //设置方法返回值
        //判断返回值类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
       
        if(methodSignature.getReturnType().equals(Results.class)){
            // 设置返回值
            
        }
    }


}
