package com.unione.cloud.core.util;

import java.util.Map;

import com.unione.cloud.core.exception.ServiceException;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BeanUtils extends BeanUtil{

	
	/**
	 * 复制对象
	 */
	public static void copy(Object from,Object to,String ...fields) {
		if(fields==null || fields.length==0) {
			BeanUtils.copyProperties(from, to,CopyOptions.create().setIgnoreNullValue(true));
		}else {
			for(String field:fields) {
				Object value=BeanUtil.getProperty(from, field);
				BeanUtil.setProperty(to, field, value);
			}
		}
	}
	
	
	/**
	 * 设置默认值
	 * @param bean
	 * @param fieldName
	 * @param value
	 */
	public static void setDefaultValue(Object bean,String fieldName,Object value) {
		Object v = getFieldValue(bean, fieldName);
		if(v==null) {
			setFieldValue(bean, fieldName, value);
		}
	}
	
	/**
	 * 设置默认值
	 * @param bean
	 * @param defValue
	 */
	public static void setDefaultValue(Object bean,Map<String,Object> defValue) {
		try {
			org.apache.commons.beanutils.BeanUtils.populate(bean, defValue);
		} catch (Exception e) {
			log.error("默认值设置失败",e);
			throw new ServiceException("默认值设置失败",e);
		}
	}
	
	
}
