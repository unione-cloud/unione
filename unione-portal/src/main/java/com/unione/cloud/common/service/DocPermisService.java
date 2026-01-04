package com.unione.cloud.common.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.DocFileDto;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.common.model.DocPermis;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RefreshScope
public class DocPermisService {
	
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	
	/**
	 * 文档权限级别：tenant(租户)、organ(机构)、user(用户)
	 */
	@Value("${doc.permis.level:tenant}")
	private String PERMIS_LEVEL;
	
	
	/**
	 * 	新建文件时：保存文件权限信息
	 * @param file
	 */
	public void save(DocFile file,List<DocPermis> permis) {
		LogsUtil.add("进入:保存文件权限方法,fileId:"+file.getId());
		AssertUtil.service().isTrue(file.getId()!=null, "文件id不能都为空");
		
		if(permis!=null && !permis.isEmpty()) {
			LogsUtil.add("批量保存权限数据");
			permis.stream().forEach(perm->{
				perm.setId(IdGenHolder.generate());
				perm.setDelFlag(0);
				if(file.getId()!=null) {
					// 单个文件权限：新增
					perm.setFileId(file.getId());
					perm.setFileTitle(file.getTitle());
					perm.setFileName(file.getName());
					perm.setFileType(file.getType());
					if("dir".equals(file.getType())){
						perm.setFileLvSn(file.getLvSn()+"%");
					}else{
						perm.setFileLvSn(file.getLvSn());
					}
				}
				if(perm.getAuditResult()==null || perm.getAuditResult()!=4) {
					perm.setAuditResult(1);
				}
				perm.setTenantId(sessionService.getTenantId());
				perm.setOrgId(sessionService.getOrgId());
				perm.setUserId(sessionService.getUserId());
				perm.setCreated(DateUtil.date());
				perm.setCreatedBy(sessionService.getUserId());
				perm.setLastUpdated(DateUtil.date());
				perm.setLastUpdatedBy(sessionService.getUserId());
			});
			
			int len[]= dataBaseDao.insertBatch(permis);
			LogsUtil.add("批量新增数据，len:%s",len);
		}
		
		LogsUtil.add("退出:保存文件权限方法");
	}
	
	
	/**
	 * 	修改文件权限,如果权限集合为空，则会清空现有权限
	 * @param file
	 * @param permis
	 * @return 返回新增权限记录数量
	 */
	public int update(DocFile file,List<DocPermis> permis) {
		LogsUtil.add("进入:修改文件权限方法,fileId:"+file.getId());
		int addPermisCount=0;
		DocPermis temp=new DocPermis();
		temp.setFileId(file.getId());
		temp.setDelFlag(0);
		Params<DocPermis> params=new Params<>();
		params.setBody(temp);
		Map<Long, DocPermis> map = new HashMap<>();
		List<DocPermis> list = dataBaseDao.findList(SqlBuilder.build(temp));
		list.stream().forEach(perm->{
			map.put(perm.getOwnerId(), perm);
		});
		Map<String,Integer> porder=new HashMap<>();
		porder.put("view", 1);
		porder.put("download", 2);
		porder.put("edit", 3);
		
		
		if(permis!=null && !permis.isEmpty()) {
			List<DocPermis> adds=new ArrayList<>();
			permis.stream().forEach(perm->{
				DocPermis p=map.remove(perm.getOwnerId());
				if(p!=null) {
					if(!StringUtils.equals(p.getList(), perm.getList())) {
						p.setList(perm.getList());
						if(porder.get(p.getList())!=null && porder.get(perm.getList())!=null && porder.get(p.getList())>porder.get(perm.getList())) {
							// 如果加大权限，则需要重新审核，设置审核状态：4变更
							p.setAuditResult(4);
						}
						int len=dataBaseDao.updateById(SqlBuilder.build(p).field("list","auditResult"));
						log.debug("权限记录更新结果,sid:{},len:{}",p.getId(),len);
					}
				}else {
					adds.add(perm);
				}
			});
			if(!adds.isEmpty()) {
				adds.stream().forEach(perm->{
					perm.setId(IdGenHolder.generate());
					perm.setDelFlag(0);
					perm.setAuditResult(1);
					perm.setFileId(file.getId());
					perm.setFileName(file.getName());
					perm.setFileTitle(file.getTitle());
					perm.setFileType(file.getType());
					if("dir".equals(file.getType())){
						perm.setFileLvSn(file.getLvSn()+"%");
					}else{
						perm.setFileLvSn(file.getLvSn());
					}
					if("public".equals(perm.getOwnerType())) {
						perm.setAuditType(1);
					}else{
						perm.setAuditType(2);
					}
					perm.setTenantId(sessionService.getTenantId());
					perm.setOrgId(sessionService.getOrgId());
					perm.setUserId(sessionService.getUserId());
					perm.setCreated(DateUtil.date());
					perm.setCreatedBy(sessionService.getUserId());
					perm.setLastUpdated(DateUtil.date());
					perm.setLastUpdatedBy(sessionService.getUserId());
				});
				
				int len[] = dataBaseDao.insertBatch(adds);
				LogsUtil.add("新增数据，len:%s",len);
				addPermisCount=adds.size();
			}
		}
		
		// 删除
		if(map!=null && !map.isEmpty()) {
			List<Long> ids=map.values().stream().map(p->p.getId()).collect(Collectors.toList());
			DocPermis tmp=new DocPermis();
			SqlBuilder<DocPermis> builder=SqlBuilder.build(tmp).ids(ids);
			int len=dataBaseDao.deleteLogicById(builder);
			LogsUtil.add("删除数据，len:%s",len);
		}
		
		LogsUtil.add("退出:修改文件权限方法");
		return addPermisCount;
	}
	

