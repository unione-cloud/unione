package com.unione.cloud.beetsql.ext;

import java.io.IOException;
import java.util.List;

import org.beetl.core.Context;
import org.beetl.core.Function;
import org.beetl.sql.core.engine.SQLParameter;

import com.unione.cloud.beetsql.SqlBuilder;

import cn.hutool.core.util.ObjectUtil;


public class UniWhere implements Function {

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object call(Object[] paras, Context ctx) {
		Integer index=(Integer)paras[0];
		SqlBuilder builder=(SqlBuilder)ctx.getGlobal("_root");
		
		List<Object> list = (List<Object>) ctx.getGlobal("_paras");
		SQLParameter param=(SQLParameter)builder.getConditions().get(index);
		if(!ObjectUtil.isEmpty(param.value)) {
			list.add(param);
		}
		try {
			ctx.byteWriter.writeString(ObjectUtil.isEmpty(param.value)?"1=1":param.expression);
		} catch (IOException e) {}
		
		return null;
	}

}
