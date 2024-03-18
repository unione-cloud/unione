package com.unione.cloud.beetsql.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.annotation.UniDataSensitive.DataSensitive;
import com.unione.cloud.beetsql.builder.SqlSensitive;
import com.unione.cloud.core.security.secret.SecretService;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;

/**
 * 	数据脱敏工具类
 * @author Jeking 杨
 */
@Service
public class SensitiveUtil {
	
	private static SecretService secretService;
	@Autowired
	public void setSecretService(SecretService secretService) {
		SensitiveUtil.secretService=secretService;
	}

	
	/**
	 * 	数据脱敏处理
	 * @param value
	 * @param sensitive
	 * @return
	 */
	public static String process(String value,SqlSensitive sensitive) {
		if(StringUtils.isEmpty(value)) {
			return null;
		}
		
		// 数据加密
		if(DataSensitive.ENCRYPT.name().equals(sensitive.getType())) {
			return secretService.decrypt(value);
		}
		
		// 正则表达式REGEX、TEL、IDCARD脱敏
		if(DataSensitive.REGEX.name().equals(sensitive.getType()) || 
				DataSensitive.TEL.name().equals(sensitive.getType()) ||
				DataSensitive.IDCARD.name().equals(sensitive.getType())) {
			return value.replaceAll(sensitive.getExpress(), sensitive.getReplace());
		}
		
		// 姓名UNAME脱敏
		if(DataSensitive.UNAME.name().equals(sensitive.getType())) {
			return processUName(value);
		}

		// 车牌号CARLICENSE脱敏
		if(DataSensitive.CARLICENSE.name().equals(sensitive.getType())) {
			return DesensitizedUtil.carLicense(value);
		}

		// 银行号BANKCARD脱敏
		if(DataSensitive.BANKCARD.name().equals(sensitive.getType())) {
			return DesensitizedUtil.bankCard(value);
		}

		// 地址ADDRESS脱敏
		if(DataSensitive.ADDRESS.name().equals(sensitive.getType())) {
			return DesensitizedUtil.address(value, StringUtils.isEmpty(sensitive.getExpress())?10:Integer.parseInt(sensitive.getExpress()));
		}
		
		throw new DatabaseException("暂不支持的数据脱敏方式,sensitive:"+sensitive.getType());
	}
	
	
	
	public static String processUName(String name) {
		if(name.length()>1) {
			return StrUtil.hide(name, 1, name.length()-1);
		}
		if(name.length()==1) {
			return "*";
		}
		return StrUtil.EMPTY;
	}
	
	
	
}
