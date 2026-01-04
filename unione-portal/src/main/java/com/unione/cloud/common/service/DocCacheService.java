package com.unione.cloud.common.service;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.common.model.DocFile;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.Data;

@Service
@RefreshScope
public class DocCacheService {
	
	
	private Cache<String, DocData> cache;
	/**
	 * 文档缓存时间，单位秒，默认：60
	 * @param time
	 */
	@Value("${doc.cache.timelife:60}")
	public void setCacheTime(int time) {
		cache=CacheUtil.newTimedCache(1000*time);
	}
	
	
	@Data
	public static class DocData implements Serializable{
		private static final long serialVersionUID = 1849131243783793324L;
		
		private Long fileId;
		private String title;
		private String type;
		private String path;
		private String error;	//	如果不为空，则直接返回该错误消息
		private String local;	//  缓存文件存储绝对地址
		
		public static DocData build(DocFile doc,File file) {
			DocData data=new DocData();
			data.setTitle(doc.getTitle());
			data.setFileId(doc.getId());
			data.setPath(doc.getPath());
			data.setType(doc.getType());
			data.setLocal(file.getAbsolutePath());
			return data;
		}
		
		public static DocData error(Long fileId,String error) {
			DocData data=new DocData();
			data.setFileId(fileId);
			data.setError(error);
			return data;
		}
		
		public File getFile() {
			if(!StringUtils.isEmpty(local)) {
				File file=new File(local);
				if(!file.exists() || file.length()<10) {
//					log.warn("缓存文件已被清理：cache path:{}",local);
					return null;
				}
				return file;
			}
			return null;
		}
	}
	
	
	/**
	 * 	从缓存中获取doc file对象
	 * @param fileId
	 * @param isPublic 是否为公共文件
	 * @return
	 */
	public DocData getDocData(Long fileId,boolean isPublic) {
		return cache.get(String.format("%s:%s",fileId,isPublic));
	}
	
	
	/**
	 * 	添加doc file 到缓存
	 * @param fileId
	 * @param isPublic 是否为公共文件
	 * @param data
	 */
	public void setDocData(Long fileId,boolean isPublic,DocData data) {
		cache.put(String.format("%s:%s",fileId,isPublic), data);
	}
	
	/**
	 * 	从缓存中删除doc file
	 * @param fileId
	 * @param isPublic 是否为公共文件
	 */
	public void delDocData(Long fileId,boolean isPublic) {
		cache.remove(String.format("%s:%s",fileId,isPublic));
	}

}
