package com.unione.cloud.beetsql.ext;

import org.beetl.core.Context;
import org.beetl.core.Function;

import cn.hutool.core.util.ObjectUtil;

public class FunIsEqules implements Function {

	@Override
	public Boolean call(Object[] paras, Context ctx) {
		return ObjectUtil.equal(paras[0], paras[1]);
	}

}
