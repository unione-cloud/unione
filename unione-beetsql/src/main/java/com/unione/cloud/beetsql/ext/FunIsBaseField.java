package com.unione.cloud.beetsql.ext;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.unione.cloud.core.model.BaseField;

import cn.hutool.core.util.ObjectUtil;


public class FunIsBaseField implements Function {

	@Override
	public Boolean call(Object[] paras, Context ctx) {
		Object field=paras[0];
		if(ObjectUtil.isEmpty(field)) {
			return false;
		}
		return BaseField.isBaseColume(field.toString())!=null;
	}

}
