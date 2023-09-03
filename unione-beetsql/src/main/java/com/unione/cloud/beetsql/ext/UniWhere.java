package com.unione.cloud.beetsql.ext;

import java.io.IOException;
import java.util.List;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.unione.cloud.beetsql.Where;
import com.unione.cloud.core.exception.AssertUtil;

public class UniWhere implements Function {

	@Override
	@SuppressWarnings("unchecked")
	public Object call(Object[] paras, Context ctx) {
		Where where=(Where)paras[0];
		AssertUtil.database().notNull(where, "SQL函数UniWhere参数不能为空");
		
		List<Object> params=where.values();
		if(params!=null && !params.isEmpty()) {
			List<Object> list = (List<Object>) ctx.getGlobal("_paras");
			list.addAll(params);
		}
		
		try {
			ctx.byteWriter.writeString(where.sql());
		} catch (IOException e) {
			
		}
		
		return null;
	}

}
