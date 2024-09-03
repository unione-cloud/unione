package com.unione.cloud.form.data.storage.format;

import java.io.Reader;
import java.sql.Clob;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClobFormat extends Format {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4754740627478094613L;

	@Override
	public StringBuffer format(Object value, StringBuffer toAppendTo, FieldPosition pos) {
		if(value!=null) {
			try {
				Clob clob = (Clob) value;
				Reader in = clob.getCharacterStream();
				return new StringBuffer(IoUtil.read(in));
			} catch (Exception e) {
				log.error("Clob转换成Strign失败",e);
			}
		}
		return null;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if(!StringUtils.isEmpty(source)) {
			try {
				return new SerialClob(source.toCharArray());
			} catch (Exception e) {
				log.error("String转Clob失败,source:{}",source,e);
			}
		}
		return null;
	}

}
