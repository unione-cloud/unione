package com.unione.cloud.core.security.secret;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.util.SpringCtxUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * 统一密码服务
 * 1、提供统一的加解密服务
 * 2、支持软国密
 * 3、支持适配各商业密码机
 * 4、通过配置化方式是使用具体的密码服务
 * @author Jeking 杨
 */
@Slf4j
@Service
@RefreshScope
public class SecretService {

	
	/**
	 * 加密服务，默认soft：软加密，其他：指定的加密服务
	 */
	@Value("${security.secret.service:soft}")
	private String ENCRYPTION;
	
	/**
	 * 密码服务开关，默认开启
	 */
	@Value("${security.secret.enable:true}")
	private boolean SECRET_ENABLE;
	
	
	/**
	 * 数据加密
	 * @param data
	 * @return
	 */
	public String encrypt(String data) {
		log.debug("进入:数据加密方法,SECRET_ENABLE:{}",SECRET_ENABLE);
		if(!SECRET_ENABLE || StringUtils.isBlank(data)) {
			return data;
		}
		AssertUtil.service().notNull(data, "需要加密的数据不能为空");
		
		SecretInterface smi=SpringCtxUtil.getBean(String.format("%sSecretService", ENCRYPTION), SecretInterface.class);
		AssertUtil.service().notNull(smi, String.format("[%s]加密服务未找到", ENCRYPTION));
		
		String result=null;
		try {
			result=smi.encrypt(data);
			AssertUtil.service().notNull(result, "数据加密失败");
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("数据加密失败",e);
			throw new ServiceException("数据加密失败",e);
		}
		
		log.debug("退出:数据加密方法");
		return result;
	}
	
	
	/**
	 * 数据解密
	 * @param data
	 * @return
	 */
	public String decrypt(String data) {
		log.debug("进入:数据解密方法,SECRET_ENABLE:{}",SECRET_ENABLE);
		if(!SECRET_ENABLE || StringUtils.isBlank(data)) {
			return data;
		}
		AssertUtil.service().notNull(data, "需要解密的数据不能为空");
		
		SecretInterface smi=SpringCtxUtil.getBean(String.format("%sSecretService", ENCRYPTION), SecretInterface.class);
		AssertUtil.service().notNull(smi, String.format("[%s]加密服务未找到", ENCRYPTION));
		
		String result=null;
		try {
			result=smi.decrypt(data);
			AssertUtil.service().notNull(result, "数据解密失败");
		} catch (Exception e) {
			log.debug("数据解密失败,data:{}",data,e);
			return data;
		}
		
		log.debug("退出:数据解密方法");
		return result;
	}
	
	
	/**
	 * 哈希计算
	 * @param data
	 * @return
	 */
	public String hash(String data) {
		log.debug("进入:哈希计算方法");
		AssertUtil.service().notNull(data, "需要哈希计算的数据不能为空");
		
		SecretInterface smi=SpringCtxUtil.getBean(String.format("%sSecretService", ENCRYPTION), SecretInterface.class);
		AssertUtil.service().notNull(smi, String.format("[%s]加密服务未找到", ENCRYPTION));
		
		String result=null;
		try {
			result=smi.hash(data);
			AssertUtil.service().notNull(result, "哈希计算失败");
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("哈希计算失败",e);
			throw new ServiceException("哈希计算失败",e);
		}
		
		log.debug("退出:哈希计算方法");
		return result;
	}
	
	
	
}
