package com.unione.cloud.beetsql.ext;

import org.beetl.core.Context;
import org.beetl.core.Function;

import cn.hutool.core.util.ObjectUtil;


public class FunVarNotNull implements Function {

	@Override
	public Boolean call(Object[] paras, Context ctx) {
		Object object=paras[0];
		return ObjectUtil.isNotEmpty(object);
	}

}
