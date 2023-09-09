package com.unione.cloud.beetsql.ext;

import java.io.IOException;

import org.beetl.sql.core.engine.WhereTag;

public class SqlWhere extends WhereTag {

	@Override
	public void render() {
		try {
			initTrimArgs(args);
			StringBuilder sb = buildTrimContent();
			if("WHEREAND".equalsIgnoreCase(sb.toString().replaceAll("\\s", ""))) {
				return;
			}
			this.ctx.byteWriter.writeString(sb.toString());
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
}
