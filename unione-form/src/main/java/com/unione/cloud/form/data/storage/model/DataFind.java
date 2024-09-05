package com.unione.cloud.form.data.storage.model;

import java.util.Map;

import com.unione.cloud.core.dto.Params;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataFind extends Params<Map<String,Object>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 669876047104921384L;

	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param cls
	 * @return
	 */
	public static <P> Params<P> build(Class<P> cls){
		try {
			P body=(P)cls.getDeclaredConstructor().newInstance();
			Params<P> param=new Params<P>();
			param.setBody(body);
			return param;
		} catch (Exception e) {
			log.error("构建参数对象异常",e);
			throw new RuntimeException("构建参数对象异常");
		}
	}
	
	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param body
	 * @return
	 */
	public static <P> Params<P> build(P body){
		Params<P> params=new Params<P>();
		params.setBody(body);
		return params;
	}
	
}
