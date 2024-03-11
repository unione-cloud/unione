package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface UniQueryLike {
	
	TYPE value() default TYPE.ALL;
	
	public static enum TYPE{
		ALL,LEFT,RIGHT
	}

	
}
