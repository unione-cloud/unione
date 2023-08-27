package com.unione.cloud.core.util;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * SpringContextSupport工具类
 * @author Jeking Yang
 * @version 1.0.0
 */
@Service
public class SpringCtxUtil implements ApplicationContextAware {

    //Spring应用上下文环境
    private  static ApplicationContext _applicationContext;
    private static AutowireCapableBeanFactory beanFactory;
    
	@Override
	@SuppressWarnings("static-access")
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this._applicationContext=applicationContext;
        beanFactory=applicationContext.getAutowireCapableBeanFactory();
    }

    /**
     * 	获取对象   
     * @param <T>
     * @param name
     * @return
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
	public static <T> T getBean(String name) throws BeansException {
        return (T)_applicationContext.getBean(name);
    }

    /**
     * 	获取对象   
     * @param <T>
     * @param clazz
     * @return
     * @throws BeansException
     */
    public  static <T> T getBean(Class<T> clazz) throws BeansException {
        return _applicationContext.getBean(clazz);
    }

    /**
      * 获取类型为requiredType的对象
      * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
      * @param name       bean注册名
      * @param requiredType 返回对象类型
      * @return Object 返回requiredType类型对象
      * @throws BeansException
      */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T getBean(String name, Class requiredType) throws BeansException {
        return (T)_applicationContext.getBean(name, requiredType);
    }
    
    
    /**
     * 	获得指定类型的所有实例集合
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> Map<String, T> getBeans(Class<T> clazz){
    	return _applicationContext.getBeansOfType(clazz);
    }

    /**
     * 	如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true 
     * @param name
     * @return
     */
    public static boolean containsBean(String name) {
        return _applicationContext.containsBean(name);
    }

    /**
      * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
      * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）   
      * @param name
      * @return boolean
      * @throws NoSuchBeanDefinitionException
      */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return _applicationContext.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    @SuppressWarnings("rawtypes")
	public static Class getType(String name) throws NoSuchBeanDefinitionException {
        return _applicationContext.getType(name);
    }

    /**
     * 	如果给定的bean名字在bean定义中有别名，则返回这些别名   
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return _applicationContext.getAliases(name);
    }

    /**
     * 	初始化bean依赖
     * @param bean
     */
    public static void autowireBean(Object bean){
    	beanFactory.autowireBean(bean);
    }

}
