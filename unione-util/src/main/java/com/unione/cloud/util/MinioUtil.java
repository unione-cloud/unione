package com.unione.cloud.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.unione.cloud.core.exception.ServiceException;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import io.minio.BucketExistsArgs;
import io.minio.DownloadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.messages.Bucket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MinioUtil {

	private static  MinioClient client = null;
	private static MinioConfig config;

	@Data
	@Component
	@RefreshScope
	public static class MinioConfig {
		@Value("${minio.server.url:}")
		private String url;

		@Value("${minio.server.key:}")
		private String key;

		@Value("${minio.server.secret:}")
		private String secret;

		@Value("${minio.server.secure:false}")
		private Boolean secure;
		/**
		 * 	允许最小的部分大小是5MiB在多部分上传，默认值：5 * 1024 * 1024 = 5242880。
		 */
		@Value("${minio.server.minMultipartSize:5242880}")
		private int minMultipartSize;
		/**
		 * 允许上传的后缀名,如：minio.server.suffix=pdf,txt,jpg,jpeg,doc,docx,zip 等
		 */
		@Value("${minio.server.suffix:}")
		private String suffix;
	}


	@Autowired
	public void setConfig(MinioConfig config) {
		MinioUtil.config = config;
	}

	/**
	 * 	链接minio
	 * @return
	 */
	public static MinioClient connect(MinioConfig config){
		log.debug("进入:链接minio方法");
		MinioClient	client=null;
		try {
			client = MinioClient.builder()
					.credentials(config.getKey(), config.getSecret())
					.endpoint(config.getUrl())
					.build(); 
		} catch (Exception e) {
			log.error("Minio服务链接失败",e);
			throw new ServiceException("Minio服务链接失败",e);
		}
		log.debug("退出:链接minio方法,client:{}",client);
		return  client;
	}

	public static MinioClient connect(){
		log.debug("进入:链接minio方法");
		try {
			if(client == null){
				client = MinioClient.builder()
						.credentials(config.getKey(), config.getSecret())
						.endpoint(config.getUrl())
						.build(); 
			}
		} catch (Exception e) {
			log.error("Minio服务链接失败",e);
			throw new ServiceException("Minio服务链接失败",e);
		}
		log.debug("退出:链接minio方法,client:{}",client);
		return  client;
	}

	/**
	 * 	创建bucket
	 * @param bucketName
	 */
	public static void createBucket(String bucketName) {
		log.debug("进入:创建bucket方法,bucketName:{}",bucketName);
		try {
			connect();
			BucketExistsArgs existArgs=BucketExistsArgs.builder().bucket(bucketName).build();
			if (!client.bucketExists(existArgs)) {
				MakeBucketArgs mkbkArgs=MakeBucketArgs.builder().bucket(bucketName).build();
				client.makeBucket(mkbkArgs);
			}
		}catch (Exception e){
			log.error("创建bucket失败,bucketName:{}",bucketName,e);
			throw new ServiceException("创建bucket失败");
		}
		log.debug("退出:创建bucket方法,bucketName:{}",bucketName);
	}

	/**
	 * 	创建bucket
	 * @param bucketName
	 */
	public static void createBucket(MinioClient client,String bucketName) {
		log.debug("进入:创建bucket方法,bucketName:{}",bucketName);
		try {
			BucketExistsArgs existArgs=BucketExistsArgs.builder().bucket(bucketName).build();
			if (!client.bucketExists(existArgs)) {
				MakeBucketArgs mkbkArgs=MakeBucketArgs.builder().bucket(bucketName).build();
				client.makeBucket(mkbkArgs);
			}
		}catch (Exception e){
			log.error("创建bucket失败,bucketName:{}",bucketName,e);
			throw new ServiceException("创建bucket失败");
		}
		log.debug("退出:创建bucket方法,bucketName:{}",bucketName);
	}

	/**
	 * 	获取全部bucket
	 */
	public static List<Bucket> getAllBuckets() {
		log.debug("进入:获取全部bucket方法");
		List<Bucket> list=null;
		try {
			connect();
			list=client.listBuckets();
		}catch (Exception e){
			log.error("获取全部bucket失败",e);
			throw new ServiceException("获取全部bucket失败");
		}
		log.debug("进入:获取全部bucket方法");
		return list;
	}

	/**
	 * 	获取全部bucket
	 */
	public static List<Bucket> getAllBuckets(MinioClient client) {
		log.debug("进入:获取全部bucket方法");
		List<Bucket> list=null;
		try {
			list=client.listBuckets();
		}catch (Exception e){
			log.error("获取全部bucket失败",e);
			throw new ServiceException("获取全部bucket失败");
		}
		log.debug("进入:获取全部bucket方法");
		return list;
	}

	/**
	 * 	获取Bucket信息
	 * @param name bucket名称
	 */
	public static Optional<Bucket> getBucket(String name) {
		log.debug("进入:获取Bucket信息方法,name:{}",name);
		Optional<Bucket> result=null;
		try {
			connect();
			result=client.listBuckets().stream().filter(b -> b.name().equals(name)).findFirst();
		}catch (Exception e){
			log.error("获取Bucket信息失败,name:{}",name,e);
			throw new ServiceException("获取Bucket信息失败" );
		}
		log.debug("退出:获取Bucket信息方法,name:{},result:{}",name,result);
		return result;
	}

	/**
	 * 	获取Bucket信息
	 * @param name bucket名称
	 */
	public static Optional<Bucket> getBucket(MinioClient client,String name) {
		log.debug("进入:获取Bucket信息方法,name:{}",name);
		Optional<Bucket> result=null;
		try {
			result=client.listBuckets().stream().filter(b -> b.name().equals(name)).findFirst();
		}catch (Exception e){
			log.error("获取Bucket信息失败,name:{}",name,e);
			throw new ServiceException("获取Bucket信息失败" );
		}
		log.debug("退出:获取Bucket信息方法,name:{},result:{}",name,result);
		return result;
	}

	/**
	 * 	删除Bucket信息
	 * @param name bucket名称
	 */
	public static void removeBucket(String name) {
		log.debug("进入:删除Bucket信息方法,name:{}",name);
		try {
			connect();
			RemoveBucketArgs args=RemoveBucketArgs.builder().bucket(name).build();
			client.removeBucket(args);
		}catch (Exception e){
			log.error("删除Bucket信息失败,name:{}",name,e);
			throw new ServiceException("删除Bucket信息失败");
		}
		log.debug("推出:删除Bucket信息方法,name:{}",name);
	}

	/**
	 * 	删除Bucket信息
	 * @param name bucket名称
	 */
	public static void removeBucket(MinioClient client,String name) {
		log.debug("进入:删除Bucket信息方法,name:{}",name);
		try {
			RemoveBucketArgs args=RemoveBucketArgs.builder().bucket(name).build();
			client.removeBucket(args);
		}catch (Exception e){
			log.error("删除Bucket信息失败,name:{}",name,e);
			throw new ServiceException("删除Bucket信息失败");
		}
		log.debug("推出:删除Bucket信息方法,name:{}",name);
	}

	/**
	 * 	上传文件
	 * @param stream	文件流对象
	 * @param remote	远程存储地址，必须 '/' 开头
	 */
	public static boolean upload(InputStream stream,String remote){
		log.debug("进入:上传文件方法,remote:{},stream:{}",remote,stream);
		File tmpf = null;
		try {
			// 校验是否合法文件扩展名
			if(!checkFileSuffix(FileUtil.getSuffix(remote))){
				return false;
			}
			connect();
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			createBucket(bucket);
			
			tmpf = File.createTempFile("minio-download", suffix);
			FileUtil.writeFromStream(stream, tmpf);
			
			UploadObjectArgs args=UploadObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.filename(tmpf.getAbsolutePath())
					.build();
			client.uploadObject(args);
			
		}catch (Exception e){
			log.error("上传文件失败,remote:{},stream:{}",remote,stream,e);
			throw new ServiceException("上传文件失败");
		} finally {
			FileUtil.del(tmpf);
			IoUtil.close(stream);
		}
		log.debug("退出:上传文件方法,remote:{},stream:{}",remote,stream);
		return true;
	}

	/**
	 * 	上传文件
	 * @param stream	文件流对象
	 * @param remote	远程存储地址，必须 '/' 开头
	 */
	public static boolean upload(MinioClient client,InputStream stream,String remote){
		log.debug("进入:上传文件方法,remote:{},stream:{}",remote,stream);
		
		File tmpf = null;
		try {
			// 校验是否合法文件扩展名
			if(!checkFileSuffix(FileUtil.getSuffix(remote))){
				return false;
			}
			
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			createBucket(bucket);
			
			tmpf = File.createTempFile("minio-download", suffix);
			FileUtil.writeFromStream(stream, tmpf);
			
			UploadObjectArgs args=UploadObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.filename(tmpf.getAbsolutePath())
					.build();
			client.uploadObject(args);
			
		}catch (Exception e){
			log.error("上传文件失败,remote:{},stream:{}",remote,stream,e);
			throw new ServiceException("上传文件失败");
		}
		log.debug("退出:上传文件方法,remote:{},stream:{}",remote,stream);
		return true;
	}

	/**
	 * 	上传文件
	 * @param file
	 * @param remote 远程存储地址，必须 '/' 开头
	 */
	public static boolean upload(File file,String remote){
		log.debug("进入:上传文件方法,remote:{},file:{},filePath:{}",remote,file,file.getPath());
		try {
			// 校验是否合法文件扩展名
			if(!checkFileSuffix(FileUtil.getSuffix(remote))){
				return false;
			}
			connect();
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			
			createBucket(bucket);
			
			UploadObjectArgs args=UploadObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.filename(file.getAbsolutePath())
					.build();
			client.uploadObject(args);
			
		}catch (Exception e){
			log.error("上传文件失败,remote:{},file:{},filePath:{}",remote,file,file.getPath(),e);
			throw new ServiceException("上传文件失败");
		}
		
		return true;
	}

	/**
	 * 	上传文件
	 * @param file
	 * @param remote 远程存储地址，必须 '/' 开头
	 */
	public static boolean upload(MinioClient client,File file,String remote){
		log.debug("进入:上传文件方法,remote:{},file:{},filePath:{}",remote,file,file.getPath());
		try {
			// 校验是否合法文件扩展名
			if(!checkFileSuffix(FileUtil.getSuffix(remote))){
				return false;
			}
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			
			createBucket(bucket);
			
			UploadObjectArgs args=UploadObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.filename(file.getAbsolutePath())
					.build();
			client.uploadObject(args);
			
		}catch (Exception e){
			log.error("上传文件失败,remote:{},file:{},filePath:{}",remote,file,file.getPath(),e);
			throw new ServiceException("上传文件失败");
		}
		
		return true;
	}

	/**
	 * 	下载文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 * @return
	 */
	public static InputStream download(String remote) {
		log.debug("进入:下载文件方法,remote:{}",remote);
		InputStream in=null;
		try {
			connect();
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			File tmpf = new File(System.getProperty("java.io.tmpdir"), UUID.fastUUID().toString()+"."+suffix);
			
			DownloadObjectArgs args = DownloadObjectArgs.builder()
			  .bucket(bucket)
			  .object(path)
			  .filename(tmpf.getAbsolutePath())
			  .build();
			
			client.downloadObject(args);
			
			in=FileUtils.openInputStream(tmpf);
			
		}catch (Exception e){
			log.error("下载文件失败,remote:{}",remote,e);
			throw new ServiceException("下载文件失败");
		}
		log.debug("退出:下载文件方法,remote:{},result:{}",remote,in);
		return in;
	}
	
	/**
	 * 	下载文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 * @return
	 */
	public static File download2file(String remote) {
		log.debug("进入:下载文件方法,remote:{}",remote);
		File tmpf = null;
		try {
			connect();
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			tmpf = new File(System.getProperty("java.io.tmpdir"), UUID.fastUUID().toString()+suffix);
			
			DownloadObjectArgs args = DownloadObjectArgs.builder()
			  .bucket(bucket)
			  .object(path)
			  .filename(tmpf.getAbsolutePath())
			  .build();
			
			client.downloadObject(args);
			
		}catch (Exception e){
			log.error("下载文件失败,remote:{}",remote,e);
			throw new ServiceException("下载文件失败");
		}
		log.debug("退出:下载文件方法,remote:{},result:{}",remote,tmpf.getAbsoluteFile());
		return tmpf;
	}
	

	/**
	 * 	下载文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 * @return
	 */
	public static InputStream download(MinioClient client,String remote) {
		log.debug("进入:下载文件方法,remote:{}",remote);
		InputStream in=null;
		try {
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			File tmpf = new File(System.getProperty("java.io.tmpdir"), UUID.fastUUID().toString()+suffix);
			
			DownloadObjectArgs args = DownloadObjectArgs.builder()
			  .bucket(bucket)
			  .object(path)
			  .filename(tmpf.getAbsolutePath())
			  .build();
			
			client.downloadObject(args);
			
			in=FileUtils.openInputStream(tmpf);
		}catch (Exception e){
			log.error("下载文件失败,remote:{}",remote,e);
			throw new ServiceException("下载文件失败");
		}
		log.debug("退出:下载文件方法,remote:{},result:{}",remote,in);
		return in;
	}
	
	/**
	 * 	下载文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 * @return
	 */
	public static File download2file(MinioClient client,String remote) {
		log.debug("进入:下载文件方法,remote:{}",remote);
		File tmpf = null;
		try {
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			String suffix=remote.substring(remote.lastIndexOf("."),remote.length());
			
			tmpf = new File(System.getProperty("java.io.tmpdir"), UUID.fastUUID().toString()+suffix);
			
			DownloadObjectArgs args = DownloadObjectArgs.builder()
			  .bucket(bucket)
			  .object(path)
			  .filename(tmpf.getAbsolutePath())
			  .build();
			
			client.downloadObject(args);
			
		}catch (Exception e){
			log.error("下载文件失败,remote:{}",remote,e);
			throw new ServiceException("下载文件失败");
		}
		log.debug("退出:下载文件方法,remote:{},result:{}",remote,tmpf.getAbsoluteFile());
		return tmpf;
	}

	
	/**
	 * 	删除文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 */
	public static boolean remove(String remote) {
		log.debug("进入:删除文件方法,remote:{}",remote);
		try {
			connect();
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			RemoveObjectArgs args=RemoveObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.build();
			client.removeObject(args);
		}catch (Exception e){
			log.error("删除失败,remote:{}",remote,e);
			throw new ServiceException("删除失败");
		}
		log.debug("退出:删除文件方法,remote:{}",remote);
		return true;
	}

	/**
	 * 	删除文件
	 * @param remote 远程文件地址，必须 '/' 开头
	 */
	public static boolean remove(MinioClient client,String remote) {
		log.debug("进入:删除文件方法,remote:{}",remote);
		try {
			String bucket=remote.split("/")[1];
			String path=remote.substring(bucket.length()+2,remote.length());
			RemoveObjectArgs args=RemoveObjectArgs.builder()
					.bucket(bucket)
					.object(path)
					.build();
			client.removeObject(args);
		}catch (Exception e){
			log.error("删除失败,remote:{}",remote,e);
			throw new ServiceException("删除失败");
		}
		log.debug("退出:删除文件方法,remote:{}",remote);
		return true;
	}

	/**
	 * 校验文件扩展名是否合法
	 *
	 * @param fileSuffix
	 * @return
	 */
	public static boolean checkFileSuffix(String fileSuffix) {
		// 如果配置了扩展名，则进行校验，否则不校验
		if (StringUtils.isNotBlank(config.getSuffix()) && StringUtils.isNotBlank(fileSuffix)) {
			log.debug("允许上传有效文件扩展名为：{}",config.getSuffix());
			// 配置的扩展名字符串中包含上传文件的扩展名则验证通过，否则验证失败
			if (config.getSuffix().indexOf(fileSuffix) > -1) {
				return true;
			}
			log.warn("非法文件扩展名：{}",fileSuffix);
			return false;
		} else {
			log.warn("没有配置允许上传的文件扩展名，放行通过");
			return true;
		}
	}

}
