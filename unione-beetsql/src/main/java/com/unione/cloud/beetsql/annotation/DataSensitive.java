package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *	数据脱敏注解：只对字符类型字段进行脱敏
 * @author Jeking 杨
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface DataSensitive {
	
	/**
	 * 脱敏方式
	 * @return
	 */
	SensitiveRule value() default SensitiveRule.ENCRYPT;
	
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

	public static enum SensitiveRule{
		// 正则表达式脱敏，配合自定义表达式
		REGEX,
		// 预设方式，手机号tel
		TEL("^(1[3-9][0-9])\\d{4}(\\d{4}$)","$1****$2"),
		// 预设方式，身份证：idcard		
		IDCARD("^(.{6})(?:\\d+).(.{4})$","$1****$2"),		
		UNAME,		// 预设方式，姓名：uname
		CARLICENSE,	// 中国大陆车牌，包含普通车辆、新能源车辆
		BANKCARD,	// 银行卡号
		ADDRESS,	// 地址
		ENCRYPT;	// 数据加密：使用平台加解密服务SecretInterface完成

		private String express;
		private String replace;
		private SensitiveRule(){}
		private SensitiveRule(String express,String replace){
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
