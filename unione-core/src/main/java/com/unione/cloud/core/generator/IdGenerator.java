package com.unione.cloud.core.generator;

import java.util.Map;

/**
 * @描述 主键生成器接口
 * @作者 Jeking Yang
 * @版本 1.0.0
 */
public interface IdGenerator {

	/**
	 * 生成id
	 * @return
	 */
	public default Long generate(){return null;};

	/**
	 * 生成id
	 * @param ctx
	 * @return
	 */
	public default String generate(Map<String,Object> ctx){
		return generate().toString();
	}

}
