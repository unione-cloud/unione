package com.unione.cloud.core.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.unione.cloud.core.dto.Results;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ExceptionHandle {
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Results<Void> handleCustomException(Exception e) {
        Results<Void> result=Results.error(e.getMessage());
        result.setCode(500);
        log.error("异常信息",e);
        return result;
    }
}
