package com.unione.cloud.core.generator;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.util.SpringCtxUtil;

import jakarta.annotation.Resource;

@Service
public class IdGenHolder {

	private static IdGenerator snowflakeGenerator;
	
	@Resource(name="snowflakeGenerator")
	public void setSnowflakeGenerator(IdGenerator snowflakeGenerator) {
		IdGenHolder.snowflakeGenerator=snowflakeGenerator;
	}
	
	/**
	 * 	获得SidGenerator实例
	 * @return
	 */
	public static IdGenerator get(String ...name) {
		if(name.length>0) {
			return SpringCtxUtil.getBean(String.format("%sGenerator", name[0]), IdGenerator.class);
		}
		return snowflakeGenerator;
	}
	
	/**
	 * 	生成主键
	 * @return
	 */
	public static long generate() {
		AssertUtil.service().notNull(IdGenHolder.snowflakeGenerator, "idGenerator服务异常");
		return IdGenHolder.snowflakeGenerator.generate();
	}

	/**
	 * 	生成编码
	 * @param name	生成器名称
	 * @param ctx	上下文参数
	 * @return
	 */
	public static String generate(String name, Map<String,Object> ctx) {
		IdGenerator idGenerator=SpringCtxUtil.getBean(String.format("%sGenerator", name), IdGenerator.class);
		return idGenerator.generate(ctx);
	}
	
}
