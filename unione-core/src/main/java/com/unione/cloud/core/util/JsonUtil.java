package com.unione.cloud.core.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unione.cloud.core.exception.ServiceException;

/**
 * 	JSON支撑转换类.
 */
@Service
public class JsonUtil {
	
	private static ObjectMapper  mapper;
	
	@Autowired
	public void setMapper(ObjectMapper mapper) {
		JsonUtil.mapper = mapper;
	}
	
	
	public static <T> T toList(TypeReference<T> typeReference,String json){
        try {
            return (T)mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new ServiceException("JSON 转换异常",e);
        }
    }
	
	public static <T> T toBean(TypeReference<T> typeReference,String json){
        try {
            return (T)mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new ServiceException("JSON 转换异常",e);
        }
    }
    
    public static <T> T toBean(Class<T> c,String json){
        try {
            return (T)mapper.readValue(json, c);
        } catch (Exception e) {
            throw new ServiceException("JSON 转换异常",e);
        }
    }
    
    public static String toJson(Object pojo){
        try {
            return mapper.writeValueAsString(pojo);
        } catch (IOException e) {
            throw new ServiceException("JSON 转换异常",e);
        }
    }
    
}

