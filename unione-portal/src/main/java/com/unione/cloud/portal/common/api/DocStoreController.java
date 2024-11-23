package com.unione.cloud.portal.common.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.portal.common.model.DocDir;
import com.unione.cloud.portal.common.model.DocFile;
import com.unione.cloud.portal.common.model.DocPermis;
import com.unione.cloud.portal.common.service.DocCacheService;
import com.unione.cloud.portal.common.service.DocCacheService.DocData;
import com.unione.cloud.portal.common.service.DocPermisService;
import com.unione.cloud.util.AttachUtil;
import com.unione.cloud.util.AttachUtil.Attach;
import com.unione.cloud.util.FileUtil;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 DocFile Controller 服务
 * @作者	Jeking Yang
 * @日期	2023年8月29日 下午11:29:27
 * @版本	1.0.0
 **/
@Slf4j
@RefreshScope
@RestController
@Api(tags = "doc管理：文件存储 管理服务")
@RequestMapping("/api/store")
public class DocStoreController{
	/**
	 * 	数据访问对象
	 */
	@Autowired
	private DataBaseDao dataBaseDao;

	/**
	 * 	用户会话对象
	 */
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DocPermisService docPermisService;
	
	/**
	 * 	doc缓存服务对象
	 */
	@Autowired
	private DocCacheService docCacheService;
	
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private HttpServletRequest request;
	
//	@Autowired
//	private PdfService pdfConvertor;
	
	@Value("${doc.convert.regix:(doc)|(docx)|(ppt)|(pptx)}")
	private String PDF_CONVERT_REGIX;
	
	/**
	 * 	文件物流删除开关，默认：物理删除
	 */
	@Value("${doc.delete.physically:true}")
	private boolean FILE_PHYSICALLY_DELETED;
	
	
	/**
	 * 	数据权限：机构权限开关，默认开启
	 */
	@Value("${doc.permis.org.enable:true}")
	private boolean ORG_PERMIS;
	
	
	/**
	 * 	数据权限：文档权限开关，默认开启
	 */
	@Value("${doc.permis.file.enable:false}")
	private boolean FILE_PERMIS_ENABLE;
	

	@ApiOperation("上传文件[单个]")
	@PostMapping(value="/upload/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		log.debug("进入:上传文件方法,appCode:{},ownerId:{}",appCode,ownerId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Insert, "上传文件");
			logSaveFlag=true;
		}
		
		LogsUtil.add("设置参数信息");
		DocFile doc=new DocFile();
		doc.setAppCode(appCode);
		doc.setOwnerId(ownerId);
		doc.setDirId(dirId!=null&&!"null".equals(dirId)?Long.parseLong(dirId):null);
		doc.setName(name!=null&&!"null".equals(name)?name:null);
		doc.setIsPublic(isPublic!=null?isPublic:0);
		doc.setExtData(extData!=null&&!"null".equals(extData)?extData:null);
		doc.setDescs(descs!=null&&!"null".equals(descs)?descs:null);
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(file, "文件不能为空")
			.isTrue(!file.isEmpty(), "文件不能为空")
			.notNull(doc,new String[] {"appCode"}, "属性%s不能为空");
		
		LogsUtil.add("设置默认信息");
		doc.setStatus(1);
		doc.setDelFlag(0);
		
		LogsUtil.add("保存附件");
		Attach attach = AttachUtil.save(file, "/"+appCode,doc.getIsPublic());
		AssertUtil.service().isTrue(attach.isSuccess(), "附件保存失败");
		
		LogsUtil.add("保存附件信息");
		LogsUtil.add("保存附件path："+attach.getPath());
		LogsUtil.add("保存附件type："+attach.getTypes());
		doc.setPath(attach.getPath());
		doc.setTitle(attach.getTitle());
		doc.setSize(attach.getSize());
		doc.setType(attach.getTypes());
		
		LogsUtil.add("保存文件记录");
		int len = dataBaseDao.insert(doc);
		
		//如果是公开文件，则创建公开文件权限记录
		if(FILE_PERMIS_ENABLE && doc.getIsPublic()==1) {
			DocPermis permis=new DocPermis();
			permis.setOwnerType("public");
			permis.setOwnerTitle("文件公开");
			permis.setList("view,dowland");
			docPermisService.save(doc, Arrays.asList(permis));
		}
		
