package com.unione.cloud.common.service;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;



public interface DocStoreService{
	
	
	@Operation(summary ="上传文件[单个]")
	@PostMapping(value="/upload/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs);
	
	
	@Operation(summary = "上传文件[单个]",description = "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
	@PostMapping(value="/upload/{appCode}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs);
	
	
	@Operation(summary ="上传文件[单个]")
	@PostMapping(value="/upload/public/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs);
	
	
	@Operation(summary =  "上传文件[批量]")
	@PostMapping(value="/upload/batch/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<List<DocFile>> uploadBatch(@RequestPart("file") List<MultipartFile> files,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs);
	
	
	
	@Operation(summary = "上传文件[批量]",description = "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
	@PostMapping(value="/upload/batch/{appCode}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<List<DocFile>> uploadBatch(@RequestPart("file") List<MultipartFile> files,
			@PathVariable("appCode") String appCode,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs);
	
	
	@PostMapping("/delete/{fileId}")
	@Operation(summary = "删除文件",description = "根据文件id删除")
	public Results<Void> delete(@PathVariable("fileId") Long fileId);
	
	@PostMapping("/delete/owner/{ownerId}")
	@Operation(summary = "删除文件",description = "根据文件归属id删除,响应数据body中的数据定义为[文件总数,成功总数]")
	public Results<Integer[]> deleteByOwner(@PathVariable("ownerId") Long ownerId);
	
	
	@GetMapping("/download/{fileId}")
	@Operation(summary = "下载文件",description = "根据文件id下载")
	public void download(@PathVariable("fileId") Long fileId);
	
	
	@PostMapping("/download")
	@Operation(summary = "下载文件【批量】",description = "根据文件id下载")
	public void download(@RequestBody Set<Long> fileIds);
	
	
	@GetMapping("/preview/{fileId}")
	@Operation(summary = "预览文件",description = "根据文件id下载")
	public void preview(@PathVariable("fileId") Long fileId);
	
	
	@GetMapping("/preview/public/{fileId}")
	@Operation(summary = "预览文件【公开】",description = "根据文件id下载，公开文件，不进行验证")
	public void previewPublic(@PathVariable("fileId") Long fileId);

	@GetMapping("/stream/{fileId}.{suffix}")
	@Operation(summary = "媒体流下载",description = "根据文件id下载媒体流")
	public ResponseEntity<byte[]> stream(@PathVariable("fileId") Long fileId,@PathVariable("suffix") String suffix);

}
