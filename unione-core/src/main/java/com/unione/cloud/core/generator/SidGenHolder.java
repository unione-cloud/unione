package com.unione.cloud.core.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;

@Service
public class SidGenHolder {

	private static SidGenerator sidGenerator;
	
	@Autowired(required=false)
	public void setSidGenerator(SidGenerator sidGenerator) {
		SidGenHolder.sidGenerator=sidGenerator;
	}
	
	/**
	 * 	获得SidGenerator实例
	 * @return
	 */
	public static SidGenerator get() {
		return sidGenerator;
	}
	
	/**
	 * 	生成主键
	 * @return
	 */
	public static long generate() {
		AssertUtil.service().notNull(SidGenHolder.sidGenerator, "sidGenerator服务异常");
		return SidGenHolder.sidGenerator.generate();
	}
	
}
