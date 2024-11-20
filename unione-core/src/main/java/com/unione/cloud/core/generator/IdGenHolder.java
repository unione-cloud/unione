package com.unione.cloud.core.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;

@Service
public class IdGenHolder {

	private static IdGenerator sidGenerator;
	
	@Autowired(required=false)
	public void setSidGenerator(IdGenerator sidGenerator) {
		IdGenHolder.sidGenerator=sidGenerator;
	}
	
	/**
	 * 	获得SidGenerator实例
	 * @return
	 */
	public static IdGenerator get() {
		return sidGenerator;
	}
	
	/**
	 * 	生成主键
	 * @return
	 */
	public static long generate() {
		AssertUtil.service().notNull(IdGenHolder.sidGenerator, "sidGenerator服务异常");
		return IdGenHolder.sidGenerator.generate();
	}
	
}
