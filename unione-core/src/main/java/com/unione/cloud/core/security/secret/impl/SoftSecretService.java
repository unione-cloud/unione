package com.unione.cloud.core.security.secret.impl;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.secret.SecretInterface;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.HMac;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认软加解密服务，采用hutool工具国密算法实现
 * 
 * @author Jeking 杨
 */
@Slf4j
@Service
@RefreshScope
public class SoftSecretService implements SecretInterface {

	private String sm2PubKey;
	private SM2 sm2;
	private HMac hmac;

	@Value("${secret.soft.sm2keys:308193020100301306072a8648ce3d020106082a811ccf5501822d0479307702010104204389c4329a001df855d14ec204d25bf12d4d631bf61bb02d0586a653e199e0daa00a06082a811ccf5501822da14403420004a684d7140aebfc83515a2312dac7ab836f3adc484627971b6936ad7d5575048caecf9c7eb07ed4dcee9d04cb3a7b1c514477b646b1ebe8245de215733baa5133@3059301306072a8648ce3d020106082a811ccf5501822d03420004a684d7140aebfc83515a2312dac7ab836f3adc484627971b6936ad7d5575048caecf9c7eb07ed4dcee9d04cb3a7b1c514477b646b1ebe8245de215733baa5133}")
	public void setSm2Keys(String keys) {
		sm2 = null;
		if (!StringUtils.isEmpty(keys)) {
			String tmps[] = keys.trim().split("@");
			AssertUtil.service().isTrue(tmps.length == 2, "秘钥服务配置错误，secret.soft.sm2keys配置方式为： {priKey}@{pubKey}");
			sm2 = SmUtil.sm2(HexUtil.decodeHex(tmps[0]), HexUtil.decodeHex(tmps[1]));
			sm2.setMode(SM2Engine.Mode.C1C3C2);
			sm2PubKey = tmps[1];
		}
	}

	/**
	 * 获取公钥
	 * 
	 * @return
	 */
	@Override
	public String getPubKey() {
		if (!StringUtils.isEmpty(sm2PubKey)) {
			return sm2PubKey;
		}
		return null;
	}

	@Value("${secret.soft.hmackey:DmiaqxvLEIL8xIYFvYa2}")
	public void setHmacKeys(String key) {
		hmac = null;
		if (!StringUtils.isEmpty(key)) {
			try {
				hmac = SmUtil.hmacSm3(key.getBytes("UTF-8"));
			} catch (Exception e) {
				log.error("hmac初始化失败,请检查配置项：secret.soft.hmackey,value:{}", key, e);
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
//		// 创建sm2 对象
//		SM2 sm = SmUtil.sm2();
//		// sm2的加解密时有两种方式即 C1C2C3、 C1C3C2，
//		sm.setMode(SM2Engine.Mode.C1C3C2);
//		// 生成私钥
//		String privateKey = HexUtil.encodeHexStr(sm.getPrivateKey().getEncoded());
//		log.info("私钥: {}", privateKey);
//		// 生成公钥
//		String publicKey = HexUtil.encodeHexStr(sm.getPublicKey().getEncoded());
//		log.info("公钥: {}", publicKey);
//		// 生成私钥 D，以D值做为js端的解密私钥
//		String privateKeyD = HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(sm.getPrivateKey()));
//		log.info("私钥D: {}", privateKeyD);
//
//		// 生成公钥 Q，以Q值做为js端的加密公钥
//		String publicKeyQ = HexUtil.encodeHexStr(((BCECPublicKey) sm.getPublicKey()).getQ().getEncoded(false));
//		log.info("公钥Q: {}", publicKeyQ);
//	}

}
