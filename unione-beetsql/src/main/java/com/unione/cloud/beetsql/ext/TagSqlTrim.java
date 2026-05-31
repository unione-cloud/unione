package com.unione.cloud.beetsql.ext;

import java.io.IOException;

import org.beetl.sql.clazz.kit.StringKit;
import org.beetl.sql.core.engine.TrimTag;

public class TagSqlTrim extends TrimTag {
	@Override
	public void render() {
		try {
			Object[] args = this.args;
			if (args != null && args.length != 0) {
				initTrimArgs(args);
				StringBuilder sb = buildTrimContent();
				this.ctx.byteWriter.writeString(sb.toString());
			} else {
				// 兼容老版本 trim.
				String sql = getBodyContent().getBody().trim();
				if(this.isSqlBlank(sql)) {
					return;
				}
				String tmp=sql.replaceAll("\\s", "");
				if("AND()".equalsIgnoreCase(tmp) || "OR()".equalsIgnoreCase(tmp) || "()".equalsIgnoreCase(tmp)) {
					return;
				}
				
				String prefix="",suffix="";
				if(sql.startsWith("(") && sql.endsWith(")")) {
					prefix="(";
					suffix=")";
					sql=sql.substring(1,sql.length()-1).trim();
				}
				
				
				// AND OR , 处理
				if (sql.toUpperCase().startsWith("AND ")) {
					String resql=sql.substring(4, sql.length());
					while(resql.toUpperCase().startsWith("AND ")){
						resql=resql.substring(4, resql.length());
					}
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,resql,suffix));
				}else if (sql.toUpperCase().endsWith(" AND")) {
					String resql=sql.substring(0, sql.length() - 4);
					while(resql.toUpperCase().endsWith(" AND")){
						resql=resql.substring(0, resql.length() - 4);
					}
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,resql,suffix));
				}else if (sql.toUpperCase().startsWith("OR ")) {
					String resql=sql.substring(3, sql.length());
					while(resql.toUpperCase().startsWith("OR ")){
						resql=resql.substring(3, resql.length());
					}
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,resql,suffix));
				}else if (sql.toUpperCase().endsWith(" OR")) {
					String resql=sql.substring(0, sql.length() - 3);
					while(resql.toUpperCase().endsWith(" OR")){
						resql=resql.substring(0, resql.length() - 3);
					}
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,resql,suffix));
				} else if (sql.startsWith(",")) {
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,sql.substring(1, sql.length()),suffix));
				}else if (sql.endsWith(",")) {
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,sql.substring(0, sql.length() - 1),suffix));
				} else {
					this.ctx.byteWriter.writeString(String.format("%s%s%s", prefix,sql,suffix));
				}
			}
		} catch (IOException ie) {
			throw new RuntimeException(ie);
		}

	}
	
	protected boolean isSqlBlank(String sql) {
		if (StringKit.isBlank(sql)) {
			return true;
		}
		String sql_=StringKit.trim(sql).replaceAll("\\s", "");
		return "()".equals(sql_) || sql_.isEmpty();
	}
}
