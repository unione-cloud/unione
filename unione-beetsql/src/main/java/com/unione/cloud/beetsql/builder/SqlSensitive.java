package com.unione.cloud.beetsql.builder;

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
}
