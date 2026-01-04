package com.unione.cloud.beetsql.ext;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.beetl.sql.core.engine.WhereTag;

import cn.hutool.core.util.ObjectUtil;

public class TagSqlWhere extends WhereTag {

	private Pattern actRegix=Pattern.compile("(\\s+AND\\s+AND\\s+)|(\\s+OR\\s+OR\\s+)|(\\s+AND\\s+OR\\s+)|(\\s+OR\\s+AND\\s+)",Pattern.CASE_INSENSITIVE); 

	@Override
	public void render() {
		try {
			initTrimArgs(args);
			String sb = buildTrimContent().toString();
			
			String sql = sb.toString().trim().toUpperCase();
			if(sql.endsWith("AND")) {
				sql = sql.substring(0, sql.lastIndexOf(" AND")).trim();
				sb = sb.substring(0, sb.toUpperCase().lastIndexOf(" AND "));
			}else if (sql.endsWith("OR")) {
				sql = sql.substring(0, sql.lastIndexOf(" OR")).trim();
				sb = sb.substring(0, sb.toUpperCase().lastIndexOf(" OR "));
			}

			String tmp=sql.replaceAll("\\s", "");
			if("WHERE".equalsIgnoreCase(tmp) ||"WHEREAND".equalsIgnoreCase(tmp) || "WHEREOR".equalsIgnoreCase(tmp)) {
				return;
			}
			if(tmp.startsWith("WHERE()AND")) {
				tmp=tmp.substring(10);
				if(ObjectUtil.isEmpty(tmp)){
					return;
				}
				this.ctx.byteWriter.writeString(String.format("WHERE %s ", process(sb.substring(sql.indexOf(" AND ")+5))));
				return;
			}

			this.ctx.byteWriter.writeString(process(sb));
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	private String process(String sql){
		Matcher actMatcher=actRegix.matcher(sql);
		while(actMatcher.find()) {
			actMatcher.group();
			sql = sql.replaceAll(actMatcher.group(), String.format(" %s ", actMatcher.group().trim().substring(0,3)));
		}
		return sql;
	}
	
}
