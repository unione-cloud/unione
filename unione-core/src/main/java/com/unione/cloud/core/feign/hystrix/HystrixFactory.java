package com.unione.cloud.core.feign.hystrix;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @类名    Feign Hystrix 降级抽象工厂
 * @描述    1、可以设置默认降级实现
 *         2、也可以在Spring容器中注册自定义降级，bean name规范为 接口类名（首字母小写）
 * @作者	Jeking Yang
 * @版本	1.0.0
 */
public abstract class HystrixFactory<T> implements FallbackFactory<T>,ApplicationContextAware{

    private static Logger logger=LoggerFactory.getLogger(HystrixFactory.class);

    private T defaultFallback;
    private String fallbackBeanName;
    private Class<T> fallbackClass;
    private ApplicationContext ctx;

    public void setDefaultFallback(T fallback){
        this.defaultFallback=fallback;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx){
        this.ctx=ctx;
    }

    @SuppressWarnings("unchecked")
	public HystrixFactory(){
        // 获得泛型类的Class类型以及该接口的熔断服务bean名称
        Type superClass = getClass().getGenericSuperclass();
        Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            this.fallbackClass = (Class<T>) ((ParameterizedType) type).getRawType();
        } else {
            this.fallbackClass = (Class<T>) type;
        }
        this.fallbackBeanName=(""+this.fallbackClass.getSimpleName().charAt(0)).toLowerCase()+this.fallbackClass.getSimpleName().substring(1);
    }

    @Override
    public T create(Throwable cause) {
        logger.debug("Feign服务[{}]调用失败,启动降级工厂,默认降级服务[{}]", this.fallbackClass.getName(),this.defaultFallback);
        if(logger.isDebugEnabled()){
            cause.printStackTrace();
        }

        // 默认应用 defualt 熔断
        T tmp=this.defaultFallback;

        // 如果Spring融洽中存在自定义熔断，则使用自定义熔断
        if(this.ctx.containsBean(this.fallbackBeanName)){
            tmp=this.ctx.getBean(this.fallbackBeanName,this.fallbackClass);

            logger.debug("Spring容器中存在自定义降级服务，优先使用自定义降级服务 [{}]",tmp);
        }
       
        return tmp;
    }


}