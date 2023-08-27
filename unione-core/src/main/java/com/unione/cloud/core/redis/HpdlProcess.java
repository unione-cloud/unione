package com.unione.cloud.core.redis;

/**
 * 	分布式锁处理接口  Hight Process Distributed locks
 * @param <T>
 */
public abstract class HpdlProcess<T> {
	
	private String hpdlName;
	/**
	 * 	分布式锁处理构造方法
	 * @param hpdlName	分布式锁名称
	 */
	public HpdlProcess(String hpdlName) {
		this.hpdlName=hpdlName;
	}
	public String getHpdlName() {
		return hpdlName;
	}
	
	public abstract T process();
}
