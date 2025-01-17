package com.unione.cloud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.unione.cloud.core.exception.ServiceException;

import cn.hutool.core.codec.Base64;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件存储服务对象
 * @author Jeking Yang
 */
@Component
public class FileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	
	private static String homedir;
	public static String getHomedir() {
		return FileUtil.homedir;
	}
	@Value("${file.service.home.dir:./filestore}")
	public void setHomedir(String homedir) {
		FileUtil.homedir = homedir;
	}
	
	
	/**
	 * 保存文件
	 * @param file
	 * @param path
	 * @return
	 */
	public static boolean save(File file,String path){
		logger.debug("进入->保存文件方法,from:"+file.getAbsolutePath()+",to:"+path);
		try {
			FileUtil.save(file, new File(FileUtil.homedir+path));
		} catch (Exception e) {
			logger.error("保存文件异常",e);
			return false;
		}
		logger.debug("退出->保存文件方法,from:"+file.getAbsolutePath()+",to:"+path);
		return true;
	}
	
	public static boolean save(File file,File target){
		logger.debug("进入->保存文件方法,from:"+file.getAbsolutePath()+",to:"+target.getAbsolutePath());
		try {
			if(!target.getParentFile().exists()) {
				target.getParentFile().mkdirs();
			}
			FileUtils.copyFile(file, target);
		} catch (Exception e) {
			logger.error("保存文件异常",e);
			return false;
		}
		
		logger.debug("退出->保存文件方法,from:"+file.getAbsolutePath()+",to:"+target.getAbsolutePath());
		return true;
	}
	
	/**
	 * 保存文件
	 * @param fileIn
	 * @param path
	 * @return
	 */
	public static boolean save(InputStream fileIn,String path){
		logger.debug("进入->保存文件方法 to:"+path);
		
		try {
			File target=new File(FileUtil.homedir+path);
			if(!target.getParentFile().exists()) {
				target.getParentFile().mkdirs();
			}
			FileUtils.copyInputStreamToFile(fileIn, target);
		} catch (Exception e) {
			logger.error("保存文件异常",e);
			return false;
		}
		
		logger.debug("退出->保存文件方法to:"+path);
		return true;
	}
	
	/**
	 * 保存文件
	 * @param fileIn
	 * @param path
	 * @return
	 */
	public static boolean save(InputStream fileIn,File target){
		logger.debug("进入->保存文件方法 to:"+target.getAbsolutePath());
		try {
			if(!target.getParentFile().exists()) {
				target.getParentFile().mkdirs();
			}
			FileUtils.copyInputStreamToFile(fileIn, target);
		} catch (Exception e) {
			logger.error("保存文件异常",e);
			return false;
		}
		logger.debug("退出->保存文件方法to:"+target.getAbsolutePath());
		return true;
	}
	
	/**
	 * 删除文件
	 * @param path
	 * @return
	 */
	public static boolean delete(String path){
		logger.debug("进入->删除文件方法，path:"+path);
		try {
			FileUtils.deleteDirectory(new File(FileUtil.homedir+path));
		} catch (IOException e) {
			logger.error("删除文件异常",e);
			return false;
		}
		logger.debug("退出->删除文件方法，path:"+path);
		return true;
	}
	
	/**
	 * 替换文件
	 * @param file
	 * @param path
	 * @return
	 */
	public static boolean replace(File file,String path){
		logger.debug("进入->替换文件方法,源文件path:"+path+",新文件path:"+file.getAbsolutePath());
		
		boolean flag=FileUtil.delete(path);
		
		flag=flag?FileUtil.save(file, path):flag;
		
		logger.debug("退出->替换文件方法,源文件path:"+path+",新文件path:"+file.getAbsolutePath());
		return flag;
	}
	
	
	/**
	 * 打开文件
	 * @param path
	 * @return
	 */
	public static InputStream open(String path){
		logger.debug("进入->打开文件方法，path:"+path);
		InputStream in=null; 
		
		try {
			in=new FileInputStream(new File(FileUtil.homedir+path));
		} catch (Exception e) {
			logger.error("打开文件异常",e);
		}
		
		logger.debug("退出->打开文件方法，path:"+path);
		return in;
	}
	
	/**
	 * 下载文件
	 */
	public static void download(String path,HttpServletRequest request,HttpServletResponse response) {
		FileUtil.download(new File(FileUtil.homedir+path), request, response);
	}
	
	/**
	 * 下载文件
	 */
	public static void download(File file,HttpServletRequest request,HttpServletResponse response) {
		download(file, file.getName(), request, response);
	}
	
	/**
	 * 下载文件
	 */
	public static void download(File file,String title,HttpServletRequest request,HttpServletResponse response) {
		logger.debug("进入->下载文件方法，path:"+file.getAbsolutePath());
		
		OutputStream out = null;
	    InputStream in=null;
		try {
			if(!file.exists()){
				throw new ServiceException("文件不存在");
			}
			
			// 如果是目录，则压缩后下载
	        if(file.isDirectory()) {
	        	file=FileUtil.zip(file);
	        }
			
			// 文件名称转码，避免下载后文件名称乱码问题
	        String attachTitle=title;
	        String requestHeaderUserAgent=request.getHeader("user-agent");
	        try {
	            if (requestHeaderUserAgent.contains("MSIE")) {				// IE浏览器 --- URL编码
	            	attachTitle = URLEncoder.encode(attachTitle, "utf-8");
	            } else if (requestHeaderUserAgent.contains("Firefox")) {	// 火狐浏览器 --- Base64编码
	            	attachTitle = "=?UTF-8?B?" + new String(Base64.encode(attachTitle.getBytes("UTF-8"))).replaceAll("[\\t\\n\\r]", "") + "?=";
	            } else {
	            	attachTitle = URLEncoder.encode(attachTitle, "utf-8");
	            }
	        } catch (Exception e) {}
	        // 设置返回信息格式
	        response.setHeader("Content-disposition","attachment;filename="+attachTitle);
	        response.setContentType("application/x-download"); 
	      
	        in=new FileInputStream(file);
			response.setHeader("Content_length",String.valueOf(in.available())); 
			out = response.getOutputStream();
			IOUtils.copy(in, out);
			out.flush();
		} catch (ServiceException e) {
			logger.error(e.getMessage(),e);
			FileUtil.sendScriptMessage(e.getMessage(), response);
		}  catch (Exception e) {
			logger.error("下载文件异常",e);
			FileUtil.sendScriptMessage("下载文件异常", response);
		} finally{
        	IOUtils.closeQuietly(in);
        	FileUtils.deleteQuietly(file);
        }
		
		logger.debug("进入->下载文件方法，path:"+file.getAbsolutePath());
	}
	
	/**
	 * 压缩文件
	 * @param file
	 * @return
	 */
	public static File zip(File file) {
		try {
			File temp = File.createTempFile(file.getName(), ".zip");
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(temp));
			FileUtil.zip(file, out, "");
			IOUtils.closeQuietly(out);
			return temp;
		} catch (Exception e) {
			logger.error("文件夹压缩异常", e);
			throw new ServiceException("文件夹压缩异常", e);
		}
	}
	
	/**
	 * 压缩文件
	 * @param input
	 * @param output
	 */
	public static void zip(File input,File output) {
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));
			FileUtil.zip(input, out, "");
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			logger.error("文件夹压缩异常", e);
			throw new ServiceException("文件夹压缩异常", e);
		}
	}
	
	private static void zip(File file, ZipOutputStream out, String dir)throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				FileUtil.zip(f, out, dir + file.getName() + File.separator);
			}
		} else {
			out.putNextEntry(new ZipEntry(dir + file.getName()));
			FileInputStream fin = FileUtils.openInputStream(file);
			IOUtils.copy(fin, out);
			IOUtils.closeQuietly(fin);
			out.flush();
			out.closeEntry();
		}
	}
	
	/**
	 * 解压文件
	 * @param zfin
	 * @param target
	 */
	public static void unzip(InputStream zfin,File target) {
		ZipInputStream zin=null;
		try {
			zin = new ZipInputStream(zfin);
			ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
            	File temp = new File(target,ze.getName());
                if (ze.isDirectory()) {
                    if(!temp.isDirectory()) {
                    	temp.mkdirs();
                    }
                } else {
                    FileOutputStream fout = new FileOutputStream(temp);
                    try {
                        for (int c = zin.read(); c != -1; c = zin.read()) {
                            fout.write(c);
                        }
                        zin.closeEntry();
                    } finally {
                        IOUtils.closeQuietly(fout);
                    }
                }
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zin);
		}
	}
	
	/**
	 * 解压文件
	 * @param zfile
	 * @param target
	 */
	public static void unzip(File zfile,File target) {
		ZipInputStream zin=null;
		try {
			zin = new ZipInputStream(new FileInputStream(zfile));
			ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
            	File temp = new File(target,ze.getName());
                if (ze.isDirectory()) {
                    if(!temp.isDirectory()) {
                    	temp.mkdirs();
                    }
                } else {
                    FileOutputStream fout = new FileOutputStream(temp);
                    try {
                        for (int c = zin.read(); c != -1; c = zin.read()) {
                            fout.write(c);
                        }
                        zin.closeEntry();
                    } finally {
                        IOUtils.closeQuietly(fout);
                    }
                }
            }
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zin);
		}
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
			logger.error("输出页面脚本异常",e);
		}
	}
	
    /**
	 * 向客户端输出alert提示信息
	 * @param message
	 * @param response
	 */
    public static void sendTextMessage(String message, HttpServletResponse response) {
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
			logger.error("输出页面文本异常",e);
		}
	}

}
