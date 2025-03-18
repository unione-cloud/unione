package com.unione.cloud.beetsql.builder;

import org.apache.commons.lang3.StringUtils;

import com.unione.cloud.beetsql.annotation.DataSensitive;

import lombok.Data;

@Data
public class SqlSensitive {
	/**
	 * 脱敏类型，对应：DataSensitive.name()
	 */
	private String type;
	/**
	 * 脱敏匹配表达式 
	 * @return
	 */
	String express;
	/**
	 * 脱敏替换表达式
	 * @return
	 */
	String replace;
	
	
	public static SqlSensitive build(DataSensitive dataSensitive) {
		SqlSensitive sensitive=new SqlSensitive();
		sensitive.setType(dataSensitive.value().name());
		if(!StringUtils.isAllEmpty(dataSensitive.express())) {
			sensitive.setExpress(dataSensitive.express());
		}
		if(!StringUtils.isAllEmpty(dataSensitive.replace())) {
			sensitive.setReplace(dataSensitive.replace());
		}
		return sensitive;
	}
	
}
