package com.unione.cloud.form.data.util;

import org.apache.commons.lang3.StringUtils;

import com.unione.cloud.core.exception.AssertUtil;

public class DataUtil {

	
	/**
	 * 	转换成驼峰命名
	 * @param name
	 * @return
	 */
	public static String toHump(String name) {
		AssertUtil.service().notNull(name, "参数name不能为空");
		String tt[]=name.toLowerCase().split("_");
		StringBuffer buf=new StringBuffer();
		buf.append(tt[0]);
		for(int i=1;i<tt.length;i++) {
			String t=tt[i];
			if(!StringUtils.isEmpty(t)) {
				buf.append((t.charAt(0)+"").toUpperCase());
				if(t.length()>1) {
					buf.append(t.substring(1));
				}
			}
		}
		return buf.toString();
	}
	
	
}
