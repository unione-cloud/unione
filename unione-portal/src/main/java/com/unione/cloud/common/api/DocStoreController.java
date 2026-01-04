package com.unione.cloud.common.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.unione.cloud.common.dto.DocFileDto;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.common.model.DocPermis;
import com.unione.cloud.common.service.DocCacheService;
import com.unione.cloud.common.service.DocCacheService.DocData;
import com.unione.cloud.common.service.DocPermisService;
import com.unione.cloud.common.service.DocStoreService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.service.CodeTreeService;
import com.unione.cloud.util.AttachUtil;
import com.unione.cloud.util.AttachUtil.Attach;
import com.unione.cloud.util.FileUtil;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@Tag(name = "doc管理：文件存储 管理服务")
@RequestMapping("/api/common/store")
public class DocStoreController implements DocStoreService{

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

	@Autowired
	private CodeTreeService codeTreeService;
	
//	@Autowired
//	private PdfService pdfConvertor;
	
	@Value("${doc.convert.regix:(doc)|(docx)|(ppt)|(pptx)}")
	private String PDF_CONVERT_REGIX;
	
	/**
	 * 	文件物理删除开关，默认：物理删除
	 */
	@Value("${doc.delete.physically:true}")
	private boolean FILE_PHYSICALLY_DELETED;
	
	
	/**
	 * 文档权限级别：tenant(租户)、organ(机构)、user(用户)
	 */
	@Value("${doc.permis.level:tenant}")
	private String PERMIS_LEVEL;
	

	private String DOCTREECODE="UNIONE:DOCTREE";
	

	@Action(title="上传文件",type=ActionType.Upload)
	@Operation(summary="上传文件[单个]-ownerId")
	@PostMapping(value="/upload/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<DocFile> upload(@RequestPart("file") MultipartFile file,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		
		LogsUtil.add("设置参数信息");
		DocFile doc=new DocFile();
		doc.setAppCode(appCode);
		doc.setOwnerId(ownerId);
		doc.setDirId(dirId!=null&&!"null".equals(dirId)?Long.parseLong(dirId):-1L);
		doc.setName(name!=null&&!"null".equals(name)?name:null);
		doc.setIsPublic(isPublic!=null?isPublic:0);
		doc.setExtData(extData!=null&&!"null".equals(extData)?extData:null);
		doc.setDescs(descs!=null&&!"null".equals(descs)?descs:null);
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(file, "文件不能为空")
			.isTrue(!file.isEmpty(), "文件不能为空")
			.notNull(doc,new String[] {"appCode"}, "属性%s不能为空");

		// 文件层级编码
		if(doc.getDirId()!=null && doc.getDirId()!=-1L) {
			LogsUtil.add("设置文件层级编码");
			DocFile dir=dataBaseDao.findById(SqlBuilder.build(DocFile.class).id(doc.getDirId()));
			AssertUtil.service().notNull(dir, "目录不存在")
				.isTrue("dir".equals(dir.getType()), "文件夹类型不对")
				.notNull(dir, new String[] {"lvNo","lvSn"}, "目录属性%s异常");

			//目录权限验证
			if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
				DocFileDto permis=new DocFileDto();
				permis.setPermisTypes(Arrays.asList("edit"));
				permis.setId(dir.getId());
				permis.setPermisUser(sessionService.getUserId());
				if ("organ".equals(PERMIS_LEVEL)) {
					permis.setPermisOrg(sessionService.getOrgId());
				}
				if(sessionService.getOrgId()!=null) {
					permis.getPermisOwners().add(sessionService.getOrgId());
				}
				permis.getPermisOwners().add(sessionService.getUserId());
				if(sessionService.getUserRoles()!=null) {
					permis.getPermisRoles().addAll(sessionService.getUserRoles());
				}
				Long valid = dataBaseDao.count("validPermis",SqlBuilder.build(permis));
				AssertUtil.service().isTrue(valid>0, "当前用户无权限访问该目录");
			}

			doc.setLvNo(dir.getLvNo()+1);
			doc.setLvSn(codeTreeService.generate(DOCTREECODE, dir.getLvSn(), dir.getLvNo()+1));
		}	
		
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
		doc.setLvNo(-1);
		
		LogsUtil.add("保存文件记录");
		int len = dataBaseDao.insert(doc);
		
