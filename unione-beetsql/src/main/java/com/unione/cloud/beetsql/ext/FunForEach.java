package com.unione.cloud.beetsql.ext;

import org.beetl.core.Context;
import org.beetl.core.Function;


/**
 * 	循环函数forEach
 *  功能： 循环集合，将每个元素应用循环表达式，返回拼接后的SQL字符串，输出SQL使用 () 包裹。
 *  参数1：集合名称
 *  参数2：循环表达式
 *  参数3：拼接符，如:or, and，默认 ','
 *  例如：
 *  forEach(lvsns,lvsn like [?%],or) ->
 *  (lvsn like [?%] or lvsn like [?%] or lvsn like [?%])
 */
public class FunForEach implements Function {

    @Override
    public Object call(Object[] args, Context ctx) {
        return null;
    }

}
