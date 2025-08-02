package com.unione.cloud.beetsql.ext;

import org.beetl.core.Context;
import org.beetl.core.Function;

import cn.hutool.core.util.ObjectUtil;

public class FunContains implements Function {

    @Override
    public Boolean call(Object[] paras, Context ctx) {
        if(paras==null || paras.length!=2){
            return false;
        }
        return ObjectUtil.contains(paras[0], paras[1]);
    }

}