		//如果是公开文件，则创建公开文件权限记录
		if(!"tenant".equals(PERMIS_LEVEL) && doc.getIsPublic()==1) {
			DocPermis permis=new DocPermis();
			permis.setOwnerType("public");
			permis.setOwnerTitle("文件公开");
			permis.setList("view,dowland");
			docPermisService.save(doc, Arrays.asList(permis));
		}
		
		return Results.build(len>0, doc);
	}


	@Action(title="上传文件",type=ActionType.Upload)
	@Operation(summary="上传文件[单个]",description= "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
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


	@Operation(summary="上传文件[批量]-ownerId")
	@Action(title="上传文件",type=ActionType.Upload)
	@PostMapping(value="/upload/batch/{appCode}/{ownerId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Results<List<DocFile>> uploadBatch(@RequestPart("file") List<MultipartFile> files,
			@PathVariable("appCode") String appCode,
			@PathVariable("ownerId") Long ownerId,
			@RequestParam(value="dirId",required=false) String dirId,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="isPublic",required=false) Integer isPublic,
			@RequestParam(value="extData",required=false) String extData,
			@RequestParam(value="descs",required=false) String descs) {
		List<DocFile> list=new ArrayList<>();
		
		LogsUtil.add("设置参数信息");
		DocFile doc=new DocFile();
		doc.setAppCode(appCode);
		doc.setOwnerId(ownerId);
		doc.setDirId(dirId!=null&&!"null".equals(dirId)?Long.parseLong(dirId):-1L);
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

		DocFile dir=null;
		if(doc.getDirId()!=null && doc.getDirId()!=-1L) {
			LogsUtil.add("设置文件层级编码");
			dir=dataBaseDao.findById(SqlBuilder.build(DocFile.class).id(doc.getDirId()));
			AssertUtil.service().notNull(dir, "目录不存在")
				.isTrue("dir".equals(dir.getType()), "文件夹类型不对")
				.notNull(dir, new String[] {"lvNo","lvSn"}, "目录属性%s异常");
			// 目录权限验证
			if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
				DocFileDto permis=new DocFileDto();
				permis.setPermisTypes(Arrays.asList("edit"));
				permis.setId(dir.getId());
				permis.setPermisUser(sessionService.getUserId());
				if ("organ".equals(PERMIS_LEVEL)) {
					permis.setPermisOrg(sessionService.getOrgId());
				}
				if(sessionService.getOrgId()!=null) {
					permis.getPermisOwners().add(sessionService.getOrgId());
				}
				permis.getPermisOwners().add(sessionService.getUserId());
				if(sessionService.getUserRoles()!=null) {
					permis.getPermisRoles().addAll(sessionService.getUserRoles());
				}
				Long valid = dataBaseDao.count("validPermis",SqlBuilder.build(permis));
				AssertUtil.service().isTrue(valid>0, "当前用户无权限访问该目录");
			}
		}	
		
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
			dfile.setLvNo(-1);

			if(dir!=null){
				LogsUtil.add("设置文件层级编码");
				doc.setLvNo(dir.getLvNo()+1);
				doc.setLvSn(codeTreeService.generate(DOCTREECODE, dir.getLvSn(), dir.getLvNo()+1));
			}
			
			LogsUtil.add("保存文件记录");
			int len = dataBaseDao.insert(dfile);
			LogsUtil.add("保存附件id:"+dfile.getId());
			LogsUtil.add("保存附件len:"+len);
			if(len<=0) {
				dfile.setPath("error");
			}else {
				//如果是公开文件，则创建公开文件权限记录
				if(!"tenant".equals(PERMIS_LEVEL) && dfile.getIsPublic()==1) {
					DocPermis permis=new DocPermis();
					permis.setOwnerType("public");
					permis.setOwnerTitle("文件公开");
					permis.setList("view,dowland");
					docPermisService.save(dfile, Arrays.asList(permis));
				}
			}
			list.add(dfile);
		}
		
		return Results.success(list);
	}

	
	@Action(title="上传文件",type=ActionType.Upload)
	@Operation(summary="上传文件[批量]",description= "该接口上传的文档无ownerId属性，如需要则调用接口进行根据文件id设置")
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
	@Action(title="删除文件",type=ActionType.Delete)
	@Operation(summary="删除文件",description = "根据文件id删除")
	public Results<Void> delete(@PathVariable("fileId") Long fileId) {
		Results<Void> results = new Results<>();
		
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFileDto entity=new DocFileDto();
		entity.setId(fileId);
		entity.setDelFlag(0);

		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("edit"));
			entity.setPermisUser(sessionService.getUserId());
			if ("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			AssertUtil.service().isTrue(valid>0, "文件记录未找到或当前用户无操作权限");
		}
		
		LogsUtil.add("查询文档记录");
		DocFile tmp=dataBaseDao.findById(SqlBuilder.build(entity));
		AssertUtil.service().notNull(tmp, "文件记录未找到或当前用户无操作权限");
		LogsUtil.setTarget(tmp.getId(), tmp.getTitle());

		
		int len = 0;
		if(FILE_PHYSICALLY_DELETED) {
			LogsUtil.add("删除文档存储,path:"+tmp.getRealPath());
			boolean flag = AttachUtil.delete(tmp.getRealPath());
			LogsUtil.add("删除文档存储结果:%s",flag);
			
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
		docCacheService.delDocData(tmp.getId(),false);
		docCacheService.delDocData(tmp.getId(),true);

		return results.setSuccess(len>0);
	}


	@PostMapping("/delete/owner/{ownerId}")
	@Action(title="删除文件",type=ActionType.Delete)
	@Operation(summary="删除文件-ownerId",description = "根据文件归属id删除,响应数据body中的数据定义为[文件总数,成功总数]")
	public Results<Integer[]> deleteByOwner(@PathVariable("ownerId") Long ownerId) {
		LogsUtil.setTarget(ownerId, "归属Id:"+ownerId);
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(ownerId, "参数ownerId不能为空");
		
		LogsUtil.add("参数处理");
		DocFileDto entity=new DocFileDto();
		entity.setDelFlag(0);
		entity.setOwnerId(ownerId);
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("edit"));
			entity.setPermisUser(sessionService.getUserId());
			if ("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			AssertUtil.service().isTrue(valid>0, "文件记录未找到或当前用户无操作权限");
		}
		
		LogsUtil.add("查询文档记录");
		List<DocFile> list=dataBaseDao.findList(SqlBuilder.build(entity));
		AssertUtil.service().notEmpty(list, "文件记录未找到或当前用户无操作权限");
		LogsUtil.add("查询文档记录,count:"+list.size());

		
		Integer count=0;
		for(DocFile tmp:list) {
			LogsUtil.add("删除文档存储,path:"+tmp.getRealPath());
			if(FILE_PHYSICALLY_DELETED) {
				boolean flag = AttachUtil.delete(tmp.getRealPath());
				LogsUtil.add("删除文档存储结果:%s",flag);
				try {
					
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
			docCacheService.delDocData(tmp.getId(),false);
			docCacheService.delDocData(tmp.getId(),true);
		}
		
		// 逻辑删除文件信息
		List<Long> ids = list.stream().map(d->d.getId()).collect(Collectors.toList());
		int len = dataBaseDao.deleteLogicById(SqlBuilder.build(DocFile.class, ids));
		LogsUtil.add("成功删除记录数量:"+len);

		return Results.success(new Integer[] {list.size(),count});
	}


	@GetMapping("/download/{fileId}")
	@Action(title="下载文件",type=ActionType.Download)
	@Operation(summary="下载文件",description = "根据文件id下载")
	public void download(@PathVariable("fileId") Long fileId) {
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFileDto entity=new DocFileDto();
		entity.setId(fileId);
		entity.setDelFlag(0);

		// 权限验证
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("download","edit"));
			entity.setPermisUser(sessionService.getUserId());
			if ("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			if(valid==0) {
				AttachUtil.sendScriptMessage("文件记录未找到或当前用户无操作权限", response);
				return;
			}
		}

		// 加载文件记录
		DocFile tmp=dataBaseDao.findOne(SqlBuilder.build(entity));
		if(tmp==null) {
			AttachUtil.sendScriptMessage("文件记录未找到或当前用户无操作权限", response);
			return;
		}
		LogsUtil.setTarget(tmp.getId(), tmp.getTitle());
		if(ObjectUtil.isEmpty(tmp.getPath())){
			AttachUtil.sendScriptMessage("文件存储path为空", response);
			return;
		}
		
		LogsUtil.add("查询文档记录");
		DocData cache=docCacheService.getDocData(fileId,false);
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
				docCacheService.setDocData(fileId,false, DocData.build(tmp, file));
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

		if(file!=null && cache!=null) {
			// 下载文件
			AttachUtil.download(file, cache!=null?cache.getTitle():file.getName(), request, response);
		}else{
			AttachUtil.sendScriptMessage("文件下载失败", response);
		}
	}
	

	
	@PostMapping("/download")
	@Action(title="下载文件",type=ActionType.Download)
	@Operation(summary="下载文件【批量】",description = "根据文件id下载")
	public void download(@RequestBody Set<Long> ids) {
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(ids, "参数fileIds不能为空")
			.notEmpty(ids, "参数fileIds不能为空");
		LogsUtil.add("文件下载批量ID:"+ids);
		
		LogsUtil.add("参数处理");
		DocFileDto entity=new DocFileDto();
		entity.setDelFlag(0);
		entity.setIds(new ArrayList<>(ids));

		// 权限验证
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("download","edit"));
			entity.setPermisUser(sessionService.getUserId());
			if ("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			if(valid==0) {
				AttachUtil.sendScriptMessage("文件记录未找到或当前用户无操作权限", response);
				return;
			}
		}
		
		// 加载文件记录
		SqlBuilder<DocFileDto>	builder=SqlBuilder.build(entity).ids(ids);
		List<DocFileDto> docs = dataBaseDao.findList(builder);
		AssertUtil.service().notEmpty(docs, "文件记录未找到或当前用户无操作权限");
		LogsUtil.setTarget(docs.get(0).getId(), docs.get(0).getTitle());
		
		// 文件处理
		File dir=new File(System.getProperty("java.io.tmpdir"), "file.download.bt."+UUID.fastUUID().toString());
		dir.mkdir();
		Map<String, Integer> map=new HashMap<>();
		
		LogsUtil.add("下载文件：开始");
		for(DocFile doc:docs) {
			DocData cache=docCacheService.getDocData(doc.getId(),false);
			File file = cache!=null?cache.getFile():null;
			if(cache==null || file==null) {
				LogsUtil.add("下载文档,path:"+doc.getPath());
				file = AttachUtil.download(doc.getRealPath());
				if(file!=null) {
					// 设置文件缓存
					docCacheService.setDocData(doc.getId(),false, DocData.build(doc, file));
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
	}

	

	@GetMapping("/preview/{fileId}")
	@Action(title="文件预览",type = ActionType.Query)
	@Operation(summary="预览文件",description = "根据文件id下载")
	public void preview(@PathVariable("fileId") Long id) {
		LogsUtil.add("参数验证");
		AssertUtil.service().notNull(id, "参数fileId不能为空");
		
		LogsUtil.add("参数处理");
		DocFileDto entity=new DocFileDto();
		entity.setDelFlag(0);
		entity.setId(id);
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("view","download","edit"));
			entity.setPermisUser(sessionService.getUserId());
			if ("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			if(valid==0) {
				AttachUtil.sendScriptMessage("文件记录未找到或当前用户无操作权限", response);
				return;
			}
		}
		
		LogsUtil.add("查询文档记录");
		DocData cache=docCacheService.getDocData(id,false);
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
					docCacheService.setDocData(id,false, DocData.build(tmp, file));
					// 文件预览
					AttachUtil.preview(file, response);
					log.debug("退出:文件下载方法，fileId:{},path:{}",id,tmp.getPath());
				}else {
					AttachUtil.sendScriptMessage("文件未找到", response);
				}
//			}
			log.debug("退出:文件预览方法，fileId:{},path:{}",id,tmp.getPath());
		}
	}
	

	@GetMapping("/preview/public/{fileId}")
	@Action(title="文件预览",type = ActionType.Query)
	@Operation(summary="预览文件【公开】",description = "根据文件id下载，公开文件，不进行验证")
	public void previewPublic(@PathVariable("fileId") Long fileId) {
		log.debug("进入:文件预览【公开】方法，fileId:{}",fileId);
		AssertUtil.service().notNull(fileId, "参数fileId不能为空");
		
		log.debug("查询文档记录");
		DocData cache=docCacheService.getDocData(fileId,true);
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
					docCacheService.setDocData(fileId,true, DocData.build(tmp, file));
					
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
