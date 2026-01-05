package com.unione.cloud.system.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.DocFileDto;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.common.model.DocPermis;
import com.unione.cloud.common.service.DocPermisService;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.util.AttachUtil;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 DocPermis Controller 服务
 * 
 * @作者	Jeking Yang
 * @日期	2021-9-27 11:37:16
 * @版本	1.0.0
 **/
@Slf4j
@RefreshScope
@RestController
@Tag(name = "文档权限 管理服务")
@RequestMapping("/api/system/doc/permis")
public class DocPermisController {
	
	@Autowired
	private DocPermisService docPermisSErvice;
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	/**
	 * 文档权限级别：tenant(租户)、organ(机构)、user(用户)
	 */
	@Value("${doc.permis.level:tenant}")
	private String PERMIS_LEVEL;
	
	
	@Operation(summary = "加载指定文档权限集合")
	@PostMapping({"/load"})
	public Results<List<DocPermis>> load(@RequestBody Long fileId) {
		log.debug("进入控制:加载文档权限列表方法,fileId:{}",fileId);
		// 参数处理
		AssertUtil.service().notNull(fileId,"参数fileId不能为空");
		
		DocPermis params=new DocPermis();
		params.setFileId(fileId);
		params.setDelFlag(0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					params.setOrgId(sessionService.getOrgId());
				}
			} else if ("user".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					params.setUserId(sessionService.getUserId());
				}
			}
		}
		
		// 执行查询
		List<DocPermis> rows = dataBaseDao.findList(SqlBuilder.build(params));
		return Results.success(rows);
	}

	
	@Operation(summary = "新增")
	@PostMapping({"/save"})
	public Results<Void> save(@RequestBody DocFileDto entity) {

		AssertUtil.service().notNull(entity.getId(), "参数id不能为空")
			.notEmpty(entity.getPermis(), "参数permis不能为空");

		// 权限验证
		DocFile tmp=new DocFile();
		tmp.setId(entity.getId());
		tmp.setDelFlag(0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			tmp.setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					tmp.setOrgId(sessionService.getOrgId());
				}
			} else if ("user".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					tmp.setUserId(sessionService.getUserId());
				}
			}
		}
		tmp=dataBaseDao.findById(SqlBuilder.build(tmp));
		AssertUtil.service().notNull(tmp, "文档记录未找到或当前用户无操作权限");
		
		docPermisSErvice.save(entity, entity.getPermis());
		
		return Results.success();
	}


	@Operation(summary = "修改")
	@PostMapping({"/update"})
	public Results<Void> update(@RequestBody DocFileDto entity) {
		AssertUtil.service().notNull(entity,new String[] {"id","isPublic","title"}, "参数%s不能为空");
		
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					entity.setOrgId(sessionService.getOrgId());
				}
			} else if ("user".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					entity.setUserId(sessionService.getUserId());
				}
			}
		}
		
		DocFile tmp=dataBaseDao.findById(SqlBuilder.build(entity));
		AssertUtil.service().notNull(tmp, "文档记录未找到或当前用户无操作权限");
		
		//如果公开属性发送变化，则迁移文档路径
		if(!ObjectUtil.equal(entity.getIsPublic(),tmp.getIsPublic()) && !"dir".equals(tmp.getType())) {
			LogsUtil.add("文档公开属性发生变化，改成："+entity.getIsPublic());
			String remote=tmp.getRealPath();
			File tFile = AttachUtil.download(remote);
			AssertUtil.service().notNull(tFile, "文档文件获取失败");
			tmp.setIsPublic(entity.getIsPublic());
			boolean flag=AttachUtil.save2path(tFile, tmp.getRealPath());
			boolean flagDel = AttachUtil.delete(remote);
			AssertUtil.service()
				.isTrue(flag, "文档迁移到"+(entity.getIsPublic()==1?"公开失败":"非公开失败"));
			LogsUtil.add("文档源文件删除结果:"+flagDel);
		}
		
		LogsUtil.add("保存文档权限信息");
		String fields[] = {"title","isPublic"};
		int addPermisCount = docPermisSErvice.update(tmp, entity.getPermis());
		if(addPermisCount>0) {
			//字典DOCFILEAUDITSTS 1待审，2通过，3拒绝
			entity.setAuditStatus(1);
			entity.setIsShare(tmp.getIsShare());
			fields=new String[] {"title","isPublic","isShare","auditStatus"};
		}
		
		if(addPermisCount<=0 && entity.getIsPublic()==0 && ObjectUtil.isEmpty(entity.getPermis())) {
			//字典DOCFILEAUDITSTS 1待审，2通过，3拒绝
			entity.setAuditStatus(null);
			entity.setIsShare(tmp.getIsShare());
			fields=new String[] {"title","isPublic","isShare","auditStatus"};
		}
		
		int len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
		return Results.build(len>0);
	}


	@PostMapping({"/delete"})
	@Operation(summary = "删除", description = "批量删除,body中传递的是文件ID，清空选中文件的所有权限")
	public Results<Void> delete(@RequestBody List<Long> params){
		docPermisSErvice.delete(params);
		return Results.success();
	}


	@Operation(summary = "查询审核列表")
	@PostMapping({"/toAudit"})
	public Results<List<DocPermis>> toAudit(@RequestBody Params<DocPermis> params){
		
		params.getBody().setDelFlag(0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					params.getBody().setOrgId(sessionService.getOrgId());
				}
			}
		}
		
		Results<List<DocPermis>> results = dataBaseDao.findPages(SqlBuilder.build(params));

		return results;
	}


	@PostMapping({"/doAudit"})
	@Operation(summary = "提交审核结果", description = "单个审核id不能为空，批量审核ids不能为空")
	public Results<Void> doAudit(@RequestBody Params<DocPermis> params){
		// 参数处理
		AssertUtil.service().notNull(params.getBody(), new String[] {"auditResult","auditOpinion"},"参数%s不能为空");
		List<Long> ids=new ArrayList<>();
		if(params.getId()!=null) {
			ids.add(params.getId());
		}
		if(params.getBody().getId()!=null){
			ids.add(params.getBody().getId());
		}
		if(params.getIds()!=null) {
			ids.addAll(params.getIds());
		}
		params.setId(null);
		params.setIds(ids);
		AssertUtil.service().notEmpty(ids, "参数id和ids不能都为空");
		
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
			if("organ".equals(PERMIS_LEVEL) && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				params.getBody().setOrgId(sessionService.getOrgId());
			}
		}
		
		LogsUtil.add("保存权限审核结果,ids:"+ids);
		params.getBody().setAuditTime(DateUtil.date());
		params.getBody().setAuditUserid(sessionService.getUserId());
		params.getBody().setAuditUsername(sessionService.getRealname());
		int len = dataBaseDao.updateById(SqlBuilder.build(params).field("auditResult,auditOpinion,auditTime,auditUserid,auditUsername"));
		LogsUtil.add("保存权限审核结果,len:"+len);
		
		if(len>0) {
			List<DocPermis> permis=dataBaseDao.findByIds(SqlBuilder.build(DocPermis.class).ids(ids));
			Set<Long> fileIds=permis.stream().map(p->p.getFileId()).collect(Collectors.toSet());
			if(!fileIds.isEmpty()){
				permis=dataBaseDao.findList(SqlBuilder.build(DocPermis.class).where("delFlag=0 and fileId in [fileIds]").where("fileIds",fileIds));
				if(permis!=null && !permis.isEmpty()) {
					LogsUtil.add("刷新文档审核结果");
					Map<Long, Integer> totalMap=new HashMap<>();		//所有权限记录数量，doc->count
					Map<Long, Integer> unAuditMap=new HashMap<>();		//未审核权限记录数，doc->unAudit
					Map<Long, Integer> passMap=new HashMap<>();			//审核通过权限记录数，	doc->pass
	
					LogsUtil.add("统计文档审核情况");
					permis.stream().forEach(perm->{
						Integer total=totalMap.getOrDefault(perm.getFileId(),0);
						Integer unAudit=unAuditMap.getOrDefault(perm.getFileId(),0);
						Integer pass=passMap.getOrDefault(perm.getFileId(),0);
						
						totalMap.put(perm.getFileId(), total+1);
						unAuditMap.put(perm.getFileId(), perm.getAuditResult()==1?(unAudit+1):unAudit);
						passMap.put(perm.getFileId(), perm.getAuditResult()==2?(pass+1):pass);
					});
					
					LogsUtil.add("更新文档审核结果");
					totalMap.keySet().stream().forEach(fileId->{
						Integer total=totalMap.getOrDefault(fileId,0);
						Integer unAudit=unAuditMap.getOrDefault(fileId,0);
						Integer pass=passMap.getOrDefault(fileId,0);
						if(total>0) {
							DocFile docFile=null;
							if(unAudit==0 && pass>0) {
								// 所有审核记录都处理完，且有审核通过的记录，则修改文档审核结果为审核通过
								LogsUtil.add("所有审核记录都处理完，且有审核通过的记录，设置文档状态为：审核通过,file id:"+fileId);
								docFile=new DocFile();
								docFile.setId(fileId);
								docFile.setAuditStatus(2);
							}else if(unAudit==0 && pass==0) {
								// 所有审核记录都处理完，没有审核通过的记录，则修改文档审核结果为审核不通过
								LogsUtil.add("所有审核记录都处理完，且有审核通过的记录，设置文档状态为：审核不通过,file id:"+fileId);
								docFile=new DocFile();
								docFile.setId(fileId);
								docFile.setAuditStatus(3);
							}
							if(docFile!=null) {
								int l=dataBaseDao.updateById(SqlBuilder.build(docFile).field("auditStatus"));
								LogsUtil.add("更新文档审核结果,file id:"+fileId+",len:"+l);
							}else {
								LogsUtil.add("文档权限记录未审核完成,file id:"+fileId+",未处理记录:"+unAudit);
							}
						}else {
							LogsUtil.add("文档权限数异常,file id:"+fileId);
						}
					});
				}
			}
			
		}
		
		return Results.build(len>0);
	}


}