	/**
	 * 	删除文件权限
	 * @param fileIds
	 */
	public void delete(List<Long> fileIds) {
		LogsUtil.add("进入:删除文件权限方法,fileIds:"+fileIds);
		AssertUtil.service().notNull(fileIds, "文件ID不能为空").notEmpty(fileIds, "文件ID不能为空");
		
		DocPermis permis=new DocPermis();
		SqlBuilder<DocPermis> builder=SqlBuilder.build(permis).ids(fileIds);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			permis.setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					permis.setOrgId(sessionService.getOrgId());
				}
			} else if ("user".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					permis.setUserId(sessionService.getUserId());
				}
			}			
		}
		
		int len=dataBaseDao.deleteLogicById(builder);
		LogsUtil.add("成功更新数据,len:%s",len);
		
	}
	
	/**
	 * 	批量加载用户文件权限信息
	 * @param files
	 * @return
	 */
	public List<DocPermis> loadFilePermis(List<DocFileDto> files){
		LogsUtil.add("进入:批量加载用户文件权限信息方法");
		
		List<Long> ids = files.stream().map(f->f.getId()).collect(Collectors.toList());
		List<Long> permisOwners=new ArrayList<Long>();
		permisOwners.add(sessionService.getUserId());
		if(sessionService.getOrgId()!=null) {
			permisOwners.add(sessionService.getOrgId());
		}
		SqlBuilder<DocPermis> builder=SqlBuilder.build(DocPermis.class)
				.where("delFlag=0 and auditResult in [auditResults] and (ownerId in [permisOwners] or ownerName in [ownerNames]) and fileId in [ids]")
				.where("auditResults", Arrays.asList(2,4))
				.where("permisOwners", permisOwners)
				.where("ownerNames", sessionService.getUserRoles())
				.where("ids", ids);
		List<DocPermis> permisList=dataBaseDao.findList(builder);

		LogsUtil.add("分发权限信息到文档对象中");
		Map<Long, DocFileDto> fMap=new HashMap<>();
		
		DocPermis view=new DocPermis();
		view.setList("view");
		view.setAuditResult(2);
		files.stream().forEach(f->{
			f.getPermis().add(view);	// 添加view权限
			fMap.put(f.getId(), f);
			if(sessionService.getUserId()==f.getUserId()) {
				f.setOwner(true);
			}
		});
		permisList.stream().forEach(p->{
			DocFileDto file=fMap.get(p.getFileId());
			if(file!=null) {
				file.getPermis().add(p);
			}
		});
		
		LogsUtil.add("退出:批量加载用户文件权限信息方法");
		return permisList;
	}
	
}
