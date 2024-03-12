package com.unione.cloud.beetsql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface UniQueryAction {
	
	ACTION value() default ACTION.EQ;
	
	public static enum ACTION{
		EQ(" = "),LT(" < "),LTE(" <= "),GT(" > "),GTE(" >= ");
		
		private ACTION(String express) {
			this.express=express;
		}
		
		private String express;
		public String express() {
			return this.express;
		}
	}
}
