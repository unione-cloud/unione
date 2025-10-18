package com.unione.cloud.beetsql.ext;

import java.util.Map;

import org.beetl.core.Context;
import org.beetl.core.Function;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;


public class FunNotNull implements Function {

	@Override
	@SuppressWarnings("unchecked")
	public Boolean call(Object[] paras, Context ctx) {
		Object object=paras[0];
		if (object!=null && object instanceof Map) {
			Map map=(Map) object;
			if(MapUtil.isEmpty(map)){
				return false;
			}
			return map.values().stream().filter(v->ObjectUtil.isNotEmpty(v)).count()>0;
		}
		return ObjectUtil.isNotEmpty(object);
	}

}
