package com.unione.cloud.web.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;


/**
 * 	异常统一处理：业务日志
 */
@Slf4j
@RestControllerAdvice("com.unione.cloud")
public class LogsGlobalHandler {

	@ExceptionHandler(Exception.class)
    public Results<?> exception(Exception e) {
		log.error("========= 系统异常:500 ==========");
		log.error(e.getMessage(), e);
		Results<?> result=new Results<>();
		result.setCode(500);
        result.setMessage("系统异常");
        if(!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
        	// 如果未设置日志类型，则认为未记录业务日志
        	LogsUtil.error(e);
        }
        return result;
    }

	@ExceptionHandler(MultipartException.class)
    public Results<?> multipartException(MultipartException e) {
    	log.error("========= 附件上传失败:600 ==========");
    	log.error(e.getMessage(), e);
    	Results<?> result=new Results<>();
		result.setCode(600);
        result.setMessage("附件上传失败");
        if(!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
        	// 如果未设置日志类型，则认为未记录业务日志
        	LogsUtil.error(e);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
	@ExceptionHandler(HttpMessageNotReadableException.class)
    public Results<?> httpMessageNotReadableException(HttpMessageNotReadableException e) {
    	log.error("========= JSON转换异常:700 ==========");
    	log.error(e.getMessage(), e);
    	Results<?> result=new Results<>();
		result.setCode(700);
        result.setMessage("JSON转换异常");
        if(!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
        	// 如果未设置日志类型，则认为未记录业务日志
        	LogsUtil.error(e);
        }
        return result;
    }

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Results<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("========= 参数验证失败:800 ==========");
		log.error(e.getMessage(), e);
		BindingResult bindingResult = e.getBindingResult();
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<String> data = new ArrayList<>();
		StringBuilder sb = new StringBuilder("参数验证失败:");
		for (FieldError error : fieldErrors) {
			Object value = error.getRejectedValue();
			sb.append(error.getField()).append("=").append(value == null ? "null" : value.toString())
			.append("[").append(error.getDefaultMessage()).append("], ");
			data.add(error.getField());
		}
		sb.delete(sb.length() - 2, sb.length());

		Results<?> result=new Results<>();
		result.setCode(800);
		result.setMessage(sb.toString());
		if (!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
			// 如果未设置日志类型，则认为未记录业务日志
			LogsUtil.failure(e);
		}
		return result;
    }
    

	@ExceptionHandler(value = BindException.class)
    public Results<?> bindException(BindException e) {
    	log.error("========= 参数绑定失败:800 ==========");
    	log.error(e.getMessage(), e);
    	Results<?> result=new Results<>();
		result.setCode(800);
        result.setMessage("参数绑定失败");
        if(!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
        	// 如果未设置日志类型，则认为未记录业务日志
        	LogsUtil.failure(e);
        }
        return result;
    }
	
    
	@ExceptionHandler(ServiceException.class)
    public Results<?> businessException(ServiceException e) {
    	log.error(e.getMessage(), e);
		Results<?> result=new Results<>();
		result.setCode(900);
        result.setMessage(e.getMessage());
        if(!StringUtils.isEmpty(LogsUtil.getEntry().getTypes())) {
        	// 如果未设置日志类型，则认为未记录业务日志
        	LogsUtil.failure(e);
        }
        return result;
    }
	
}
