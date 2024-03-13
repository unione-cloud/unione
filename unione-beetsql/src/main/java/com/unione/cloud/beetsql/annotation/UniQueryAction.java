package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.unione.cloud.beetsql.builder.SqlAction;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface UniQueryAction {
	
	SqlAction value() default SqlAction.EQ;
	
}