		log.debug("退出:上传文件方法,appCode:{},ownerId:{}",appCode,ownerId);
		if(logSaveFlag) {
			//保存日志
			LogsUtil.save(len>0,doc.getId());
		}
		return Results.build(len>0, doc);
	}


	@ApiOperation(value="上传文件[单个]",notes = "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
	@PostMapping(value="/upload/{appCode}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		return this.upload(file, appCode, null, dirId, name, isPublic, extData, descs);
	}


	@ApiOperation("上传文件[批量]")
	@PostMapping(value="/upload/batch/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<List<DocFile>> uploadBatch(@RequestPart("file") List<MultipartFile> files,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		log.debug("进入:上传文件方法,appCode:{},ownerId:{}",appCode,ownerId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Insert, "上传文件[批量]");
			logSaveFlag=true;
		}
		List<DocFile> list=new ArrayList<>();
		
		LogsUtil.add("设置参数信息");
		DocFile doc=new DocFile();
		doc.setAppCode(appCode);
		doc.setOwnerId(ownerId);
		doc.setDirId(dirId!=null&&!"null".equals(dirId)?Long.parseLong(dirId):null);
		doc.setName(name!=null&&!"null".equals(name)?name:null);
		doc.setIsPublic(isPublic!=null?isPublic:0);
		doc.setExtData(extData!=null&&!"null".equals(extData)?extData:null);
		doc.setDescs(descs!=null&&!"null".equals(descs)?descs:null);
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(files, "文件不能为空")
			.isTrue(!files.isEmpty(), "文件不能为空")
			.notNull(doc,new String[] {"appCode"}, "属性%s不能为空");
		
		LogsUtil.add("设置默认信息");
		doc.setTenantId(sessionService.getTenantId());
		doc.setOrgId(sessionService.getOrgId());
		doc.setUserId(sessionService.getUserId());
		doc.setStatus(1);
		doc.setDelFlag(0);
		doc.setCreated(DateUtil.date());
		doc.setCreatedBy(sessionService.getUserId());
		doc.setLastUpdated(DateUtil.date());
		doc.setLastUpdatedBy(sessionService.getUserId());
		
		LogsUtil.add("保存附件,count:"+files.size());
		for(MultipartFile file:files) {
			DocFile dfile=new DocFile();
			BeanUtils.copyProperties(doc, dfile);		//复制基本属性
			Attach attach = AttachUtil.save(file, "/"+appCode,doc.getIsPublic());
			AssertUtil.service().isTrue(attach.isSuccess(), "附件保存失败");
			
			LogsUtil.add("保存附件信息");
			LogsUtil.add("保存附件path："+attach.getPath());
			LogsUtil.add("保存附件type："+attach.getTypes());
			dfile.setPath(attach.getPath());
			dfile.setTitle(attach.getTitle());
			dfile.setSize(attach.getSize());
			dfile.setType(attach.getTypes());
			
			LogsUtil.add("保存文件记录");
			int len = dataBaseDao.insert(dfile);
			LogsUtil.add("保存附件id:"+dfile.getId());
			LogsUtil.add("保存附件len:"+len);
			if(len<=0) {
				dfile.setPath("error");
			}else {
				//如果是公开文件，则创建公开文件权限记录
				if(FILE_PERMIS_ENABLE && dfile.getIsPublic()==1) {
					DocPermis permis=new DocPermis();
					permis.setOwnerType("public");
					permis.setOwnerTitle("文件公开");
					permis.setList("view,dowland");
					docPermisService.save(dfile, Arrays.asList(permis));
				}
			}
			list.add(dfile);
		}
		
		log.debug("退出:上传文件[批量]方法,appCode:{},ownerId:{}",appCode,ownerId);
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		return Results.success(list);
	}

	@ApiOperation(value="上传文件[批量]",notes = "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
	@PostMapping(value="/upload/batch/{appCode}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<List<DocFile>> uploadBatch(@RequestPart("file") List<MultipartFile> files,
			@PathVariable("appCode") String appCode,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		return this.uploadBatch(files, appCode, null,dirId, name, isPublic, extData, descs);
	}


	@PostMapping("/delete/{fileId}")
	@ApiOperation(value="删除文件",notes = "根据文件id删除")
	public Results<Void> delete(@PathVariable("fileId") Long fileId) {
		log.debug("进入:删除文件存储信息方法，fileId:{}",fileId);
		Results<Void> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Delete, "删除文档存储");
			logSaveFlag=true;
		}
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFile entity=new DocFile();
		entity.setId(fileId);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		LogsUtil.add("查询文档记录");
		DocFile tmp=dataBaseDao.findById(entity);
		AssertUtil.service().notNull(tmp, "文件记录未找到");
		LogsUtil.setTarget(tmp.getId(), tmp.getTitle());
		
		int len = 0;
		if(FILE_PHYSICALLY_DELETED) {
			LogsUtil.add("删除文档存储,path:"+tmp.getRealPath());
			boolean flag = AttachUtil.delete(tmp.getRealPath());
			LogsUtil.add("删除文档存储结果:%s",flag);
//			AssertUtil.service().isTrue(flag, "文档删除失败");
			
			// 删除pdf文件
			String pdfPath=tmp.getRealPath().substring(0,tmp.getRealPath().lastIndexOf("."))+".pdf";
			AttachUtil.delete(pdfPath);
			LogsUtil.add("删除文档记录");
			len = dataBaseDao.deleteById(SqlBuilder.build(DocFile.class, tmp.getId()));
			LogsUtil.add("成功删除记录数量:"+len);
		}else {
			len = dataBaseDao.deleteLogicById(SqlBuilder.build(DocFile.class, tmp.getId()));
		}
		
		// 删除缓存
		docCacheService.delDocData(tmp.getId());

		log.debug("退出:删除文件存储信息方法，fileId:{}",fileId);
		if(logSaveFlag) {
			//保存日志
			LogsUtil.save(len>0);
		}
		return results.setSuccess(len>0);
	}


	@PostMapping("/delete/owner/{ownerId}")
	@ApiOperation(value="删除文件",notes = "根据文件归属id删除,响应数据body中的数据定义为[文件总数,成功总数]")
	public Results<Integer[]> deleteByOwner(@PathVariable("ownerId") Long ownerId) {
		log.debug("进入:删除文件存储信息方法，ownerId:{}",ownerId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Delete, "删除文档存储[ByOwner]");
			logSaveFlag=true;
		}
		
		LogsUtil.setTarget(ownerId, "归属Id:"+ownerId);
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(ownerId, "参数ownerId不能为空");
		
		LogsUtil.add("参数处理");
		DocFile entity=new DocFile();
		entity.setOwnerId(ownerId);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		LogsUtil.add("查询文档记录");
		List<DocFile> list=dataBaseDao.findList(entity);
		AssertUtil.service().notNull(list, "文件记录未找到").isTrue(!list.isEmpty(), "文件记录未找到");
		LogsUtil.add("查询文档记录,count:"+list.size());
		
		Integer count=0;
		for(DocFile tmp:list) {
			LogsUtil.add("删除文档存储,path:"+tmp.getRealPath());
			if(FILE_PHYSICALLY_DELETED) {
				boolean flag = AttachUtil.delete(tmp.getRealPath());
				LogsUtil.add("删除文档存储结果:%s",flag);
				try {
//					AssertUtil.service().isTrue(flag, "文档删除失败");
					
					LogsUtil.add("删除文档记录");
					int len = dataBaseDao.deleteById(SqlBuilder.build(DocFile.class, tmp.getId()));
					LogsUtil.add("删除文档结果:%s",len);
					if(len>0) {
						count=count+1;
					}
					LogsUtil.add("成功删除文档count:%s",count);
				} catch (Exception e) {
					LogsUtil.add("删除文档失败,id:"+tmp.getId());
					log.error("删除文档失败,id:{}",tmp.getId(),e);
				}
			}else {
				LogsUtil.add("删除文档记录:逻辑");
				int len = dataBaseDao.deleteLogicById(SqlBuilder.build(DocFile.class, tmp.getId()));
				LogsUtil.add("删除文档结果:%s",len);
				if(len>0) {
					count=count+1;
				}
				LogsUtil.add("成功删除文档count:%s",count);
			}
			
			// 删除缓存
			docCacheService.delDocData(tmp.getId());
		}
		
		// 逻辑删除文件信息
		List<Long> ids = list.stream().map(d->d.getId()).collect(Collectors.toList());
		int len = dataBaseDao.deleteLogicById(SqlBuilder.build(DocFile.class, ids));
		LogsUtil.add("成功删除记录数量:"+len);

		log.debug("退出:删除文件存储信息方法，ownerId:{}",ownerId);
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		return Results.success(new Integer[] {list.size(),count});
	}


	@GetMapping("/download/{fileId}")
	@ApiOperation(value="下载文件",notes = "根据文件id下载")
	public void download(@PathVariable("fileId") Long fileId) {
		log.debug("进入:文件下载方法，fileId:{}",fileId);
		File file = this.downloadFile(fileId);
		if(file!=null) {
			DocData cache=docCacheService.getDocData(fileId);
			// 下载文件
			AttachUtil.download(file, cache!=null?cache.getTitle():file.getName(), request, response);
		}
	}
	
	
	public File downloadFile(Long fileId) {
		log.debug("进入:文件下载方法，fileId:{}",fileId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "文件下载");
			logSaveFlag=true;
		}
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFile entity=new DocFile();
		entity.setId(fileId);
		entity.setIncPublic(true);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		if(FILE_PERMIS_ENABLE) {
			entity.setPermisUser(sessionService.getUserId());
			if(ORG_PERMIS) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				//entity.getPermisOwners().addAll(sessionService.getUserRoles());
			}
		}
		DocFile tmp=dataBaseDao.findOne(entity);
		AssertUtil.service().notNull(tmp, "文件记录未找到").notNull(tmp.getPath(), "文件存储path为空");
		LogsUtil.setTarget(tmp.getId(), tmp.getTitle());
		
		
		LogsUtil.add("查询文档记录");
		DocData cache=docCacheService.getDocData(fileId);
		File file = null;
		if(cache==null || (file=cache.getFile())==null) {
			// 如果是公开文档，则添加public
			if(tmp.getIsPublic()!=null && tmp.getIsPublic()==1) {
				tmp.setPath("/public"+tmp.getPath());
			}
			
			LogsUtil.add("下载文档,path:"+tmp.getPath());
			file = AttachUtil.download(tmp.getPath());
			if(file!=null) {
				// 设置文件缓存
				docCacheService.setDocData(fileId, DocData.build(tmp, file));
			}else {
				AttachUtil.sendScriptMessage("文件未找到", response);
			}
		}else {
			LogsUtil.add("成功从缓存加载文件");
			LogsUtil.setTarget(cache.getFileId(), cache.getTitle());
			// 下载文件
			AttachUtil.download(file, cache.getTitle(), request, response);
			log.debug("退出:文件下载方法，fileId:{},path:{}",fileId,cache.getPath());
		}
		
		if(logSaveFlag) {
			// 保存日志
			LogsUtil.success();
		}
		return file;
	}

	
	@PostMapping("/download")
	@ApiOperation(value="下载文件【批量】",notes = "根据文件id下载")
	public void download(@RequestBody List<Long> fileIds) {
		log.debug("进入:文件下载方法，fileIds:{}",fileIds);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "文件下载【批量】");
			logSaveFlag=true;
		}
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileIds, "参数fileIds不能为空")
			.notEmpty(fileIds, "参数fileIds不能为空");
		LogsUtil.add("文件下载批量ID:"+fileIds);
		
		LogsUtil.add("参数处理");
		DocFile entity=new DocFile();
		entity.setIncPublic(true);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		if(FILE_PERMIS_ENABLE) {
			entity.setPermisUser(sessionService.getUserId());
			if(ORG_PERMIS) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				//entity.getPermisOwners().addAll(sessionService.getUserRoles());
			}
		}
		
		LogsUtil.add("查询文档记录");
		SqlBuilder<DocFile>	builder=SqlBuilder.build(entity).ids(fileIds);
		List<DocFile> docs = dataBaseDao.findList(builder);
		AssertUtil.service().notEmpty(docs, "文件记录未找到");
		LogsUtil.setTarget(docs.get(0).getId(), docs.get(0).getTitle());
		
		File dir=new File(System.getProperty("java.io.tmpdir"), "file.download.bt."+UUID.fastUUID().toString());
		dir.mkdir();
		Map<String, Integer> map=new HashMap<>();
		
		LogsUtil.add("下载文件：开始");
		for(DocFile doc:docs) {
			DocData cache=docCacheService.getDocData(doc.getId());
			File file = cache!=null?cache.getFile():null;
			if(cache==null || file==null) {
				LogsUtil.add("下载文档,path:"+doc.getPath());
				file = AttachUtil.download(doc.getRealPath());
				if(file!=null) {
					// 设置文件缓存
					docCacheService.setDocData(doc.getId(), DocData.build(doc, file));
				}else {
					LogsUtil.add("文件未找到，title:"+doc.getTitle());
					LogsUtil.add("文件未找到，path:"+doc.getRealPath());
					continue;
				}
			}else {
				LogsUtil.add("成功从缓存加载文件");
			}
			
			// 下载文件,加入批量下载目录
			Integer c=map.get(doc.getTitle());
			if(c==null) {
				c=0;
			}
			c++;
			map.put(doc.getTitle(), c);
			try {
				File tmp=new File(dir,c>1?(doc.getTitle()+".["+c+"]."+doc.getType()):doc.getTitle());
				boolean flag = tmp.createNewFile();
				if(flag) {
					FileUtils.copyFile(file, tmp);
					FileUtils.deleteQuietly(file);
				}else {
					LogsUtil.add("文件拷贝失败，title:"+doc.getTitle());
					LogsUtil.add("文件拷贝失败，path:"+doc.getRealPath());
				}
			} catch (IOException e) {
				LogsUtil.add("文件下载失败，title:"+doc.getTitle());
				LogsUtil.add("文件下载失败，path:"+doc.getRealPath());
			}
		}
		LogsUtil.add("下载文件：完成");
		
		LogsUtil.add("压缩文件：开始,file count:"+(dir.list()!=null?dir.list().length:0));
		File zipFile=FileUtil.zip(dir);
		LogsUtil.add("压缩文件：完成");
		
		LogsUtil.add("下载压缩文件");
		AttachUtil.download(zipFile,DateUtil.format(new Date(), "批量下载文件-yyyymmdd")+".zip", request, response);
		
		FileUtils.deleteQuietly(zipFile);
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
	}

	
	@PostMapping("/download/dir")
	@ApiOperation(value="下载文档【批量】",notes = "批量下载文件和文件夹")
	public void download(@RequestParam("fileIds") List<Long> fileIds,@RequestParam("dirIds") List<Long> dirIds) {
		log.debug("进入:下载文档【批量】方法，fileIds:{},dirIds:{}",fileIds,dirIds);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "下载文档【批量】");
			logSaveFlag=true;
		}
		
		LogsUtil.add("参数验证");
		AssertUtil.service().isTrue(fileIds!=null&&!fileIds.isEmpty() || 
				dirIds!=null&&!dirIds.isEmpty(), "参数fileIds和dirIds不能都为空");
		
		List<DocDir> docDirs=new ArrayList<>();
		List<DocFile> docFiles=new ArrayList<>();
