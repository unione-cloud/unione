package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface UniDataSensitive {
	
	/**
	 * 脱敏方式
	 * @return
	 */
	DataSensitive value() default DataSensitive.ENCRYPT;
	
	/**
	 * 脱敏匹配表达式 
	 * @return
	 */
	String express();

	/**
	 * 脱敏替换表达式
	 * @return
	 */
	String replace();

	public static enum DataSensitive{
		// 正则表达式脱敏，配合自定义表达式
		REGEX,
		// 预设方式，手机号tel
		TEL("^(1[3-9][0-9])\\d{4}(\\d{4}$)","$1****$2"),
		// 预设方式，身份证：idcard		
		IDCARD("^(.{6})(?:\\d+).(.{4})$","$1****$2"),		
		UNAME,		// 预设方式，姓名：uname
		ENCRYPT;	// 数据加密：使用平台加解密服务SecretInterface完成

		private String express;
		private String replace;
		private DataSensitive(){}
		private DataSensitive(String express,String replace){
			this.express=express;
			this.replace=replace;
		}

		public String express(){
			return this.express;
		}
		public String replace(){
			return this.replace;
		}
	}
}
