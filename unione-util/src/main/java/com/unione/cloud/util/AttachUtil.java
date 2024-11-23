package com.unione.cloud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 附件工具类
 * @author Jeking Yang
 */
@Slf4j
@Service
public class AttachUtil {

	/**
	 * 文件服务类型:file、minio，默认使用file
	 */
	private static String FILE_SERVER_TYPE="file";
	private static String FILE_SERVER_ROOT="";
	
	@Value("${attach.file.server.type:file}")
	public void setFileStoreType(String type) {
		FILE_SERVER_TYPE=type;
	}

	@Value("${attach.file.server.root:}")
	public void setFileStoreRoot(String root) {
		FILE_SERVER_ROOT=root;
	}
	
	/**
	 * 保存附件
	 * @param file
	 * @param dir
	 * @return
	 */
	public static Attach save(File file,String dir){
		return save(file,dir, 0);
	}


	/**
	 * 保存附件
	 * @param file
	 * @param dir
	 * @param isPublic
	 * @return
	 */
	public static Attach save(File file,String dir,int isPublic){
		log.debug("进入->保存附件方法，isPublic:{},dir:{}",isPublic,dir);
		Attach attach=new Attach();
		attach.setIsPublic(isPublic);

		StringBuffer remoteDir=new StringBuffer(FILE_SERVER_ROOT);
		remoteDir.append(StringUtils.trimToEmpty(dir));
		remoteDir.append("/").append(createDateDir(DateUtil.date()));
		
		InputStream in=null;
		try {
			long size=file.length();
			String title=file.getName();
			String type = title.substring(title.lastIndexOf(".")+1, title.length());

			attach.setCreated(DateUtil.date());
			attach.setLastUpdated(DateUtil.date());
			attach.setTitle(title);
			attach.setSize(size);
			attach.setTypes(type);

			StringBuffer remote=new StringBuffer();
			remote.append(remoteDir);
			remote.append(createFname(type));
			attach.setPath(remote.toString());

			boolean flag=false;
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.upload(file, attach.getRealPath());
			}else{
				flag=FileUtil.save(file, attach.getRealPath());
			}

			log.debug("附件  title:"+attach.getTitle()+"上传结果："+flag);
			if(flag){
				attach.setSuccess(true);
			}else{
				attach.setMessage("附件上传失败");
			}
		} catch (Exception e) {
			log.error("保存附件异常",e);
			attach.setMessage("附件上传失败");
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("退出->保存附件方法");
		return attach;
	}

	/**
	 * 保存附件
	 * @param name
	 * @param request
	 * @return
	 */
	public static Attach save(String name,MultipartHttpServletRequest request){
		return save(name, 0, request);
	}


	/**
	 * 保存附件
	 * @param name
	 * @param isPublic
	 * @param request
	 * @return
	 */
	public static Attach save(String name,Integer isPublic,MultipartHttpServletRequest request){
		Attach attach=new Attach();
		attach.setName(name);
		attach.setIsPublic(isPublic);
		return save(attach,null, request);
	}

	/**
	 * 保存附件
	 * @param name
	 * @param dir
	 * @param request
	 * @return
	 */
	public static Attach save(String name,String dir,MultipartHttpServletRequest request){
		return save(name, dir, 0, request);
	}


	/**
	 * 保存附件
	 * @param name
	 * @param dir
	 * @param isPublic
	 * @param request
	 * @return
	 */
	public static Attach save(String name,String dir,Integer isPublic,MultipartHttpServletRequest request){
		Attach attach=new Attach();
		attach.setName(name);
		attach.setIsPublic(isPublic);
		return save(attach,dir, request);
	}