//		if(dirIds!=null && !dirIds.isEmpty()) {
//			LogsUtil.add("查询文件夹列表");
//			DocDir entity=new DocDir();
//			entity.setTenantId(sessionService.getTenantId());
////			entity.setIds(dirIds);
//			entity.setDelFlag(0);
//			
//			if(FILE_PERMIS_ENABLE) {
//				entity.setPermisUser(sessionService.getUserId());
//				if(ORG_PERMIS) {
//					entity.setPermisOrg(sessionService.getOrgId());
//				}
//				entity.getPermisOwners().add(sessionService.getUserId());
//				if(sessionService.getOrgId()!=null) {
//					entity.getPermisOwners().add(sessionService.getOrgId());
//				}
//				if(sessionService.getUserRoles()!=null) {
//					entity.getPermisOwners().addAll(sessionService.getUserRoles());
//				}
//			}
//			
//			docDirs=dataBaseDao.selectList("findByIdsDocDir",entity);
//		}
//		
//		if(download.getFileIds()!=null&&!download.getFileIds().isEmpty()) {
//			LogsUtil.add("查询文件列表");
//			DocFile entity=new DocFile();
//			entity.setTenantId(sessionService.getTenantId());
//			entity.setIds(download.getFileIds());
//			entity.setDelFlag(0);
//			entity.setIncPublic(true);
//			
//			if(FILE_PERMIS_ENABLE) {
//				entity.setPermisUser(sessionService.getUserId());
//				if(ORG_PERMIS) {
//					entity.setPermisOrg(sessionService.getOrgId());
//				}
//				entity.getPermisOwners().add(sessionService.getUserId());
//				if(sessionService.getOrgId()!=null) {
//					entity.getPermisOwners().add(sessionService.getOrgId());
//				}
//				if(sessionService.getUserRoles()!=null) {
//					entity.getPermisOwners().addAll(sessionService.getUserRoles());
//				}
//			}
//			
//			docFiles=dataBaseDao.selectList("findByIdsDocFile",entity);
//		}
//		
//		if(docDirs.isEmpty() && docFiles.isEmpty()) {
//			LogsUtil.add("文件夹信息和文件信息都为空");
//			AttachUtil.sendScriptMessage("文件夹信息和文件信息都为空", response);
//			LogsUtil.failure(new ServiceException("文件夹信息和文件信息都为空"));
//			return;
//		}
//		
//		File zipFileDir = docService.download(docDirs, docFiles,"批量下载文件",null);
//		if(zipFileDir==null) {
//			LogsUtil.add("文件下载失败");
//			AttachUtil.sendScriptMessage("文件下载失败", response);
//			LogsUtil.failure(new ServiceException("文件下载失败"));
//			return;
//		}
//		
//		LogsUtil.add("压缩文件：开始");
//		File zipFile=FileUtil.zip(zipFileDir);
//		LogsUtil.add("压缩文件：完成");
//		
//		LogsUtil.add("下载压缩文件");
//		AttachUtil.download(zipFile,DateUtil.formatCharacter(new Date(), "批量下载文件-yyyymmdd")+".zip", request, response);
//		
//		FileUtils.deleteQuietly(zipFile);
//		FileUtils.deleteQuietly(zipFileDir);
//		
//		if(logSaveFlag) {
//			// 保存日志
//			LogsUtil.success();
//		}
	}
	

	@GetMapping("/preview/{fileId}")
	@ApiOperation(value="预览文件",notes = "根据文件id下载")
	public void preview(@PathVariable("fileId") Long fileId) {
		log.debug("进入:文件预览方法，fileId:{}",fileId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "文件预览");
			logSaveFlag=true;
		}
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFile entity=new DocFile();
		entity.setId(fileId);
		entity.setIncPublic(true);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		if(FILE_PERMIS_ENABLE) {
			entity.setPermisUser(sessionService.getUserId());
			if(ORG_PERMIS) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				//entity.getPermisOwners().addAll(sessionService.getUserRoles());
			}
		}
		
		LogsUtil.add("查询文档记录");
		DocData cache=docCacheService.getDocData(fileId);
		File file = cache!=null?cache.getFile():null;
		if(cache!=null && !StringUtils.isEmpty(cache.getError())) {
			// 直接输出错误信息
			AttachUtil.sendHtmlMessage(cache.getError(), response);
		}else if(cache!=null && file!=null) {
			LogsUtil.add("成功从缓存加载文件");
			LogsUtil.setTarget(cache.getFileId(), cache.getTitle());
			AttachUtil.preview(file, response);
		} else {
			DocFile tmp=dataBaseDao.findById(SqlBuilder.build(entity));
			// 数据验证
			if(tmp==null) {
//				docCacheService.setDocData(fileId, DocData.error(fileId, "文件记录未找到"));
				AttachUtil.sendHtmlMessage("文件记录未找到", response);
				return;
			}else if(StringUtils.isEmpty(tmp.getPath())) {
//				docCacheService.setDocData(fileId, DocData.error(fileId, "文件存储path为空"));
				AttachUtil.sendHtmlMessage("文件存储path为空", response);
				return;
			}
			LogsUtil.setTarget(tmp.getId(), tmp.getTitle());
			
			// 如果是公开文档，则添加public
			if(tmp.getIsPublic()!=null && tmp.getIsPublic()==1) {
				tmp.setPath("/public"+tmp.getPath());
			}
			
			LogsUtil.add("预览文档,path:"+tmp.getPath());
			
//			if(tmp.getType()!=null && tmp.getType().matches(PDF_CONVERT_REGIX) && pdfConvertor.enable()) {
//				file = pdfConvertor.convert2pdf(tmp.getPath());
//				
//				// 设置文件缓存
//				if(file!=null) {
//					tmp.setType("pdf");
//					docCacheService.setDocDataPreview(fileId, DocData.build(tmp, file));
//				}
//				
//				AttachUtil.preview(file, response);
//			}else {
				file = AttachUtil.download(tmp.getPath());
				if(file!=null) {
					// 设置文件缓存
					docCacheService.setDocData(fileId, DocData.build(tmp, file));
					// 文件预览
					AttachUtil.preview(file, response);
					log.debug("退出:文件下载方法，fileId:{},path:{}",fileId,tmp.getPath());
				}else {
					AttachUtil.sendScriptMessage("文件未找到", response);
				}
//			}
			log.debug("退出:文件预览方法，fileId:{},path:{}",fileId,tmp.getPath());
		}
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		log.debug("退出:文件预览方法，fileId:{}",fileId);
	}
	

	@GetMapping("/preview/public/{fileId}")
	@ApiOperation(value="预览文件【公开】",notes = "根据文件id下载，公开文件，不进行验证")
	public void previewPublic(@PathVariable("fileId") Long fileId) {
		log.debug("进入:文件预览【公开】方法，fileId:{}",fileId);
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		log.debug("查询文档记录");
		DocData cache=docCacheService.getDocData(fileId);
		File file = cache!=null?cache.getFile():null;
		if(cache!=null && !StringUtils.isEmpty(cache.getError())) {
			// 直接输出错误信息
			AttachUtil.sendHtmlMessage(cache.getError(), response);
		}else if(cache==null || file==null) {
			DocFile tmp=dataBaseDao.findById(SqlBuilder.build(DocFile.class,fileId));
			
			// 数据验证
			if(tmp==null || tmp.getIsPublic()==null || tmp.getIsPublic()!=1) {
//				docCacheService.setDocData(fileId, DocData.error(fileId, "文件记录未找到或未公开"));
				AttachUtil.sendHtmlMessage("文件记录未找到或未公开", response);
				return;
			}else if(StringUtils.isEmpty(tmp.getPath())) {
//				docCacheService.setDocData(fileId, DocData.error(fileId, "文件存储path为空"));
				AttachUtil.sendHtmlMessage("文件存储path为空", response);
				return;
			}
			
			// 如果是公开文档，则添加public
			if(tmp.getIsPublic()!=null && tmp.getIsPublic()==1) {
				tmp.setPath("/public"+tmp.getPath());
			}
			
//			log.debug("预览文档,path:"+tmp.getPath());
//			if(tmp.getType()!=null && tmp.getType().matches(PDF_CONVERT_REGIX) && pdfConvertor.enable()) {
//				file = pdfConvertor.convert2pdf(tmp.getPath());
//				
//				// 设置文件缓存
//				if(file!=null) {
//					docCacheService.setDocDataPreview(fileId, DocData.build(tmp, file));
//				}
//				AttachUtil.preview(file, response);
//			}else {
				file = AttachUtil.download(tmp.getPath());
				if(file!=null) {
					// 设置文件缓存
					docCacheService.setDocData(fileId, DocData.build(tmp, file));
					
					// 文件预览
					AttachUtil.preview(file, response);
					log.debug("退出:文件下载方法，fileId:{},path:{}",fileId,tmp.getPath());
				}else {
					AttachUtil.sendScriptMessage("文件未找到", response);
				}
//			}
			log.debug("退出:文件预览方法，fileId:{},path:{}",fileId,tmp.getPath());
		}else {
			log.debug("成功从缓存加载文件");
			AttachUtil.preview(file, response);
		}
		
		log.debug("退出:文件预览【公开】方法，fileId:{}",fileId);
	}


}
