package com.unione.cloud.beetsql.ext;

import java.io.IOException;

import org.beetl.sql.core.engine.WhereTag;

public class TagSqlWhere extends WhereTag {

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

			this.ctx.byteWriter.writeString(sb);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
}