	/**
	 * 保存附件
	 * @param attach
	 * @param dir
	 * @param request
	 * @return
	 */
	private static Attach save(Attach attach,String dir,MultipartHttpServletRequest request){
		log.debug("进入->保存附件方法，ownerid:"+attach.getOwnerId()+",dir:"+dir);
		Attach result=new Attach();
		StringBuffer remoteDir=new StringBuffer(FILE_SERVER_ROOT);
		remoteDir.append(StringUtils.trimToEmpty(dir));
		remoteDir.append("/").append(createDateDir(DateUtil.date()));

		List<MultipartFile> files=request.getFiles(attach.getName());

		try {
			for(MultipartFile file:files){
				if(file.getSize()<=0){
					continue;
				}

				long attachSize=file.getSize();
				String attachTitle=file.getOriginalFilename();
				String attachType = attachTitle.substring(attachTitle.lastIndexOf(".")+1, attachTitle.length());

				attach.setCreated(DateUtil.date());
				attach.setTitle(attachTitle);
				attach.setSize(attachSize);
				attach.setTypes(attachType);

				StringBuffer remote=new StringBuffer();
				remote.append(remoteDir);
				remote.append(createFname(attachType));
				attach.setPath(remote.toString());


				boolean flag=false;
				if("minio".equals(FILE_SERVER_TYPE)){
					flag=MinioUtil.upload(file.getInputStream(), attach.getRealPath());
				}else{
					flag=FileUtil.save(file.getInputStream(), attach.getRealPath());
				}

				log.debug("附件  title:"+attach.getTitle()+"上传结果："+flag);
				if(flag){
					Attach att=new Attach();
					BeanUtils.copyProperties(attach,att);
					result.getRows().add(att);
				}
			}
		} catch (Exception e) {
			log.error("保存附件异常",e);
		} 
		
		if(result.getRows().size()>0){
			result.setSuccess(true);
		}else{
			result.setMessage("附件上传失败");
		}

		log.debug("退出->保存附件方法");
		return result;
	}

	
	/**
	 * 保存附件
	 * @param file
	 * @param dir
	 * @return
	 */
	public static Attach save(MultipartFile file,String dir){
		return save(file, dir,0);
	}


	/**
	 * 保存附件
	 * @param file
	 * @param dir
	 * @param isPublic
	 * @return
	 */
	public static Attach save(MultipartFile file,String dir,int isPublic){
		log.debug("进入->保存方法,dir:"+dir);
		Attach result=new Attach();
		result.setIsPublic(isPublic);

		StringBuffer remoteDir=new StringBuffer(FILE_SERVER_ROOT);
		remoteDir.append(StringUtils.trimToEmpty(dir));
		remoteDir.append("/").append(createDateDir(DateUtil.date()));
		try {
			String attachTitle=file.getOriginalFilename();
			String attachType = attachTitle.substring(attachTitle.lastIndexOf(".")+1, attachTitle.length());

			StringBuffer remote=new StringBuffer();
			remote.append(remoteDir);
			remote.append(createFname(attachType));
			result.setPath(remote.toString());
			result.setTitle(attachTitle);
			result.setTypes(attachType);
			result.setSize(file.getSize());
			result.setCreated(DateUtil.date());
			result.setLastUpdated(DateUtil.date());
			

			boolean flag=false;
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.upload(file.getInputStream(), result.getRealPath());
			}else{
				flag=FileUtil.save(file.getInputStream(), result.getRealPath());
			}

			log.debug("附件  title:"+attachTitle+"上传结果："+flag);
			result.setSuccess(flag);
			result.setMessage(flag?"保持成功":"保持失败");
		} catch (Exception e) {
			log.error("保存附件异常",e);
		} 

