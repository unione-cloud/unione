package com.unione.cloud.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface Action {
	
	/**
	 * 操作标题
	 * @return
	 */
	String title();
	
	/**
	 * 操作类型
	 * @return
	 */
	ActionType type();
	
	/**
	 * 预期角色编码列表，当前用户必须拥有其中某一个角色
	 * @return
	 */
	String[] roles() default {};
	
	/**
	 * 是否不记录操作日志
	 * @return
	 */
	boolean nolog() default false;
}
