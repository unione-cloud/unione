package com.unione.cloud.beetsql.ext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.beetl.core.Context;
import org.beetl.core.Function;
import org.beetl.core.GeneralLoopStatus;
import org.beetl.core.ILoopStatus;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.core.ExecuteContext;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.engine.SQLParameter;

import com.unione.cloud.core.util.BeanUtils;



/**
 * 	循环函数forEach
 *  功能： 循环集合，将每个元素应用循环表达式，返回拼接后的SQL字符串，输出SQL使用 () 包裹。
 *  参数1：集合名称,从ctx.get('集合名称')获取集合数据
 *  参数2：循环表达式
 *  参数3：拼接符，如:or, and，默认 ','
 *  例如：
 *  forEach(lvsns,lvsn like [?%],or) ->
 *  (lvsn like [?%] or lvsn like [?%] or lvsn like [?%])
 */
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FunForEach implements Function {

    @Override
    public Object call(Object[] args, Context ctx) {
    	if(args.length<=2) {
    		throw new NullPointerException("forEach 至少2个参数，参数1:集合，数组，参数2:表达式");
    	}

    	Object temp = args[0];
		if (temp == null) {
			throw new NullPointerException("forEach 参数为null");
		}
		Object params=ctx.getGlobal("params");
		if(params==null) {
			throw new NullPointerException("SQL 参数为null");
		}

		ILoopStatus it = GeneralLoopStatus.getIteratorStatus(BeanUtils.getFieldValue(params, temp.toString()));
		if (it == null) {
			throw new NullPointerException("forEach 参数1为必须为集合，数组，Iterator");
		}
		
		String express=args[1].toString();
		String separator=args.length>2?args[2].toString():"OR";

		List<SQLParameter> dbParas = (List<SQLParameter>) ctx.getGlobal("_paras");
		ExecuteContext executeContext = (ExecuteContext)ctx.getGlobal("_executeContext");
		DBStyle dbStyle = executeContext.sqlManager.getDbStyle();
		try {
			ctx.byteWriter.writeString(forEach(it,express,separator, dbParas,dbStyle));
		} catch (Exception e) {
			// IO错误这里不抛出
		} 

        return null;
    }
    
    
	private Pattern fieldRegix=Pattern.compile("[\\w]+");
	private Pattern funRegix=Pattern.compile("[\\s]+(AND|OR)[\\s]+",Pattern.CASE_INSENSITIVE);
	private Pattern varRegix=Pattern.compile("\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?\\.?\\w*[\\s]*\\]");
	private Pattern conditionRegix=Pattern.compile("[\\s]*(AND|OR)?[\\s]*[\\w]+[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN))[\\s]*(\\?|\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?\\.?\\w*[\\s]*\\])",Pattern.CASE_INSENSITIVE);
	private Pattern humpFieldRegix=Pattern.compile("[\\s]*([a-z][A-Za-z0-9]+)[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN)|IS|like|(not like)|in|(not in)|is)[\\s]+");
	
    
    private String forEach(ILoopStatus it,String express,String separator, List dbParas,DBStyle dbStyle ) {
    	
    	// where条件处理
		Matcher matcher=conditionRegix.matcher(express);
		List<String[]> conditions=new ArrayList<String[]>();
		while(matcher.find()) {
			String condition=matcher.group();
			String[] cons= whereCondition(condition);
			conditions.add(cons);
		}

		StringBuilder buf = new StringBuilder();
		boolean isFirst=true;
		
		while (it.hasNext()) {
			Object o = it.next();
			
			StringBuilder tmp=new StringBuilder();
			conditions.stream().forEach(con->{
				String fun=con[0];
				String field=con[1];
				String condition=con[2];
				Object value = getValue(o, field.replaceAll("%", ""));
				if(value!=null) {
					if(!tmp.isEmpty()) {
						tmp.append(fun).append(" ");
					}
					tmp.append(condition).append(" ");
					if(field.startsWith("%") && field.endsWith("%")){
						dbParas.add(new SQLParameter(null, String.format("%%s%%", value)));
					}else if(field.startsWith("%")){
						dbParas.add(new SQLParameter(null, String.format("%%%s", value)));
					}else if(field.endsWith("%")){
						dbParas.add(new SQLParameter(null, String.format("%s%%", value)));
					}else{
						dbParas.add(new SQLParameter(null, value));
					}
				}
			});
			
			if(!tmp.isEmpty()) {
				if(!isFirst) {
					buf.append(separator).append(" ");
				}
				if(express.startsWith("(")) {
					buf.append("(").append(tmp).append(")");
				}else {
					buf.append(tmp);
				}
				isFirst=false;
			}
		}
		
		return buf.toString();
	}

	private Object getValue(Object o, String attrName) {
		if(o==null){
			return null;
		}
		if(!o.getClass().isPrimitive()){
			return o;
		}
		Object value=BeanUtils.getFieldValue(o, attrName);
		return value;
	}
    
    /**
	 * 处理Where条件
	 * @param condition
	 * @return
	 */
	private String[] whereCondition(String condition) {
		
		Matcher funMatcher=funRegix.matcher(condition);
		String funName="";
		if(funMatcher.find()) {
			funName=funMatcher.group();
			condition=condition.replace(funName, "");
		}
		
		Matcher fieldMatcher=fieldRegix.matcher(condition);
		fieldMatcher.find();
		String fieldName=fieldMatcher.group().trim();
		
		// 字段名称变成大写
		if(!fieldName.matches("^[A-Z\\_]*$")) {
			condition=condition.replaceFirst(fieldName, fieldName.replaceAll("[A-Z]", "_$0").toUpperCase());
		}
		
		Matcher varMatcher=varRegix.matcher(condition);
		if(varMatcher.find()) {
			// [复杂变量处理]，eg：[%name%]
			String group=varMatcher.group();
			fieldName=group.replace("?", fieldName).replaceAll("[^a-zA-Z0-9%]", "").trim();
			condition=condition.replace(group, "?");
		}
		
		// 静态参数字段名称处理
		Matcher humpMatcher=humpFieldRegix.matcher(condition);
		while(humpMatcher.find()) {
			String tmp=humpMatcher.group();
			condition=condition.replace(tmp, tmp.replaceAll("[A-Z]", "_$0").toUpperCase());
		}
		
		String result[]=new String[3];
		result[0]=funName;
		result[1]=fieldName;
		result[2]=condition;
		
		return result;
	}
    

}
