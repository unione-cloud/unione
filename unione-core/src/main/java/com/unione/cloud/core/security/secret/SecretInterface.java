package com.unione.cloud.core.security.secret;

public interface SecretInterface {

	/**
	 * 数据加密
	 * @param data
	 * @return
	 */
	public String encrypt(String data);
	
	
	/**
	 * 数据解密
	 * @param data
	 * @return
	 */
	public String decrypt(String data);
	
	
	/**
	 * 哈希计算
	 * @param data
	 * @return
	 */
	public String hash(String data);
	
	
}
