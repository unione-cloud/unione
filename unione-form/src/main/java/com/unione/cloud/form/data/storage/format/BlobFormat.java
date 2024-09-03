package com.unione.cloud.form.data.storage.format;

import java.io.InputStream;
import java.sql.Blob;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BlobFormat extends Format {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8492338497021364006L;

	@Override
	public StringBuffer format(Object value, StringBuffer toAppendTo, FieldPosition pos) {
		if(value!=null) {
			try {
				Blob blob = (Blob) value;
				InputStream in = blob.getBinaryStream();
				byte[] data = IoUtil.readBytes(in);
				return new StringBuffer(Base64.encode(data));
			} catch (Exception e) {
				log.error("Blob转换成Strign失败",e);
			}
		}
		return null;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if(!StringUtils.isEmpty(source)) {
			try {
				return new SerialBlob(source.getBytes());
			} catch (Exception e) {
				log.error("String转Blob失败,source:{}",source,e);
			}
		}
		return null;
	}

}
