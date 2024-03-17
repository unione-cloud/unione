package com.unione.cloud.core.security.secret.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.secret.SecretInterface;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.HMac;
import lombok.extern.slf4j.Slf4j;


/**
 * 默认软加解密服务，采用hutool工具国密算法实现
 * @author Jeking 杨
 */
@Slf4j
@Service
@RefreshScope
public class SoftSecretService implements SecretInterface {
	
	private SM2 sm2;
	private HMac hmac;
	
	@Value("${secret.soft.sm2keys:MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgy/b+7xHanQfWlOR0InSSWWa9P8JtkwUOWRegrPCbIqGgCgYIKoEcz1UBgi2hRANCAAQo3Xe0yQ0NPhyFYWOBUjBGlwbK/tMt06XwZdiXCY4TJL5RmqP9CADvEud+2nypPBYYU7eqHGk676noK4MhmPJI@MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEKN13tMkNDT4chWFjgVIwRpcGyv7TLdOl8GXYlwmOEyS+UZqj/QgA7xLnftp8qTwWGFO3qhxpOu+p6CuDIZjySA==}")
	public void setSm2Keys(String keys) {
		sm2=null;
		if(!StringUtils.isEmpty(keys)) {
			String tmps[]=keys.trim().split("@");
			AssertUtil.service().isTrue(tmps.length==2, "秘钥服务配置错误，secret.soft.sm2keys配置方式为： {priKey}@{pubKey}");
			sm2=SmUtil.sm2(tmps[0], tmps[1]);
		}
	}
	
	@Value("${secret.soft.hmackey:DmiaqxvLEIL8xIYFvYa2}")
	public void setHmacKeys(String key) {
		hmac=null;
		if(!StringUtils.isEmpty(key)) {
			try {
				hmac=SmUtil.hmacSm3(key.getBytes("UTF-8"));
			} catch (Exception e) {
				log.error("hmac初始化失败,请检查配置项：secret.soft.hmackey,value:{}",key,e);
			}
		}
	}

	@Override
	public String encrypt(String data) {
		AssertUtil.service().notNull(sm2, "秘钥服务配置错误，secret.soft.sm2keys不能为空，配置方式为： {priKey}@{pubKey}");
		return sm2.encryptHex(data, KeyType.PublicKey);
	}

	@Override
	public String decrypt(String data) {
		AssertUtil.service().notNull(sm2, "秘钥服务配置错误，secret.soft.sm2keys不能为空，配置方式为： {priKey}@{pubKey}");
		return sm2.decryptStr(data, KeyType.PrivateKey);
	}

	@Override
	public String hash(String data) {
		AssertUtil.service().notNull(hmac, "秘钥服务配置错误，secret.soft.hmackey不能为空");
		return hmac.digestHex(data);
	}
	
	
//	public static void main(String[] args) throws Exception {
//
////		KeyPair pair = SecureUtil.generateKeyPair("SM2");
////		String privateKey = Base64.encode(pair.getPrivate().getEncoded());
////		String publicKey = Base64.encode(pair.getPublic().getEncoded());
//		String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgy/b+7xHanQfWlOR0InSSWWa9P8JtkwUOWRegrPCbIqGgCgYIKoEcz1UBgi2hRANCAAQo3Xe0yQ0NPhyFYWOBUjBGlwbK/tMt06XwZdiXCY4TJL5RmqP9CADvEud+2nypPBYYU7eqHGk676noK4MhmPJI";
//		String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEKN13tMkNDT4chWFjgVIwRpcGyv7TLdOl8GXYlwmOEyS+UZqj/QgA7xLnftp8qTwWGFO3qhxpOu+p6CuDIZjySA==";
////		System.out.println("privateKey:"+privateKey);
////		System.out.println("publicKey:"+publicKey);
//
//		SoftSMI smi=new SoftSMI();
//		smi.sm2=SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
//		smi.hmac=SmUtil.hmacSm3("DmiaqxvLEIL8xIYFvYa2".getBytes("UTF-8"));
//
//		String text="这是一个绝密消息，不能外传！";
//		System.out.println("明文："+text);
//		String hash =  smi.hash(text);
//		System.out.println("hash："+hash);
//		String cipher = smi.encrypt(text);
//		System.out.println("加密后密文："+cipher);
//		String plaintext = smi.decrypt(cipher);
//		System.out.println("解密后明文："+plaintext);
//
//	}
	

}
