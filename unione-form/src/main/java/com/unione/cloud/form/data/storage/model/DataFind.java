package com.unione.cloud.form.data.storage.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.unione.cloud.core.dto.Params;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel("数据存储：数据查询对象")
public class DataFind extends Params<Map<String,Object>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 669876047104921384L;
	
	@ApiModelProperty(value="数据定义编码",notes = "")
	private String dsn;
	
	@ApiModelProperty(value="数据定义版本号",notes = "")
	private Integer vers;

	@ApiModelProperty(value="数据字段集合",notes = "查询指定字段信息，如果为空则是所有字段，字段别名：驼峰")
	private List<String> fields=new ArrayList<>();
	
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
