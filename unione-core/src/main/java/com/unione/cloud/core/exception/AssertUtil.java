package com.unione.cloud.core.exception;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssertUtil {
	
	private String type;
	
	private AssertUtil(String type) {
		this.type=type;
	}
	
	public static AssertUtil service() {
		return new AssertUtil("service");
	}
	public static AssertUtil remote() {
		return new AssertUtil("remote");
	}
	public static AssertUtil database() {
		return new AssertUtil("database");
	}

	public AssertUtil notNull(Object value,String message) {
		if(value==null) {
			buildExpception(message);
		}
		if (value instanceof CharSequence) {
			if(value.toString().trim().length()==0) {
				buildExpception(message);
			}
		}
		return this;
	}
	
	public AssertUtil notNull(Object value,String errMsg,String errCode) {
		if(value==null) {
			buildExpception(errMsg,errCode);
		}
		if (value instanceof CharSequence) {
			if(value.toString().trim().length()==0) {
				buildExpception(errMsg,errCode);
			}
		}
		return this;
	}
	
	public AssertUtil notNull(Object object,String props[],String message) {
		if(object==null) {
			buildExpception("对象不能为空");
		}
		
		for(String field:props) {
			Object v=getProperty(object, field);
			if(v==null) {
				buildExpception(String.format(message, field));
			}
			if (v instanceof CharSequence) {
				if(v.toString().trim().length()==0) {
					buildExpception(String.format(message, field));
				}
			}
		}
		return this;
	}
	
	public AssertUtil notEq(Object object,Object target,String message) {
		if(object!=null && !object.equals(target)) {
			buildExpception(message);
		}
		if(target!=null && !target.equals(object)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil notEq(Object object,Object target,String errMsg,String errCode) {
		if(object!=null && !object.equals(target)) {
			buildExpception(errMsg,errCode);
		}
		if(target!=null && !target.equals(object)) {
			buildExpception(errMsg,errCode);
		}
		return this;
	}
	
	public AssertUtil notIn(Object object,Object ins[],String message) {
		if(!Arrays.asList(ins).contains(object)) {
			buildExpception(message);
		}
		return this;
	}
	
	/**
	 * 验证是否集合array中所有元素都不在collection集合中,即：如果array中有一个元素在collection中，则正常
	 * @param array
	 * @param collection
	 * @param message
	 * @return
	 */
	public AssertUtil notIn(Object[] array,Object[] collection,String message) {
		boolean flag=false;
		List<Object> list=Arrays.asList(collection);
		for(Object o:array) {
			if(list.contains(o)) {
				flag=true;
				break;
			}
		}
		if(!flag) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil notIn(Object object,Collection<?> ins,String message) {
		if(!ins.contains(object)) {
			buildExpception(message);
		}
		return this;
	}
	
	/**
	 * 验证是否集合array中所有元素都不在collection集合中,即：如果array中有一个元素在collection中，则正常
	 * @param array
	 * @param collection
	 * @param message
	 * @return
	 */
	public AssertUtil notIn(Object[] array,Collection<?> collection,String message) {
		boolean flag=false;
		for(Object o:array) {
			if(collection.contains(o)) {
				flag=true;
				break;
			}
		}
		if(!flag) {
			buildExpception(message);
		}
		return this;
	}

	public AssertUtil isEq(Object object,Object target,String message) {
		if(ObjectUtil.equal(object, target)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil isIn(Object object,Object ins[],String message) {
		if(Arrays.asList(ins).contains(object)) {
			buildExpception(message);
		}
		return this;
	}
	
	
	public AssertUtil isIn(Object object,Collection<Object> ins,String message) {
		if(ins.contains(object)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil isGt(Number value,Number target,String message) {
		if(value!=null && target!=null && value.longValue()>target.longValue()) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil isGte(Number value,Number target,String message) {
		if(value!=null && target!=null && value.longValue()>=target.longValue()) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil isTrue(boolean expression, String message) {
		if (!expression) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil isTrue(boolean expression, String errMsg,String errCode) {
		if (!expression) {
			buildExpception(errMsg,errCode);
		}
		return this;
	}
	
	public AssertUtil hasLength(@Nullable String text, String message) {
		if (!StringUtils.hasLength(text)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil hasText(@Nullable String text, String message) {
		if (!StringUtils.hasText(text)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil doesNotContain(@Nullable String textToSearch, String substring, String message) {
		if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
				textToSearch.contains(substring)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil notEmpty(@Nullable Object[] array, String message) {
		if (ObjectUtils.isEmpty(array)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil notEmpty(@Nullable Collection<?> collection, String message) {
		if (CollectionUtils.isEmpty(collection)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil notEmpty(@Nullable Map<?, ?> map, String message) {
		if (CollectionUtils.isEmpty(map)) {
			buildExpception(message);
		}
		return this;
	}
	
	public AssertUtil noNullElements(@Nullable Object[] array, String message) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					buildExpception(message);
				}
			}
		}
		return this;
	}
	
	public AssertUtil noNullElements(@Nullable Collection<?> collection, String message) {
		if (collection != null) {
			for (Object element : collection) {
				if (element == null) {
					buildExpception(message);
				}
			}
		}
		return this;
	}
	
	public AssertUtil noNullElements(@Nullable Map<?, ?> map, String message) {
		if (map != null) {
			for (Object element : map.values()) {
				if (element == null) {
					buildExpception(message);
				}
			}
		}
		return this;
	}
	
	
	private static Object getProperty(Object bean, String name) {
		try {
			Field targetField = getField(bean.getClass(), name);
			if(targetField==null){
				return null;
			}
			return FieldUtils.readField(targetField, bean, true);
		} catch (IllegalAccessException e) {
			log.error("获得对象属性异常,bean:{},name:{}",bean,name,e);
		}
		return null;
	}
	
	/**
	 * 获得类属性Field对象
	 * @param bean
	 * @param name
	 * @return
	 */
	private static Field getField(Class<?> bean, String name) {
		Field field = null;
		try {
			if (Object.class.equals(bean)) {
				return field;
			}
			field = FieldUtils.getDeclaredField(bean, name, true);
			if (field == null) {
				field = getField(bean.getSuperclass(), name);
			}
		} catch (Exception e) {
			log.error("获得类属性Field对象",e);
		}
		return field;
	}
	
	
	/**
	 * 构建异常信息
	 * @param message
	 */
	private void buildExpception(String message) {
		switch (type) {
		case "service":
			throw new ServiceException(message);
		case "remote":
			throw new RemoteException(message);
		case "database":
			throw new DataBaseException(message);
		}
	}
	
	/**
	 * 构建异常信息
	 * @param message
	 */
	private void buildExpception(String message,String errCode) {
		switch (type) {
		case "service":
			throw new ServiceException(errCode,message);
		case "remote":
			throw new RemoteException(errCode,message);
		case "database":
			throw new DataBaseException(errCode,message);
		}
	}
	
}