		log.debug("退出->保存方法");
		return result;
	}

	/**
	 * 	保存附件到指定path
	 * @param file
	 * @param remote
	 * @return
	 */
	public static boolean save2path(MultipartFile file,String remote){
		log.debug("进入->保存附件到指定path方法,remote:{}",remote);
		boolean flag=false;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.upload(file.getInputStream(), remote);
			}else{
				flag=FileUtil.save(file.getInputStream(), remote);
			}
			log.debug("附件  title:"+file.getOriginalFilename()+"上传结果："+flag);
		} catch (Exception e) {
			log.error("保存附件异常",e);
		}

		log.debug("进入->保存附件到指定path方法,remote:{},result:{}",remote,flag);
		return flag;
	}
	
	/**
	 * 	保存附件到指定path
	 * @param file
	 * @param remote
	 * @return
	 */
	public static boolean save2path(File file,String remote){
		log.debug("进入->保存附件到指定path方法,remote:{}",remote);
		InputStream in=null;
		boolean flag=false;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.upload(file, remote);
			}else{
				flag=FileUtil.save(file, remote);
			}

			log.debug("附件  title:"+file.getName()+"上传结果："+flag);
		} catch (Exception e) {
			log.error("保存附件异常",e);
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("进入->保存附件到指定path方法,remote:{},result:{}",remote,flag);
		return flag;
	}
	
	/**
	 * 	保存附件到指定path
	 * @param in
	 * @param remote
	 * @return
	 */
	public static boolean save2path(InputStream in,String remote){
		log.debug("进入->保存附件到指定path方法,remote:{}",remote);
		boolean flag=false;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.upload(in, remote);
			}else{
				flag=FileUtil.save(in, remote);
			}
		} catch (Exception e) {
			log.error("保存附件异常",e);
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("进入->保存附件到指定path方法,remote:{},result:{}",remote,flag);
		return flag;
	}

	/**
	 * 下载附件
	 * @param path
	 * @param title
	 * @param request
	 * @param response
	 */
	public static void download(String path,String title,HttpServletRequest request,HttpServletResponse response){
		log.debug("进入->下载附件方法，path:{},title:{}",path,title);

		OutputStream out = null;
		InputStream in=null;
		
		try {
			// 文件名称转码，避免下载后文件名称乱码问题
			String requestHeaderUserAgent=request.getHeader("user-agent");
			try {
				if (requestHeaderUserAgent.contains("MSIE")) {				// IE浏览器 --- URL编码
					title = URLEncoder.encode(title, "utf-8");
				} else if (requestHeaderUserAgent.contains("Firefox")) {	// 火狐浏览器 --- Base64编码
					title = "=?UTF-8?B?" + new String(Base64.encode(title.getBytes("UTF-8"))).replaceAll("[\\t\\n\\r]", "") + "?=";
				} else {
					title = URLEncoder.encode(title, "utf-8");
				}
			} catch (Exception e1) {}

			// 设置返回信息格式
			response.setHeader("Content-disposition","attachment;filename="+title);
			response.setContentType("application/x-download"); 

			if("minio".equals(FILE_SERVER_TYPE)){
				in=MinioUtil.download(path);
			}else{
				in=FileUtil.open(path);
			}

			if(in!=null){
				String length=String.valueOf(in.available()); 
				response.setHeader("Content_length",length); 
				out = response.getOutputStream();
				IOUtils.copy(in, out);
				out.flush();
			}else{
				sendScriptMessage("附件下载失败", response);
			}

		} catch (Exception e) {
			log.error("下载附件异常",e);
			sendScriptMessage("下载附件异常", response);
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("退出->下载附件方法，path:{},title:{}",path,title);
	}



	/**
	 * 	下载附件
	 * @param file
	 * @param title
	 * @param request
	 * @param response
	 */
	public static void download(File file,String title,HttpServletRequest request,HttpServletResponse response){
		log.debug("进入->下载附件方法，path:"+file.getAbsolutePath());

		OutputStream out = null;
		InputStream in=null;
		try {
			AssertUtil.service().isTrue(file.exists(), "文件不存在");

			// 文件名称转码，避免下载后文件名称乱码问题
			String requestHeaderUserAgent=request.getHeader("user-agent");
			try {
				if (requestHeaderUserAgent.contains("MSIE")) {				// IE浏览器 --- URL编码
					title = URLEncoder.encode(title, "utf-8");
				} else if (requestHeaderUserAgent.contains("Firefox")) {	// 火狐浏览器 --- Base64编码
					title = "=?UTF-8?B?" + new String(Base64.encode(title.getBytes("UTF-8"))).replaceAll("[\\t\\n\\r]", "") + "?=";
				} else {
					title = URLEncoder.encode(title, "utf-8");
				}
			} catch (Exception e1) {}
			// 设置返回信息格式
			response.setHeader("Content-disposition","attachment;filename="+title);
			response.setContentType("application/x-download"); 

			in=new FileInputStream(file);
			response.setHeader("Content_length",String.valueOf(in.available())); 
			out = response.getOutputStream();
			IOUtils.copy(in, out);
			out.flush();
		} catch (ServiceException e) {
			log.error(e.getMessage(),e);
			sendScriptMessage(e.getMessage(), response);
		}  catch (Exception e) {
			log.error("下载附件异常",e);
			sendScriptMessage("下载附件异常", response);
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("进入->下载附件方法，path:"+file.getAbsolutePath());
	}

	/**
	 * 下载附件附件
	 * @param path
	 * @return
	 */
	public static File download(String path){
		log.debug("进入->下载附件附件方法，path:{}",path);

		OutputStream out = null;
		InputStream in=null;
		File file=null;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				file=MinioUtil.download2file(path);
			}else{
				in=FileUtil.open(path);
			}
			String type = path.substring(path.lastIndexOf("."), path.length());
			
			if(in!=null){
				file=File.createTempFile("download", type);
				out=new FileOutputStream(file);
				IOUtils.copy(in, out);
				out.flush();
			}

		} catch (Exception e) {
			log.error("下载附件附件异常",e);
		} finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

		log.debug("退出->下载附件附件方法，path:{},path:{}",path,(file!=null?file.getAbsoluteFile():null));
		return file;
	}

	/**
	 * 预览附件
	 * @param path
	 * @param response
	 */
	public static void preview(String path,HttpServletResponse response){
		log.debug("进入->根据附件id预览方法，path:{}",path);

		OutputStream out = null;
		InputStream in=null;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				in=MinioUtil.download(path);
			} else {
				in = FileUtil.open(path);
			}

			if(in!=null){
				out = response.getOutputStream();
				IOUtils.copy(in, out);
				out.flush();
			}else{
				sendHtmlMessage("附件下载失败", response);
			}

		} catch (Exception e) {
			log.error("下载附件异常",e);
			sendHtmlMessage("下载附件异常", response);
		} finally{
			IOUtils.closeQuietly(in);
		}

		log.debug("退出->根据附件id预览方法，path:{}",path);
	}

	/**
	 * 预览附件
	 * @param file
	 * @param response
	 */
	public static void preview(File file,HttpServletResponse response){
		log.debug("进入->附件预览方法，path:"+file.getAbsolutePath());

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			FileUtils.copyFile(file, out);
		} catch (Exception e) {
			log.error("预览附件异常",e);
			sendHtmlMessage("预览附件异常", response);
		} 

		log.debug("退出->附件预览方法，path:"+file.getAbsolutePath());
	}


	/**
	 * 删除附件
	 * @param path
	 * @return
	 */
	public static boolean delete(String path){
		log.debug("进入->删除附件方法,path:{}",path);
		boolean flag=true;
		try {
			if("minio".equals(FILE_SERVER_TYPE)){
				flag=MinioUtil.remove(path);
			}else{
				flag=FileUtil.delete(path);
			}
		} catch (Exception e) {
			log.error("删除附件异常",e);
		} 
		log.debug("退出->删除附件方法,path:{},result:{}",path,flag);
		return flag;
	}

	
	/**
	 * 生成日期目录
	 * @param date
	 * @return
	 */
    public static synchronized String createDateDir(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/");
        return format.format(date!=null?date:DateUtil.date());
    }
    
    /**
     * 创建一个文件名称
     * @author Jeking Yang
     * @return uuid+.+type
     */
    public static synchronized String createFname(String type) {
        StringBuffer filename = new StringBuffer();
        filename.append(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase()).append(".").append(type);
        return filename.toString();
    }


	/**
	 * 向客户端输出alert提示信息
	 * @param message
	 * @param response
	 */
	public static void sendScriptMessage(String message, HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		String str = "<script type='text/javascript'>alert('" + message + "');</script>";
		try {
			response.setCharacterEncoding("UTF-8");
			response.setHeader("pragma", "no-cache");
			response.setHeader("cache-control", "no-cache");

			PrintWriter pw = response.getWriter();
			pw.write(str);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			log.error("输出页面脚本异常",e);
		}
	}


	/**
	 * 向客户端输出 html 提示信息
	 * @param message
	 * @param response
	 */
	public static void sendHtmlMessage(String message, HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		try {
			response.setCharacterEncoding("UTF-8");
			response.setHeader("pragma", "no-cache");
			response.setHeader("cache-control", "no-cache");

			PrintWriter pw = response.getWriter();
			pw.write(message);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			log.error("输出页面脚本异常",e);
		}
	}

	
	@Data
	public static class Attach{
		private Long sid;
		private Long appId;
		private Long tenantId;
		private Long ownerId;
		private String title;
		private String name;
		private String types;
		private Long size;
		private String path;
		private String store;
		private Integer isPublic;
		private Integer ordered;
		private Date created;
		private Long createdBy;
		private Date lastUpdated;
		private Long lastUpdatedBy;
		
		public String getRealPath() {
			return (isPublic!=null&&isPublic==1?"/public":"")+path;
		}
		private boolean success;
		private String message;
		private List<Attach> rows=new ArrayList<>();
	}
	
	public interface AttachStore{
		public AttachStore open();
		public void close();
		public boolean save(File file,String path);
		public boolean save(InputStream fileIn,String path);
		public boolean delete(String path);
		public InputStream read(String path);
		public File getFile(String path);
	}

}
