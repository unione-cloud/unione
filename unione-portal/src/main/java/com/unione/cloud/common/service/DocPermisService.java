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
import com.unione.cloud.beetsql.Updater;
import com.unione.cloud.beetsql.builder.SqlBuilder;
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
	 * 	数据权限：机构权限开关，默认开启
	 */
	@Value("${data.permis.org.enable:true}")
	private boolean ORG_PERMIS;
	
	
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
		List<DocPermis> list = dataBaseDao.findList(temp);
		list.stream().forEach(perm->{
			map.put(perm.getOwnerId(), perm);
		});
		
		if(permis!=null && !permis.isEmpty()) {
			List<DocPermis> adds=new ArrayList<>();
			permis.stream().forEach(perm->{
				DocPermis p=map.remove(perm.getOwnerId());
				if(p!=null) {
					if(!StringUtils.equals(p.getList(), perm.getList())) {
						// 更新
						if(p.getList().indexOf("download")<0 && perm.getList().indexOf("download")>=0) {
							// 如果是新添加下载权限，则需要重新审核，设置审核状态：4变更
							p.setDelFlag(1);
							perm.setAuditResult(4);
							adds.add(perm);
						}else {
							p.setList(perm.getList());
						}
						p.setLastUpdated(DateUtil.date());
						p.setLastUpdatedBy(sessionService.getUserId());
						int len=dataBaseDao.updateById(Updater.build(p).fields("list","delFlag"));
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
					if(perm.getAuditResult()==null || perm.getAuditResult()!=4) {
						perm.setAuditResult(1);
					}
					perm.setFileId(file.getId());
					perm.setFileTitle(file.getTitle());
					perm.setFileName(file.getName());
					perm.setFileType(file.getType());
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
			tmp.setLastUpdated(DateUtil.date());
			tmp.setLastUpdatedBy(sessionService.getUserId());
			SqlBuilder<DocPermis> builder=SqlBuilder.build(tmp).ids(ids);
			int len=dataBaseDao.deleteLogicById(builder);
			LogsUtil.add("删除数据，len:"+len);
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
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				permis.setOrgId(sessionService.getOrgId());
			}
		}
		
		int len=dataBaseDao.update(builder);
		LogsUtil.add("成功更新数据,len:"+len);
		
		LogsUtil.add("退出:删除文件权限方法");
	}
	
	/**
	 * 	批量加载用户文件权限信息
	 * @param files
	 * @return
	 */
	public List<DocPermis> loadFilePermis(List<DocFile> files){
		LogsUtil.add("进入:批量加载用户文件权限信息方法");
		
		List<Long> ids = files.stream().map(f->f.getId()).collect(Collectors.toList());
		DocPermis permis=new DocPermis();
		Map<String, Object> params=new HashMap<String, Object>();
		params.put("delFlag", 0);
		params.put("inResults", Arrays.asList(2,4));
		params.put("permisUser", sessionService.getUserId());
		List<Long> permisOwners=new ArrayList<Long>();
		params.put("permisOwners", permisOwners);
		permisOwners.add(sessionService.getUserId());
		if(sessionService.getOrgId()!=null) {
			permisOwners.add(sessionService.getOrgId());
		}
		if(sessionService.getUserRoles()!=null) {
			//permisOwners.addAll(sessionService.getUserRoles());
		}
		SqlBuilder<DocPermis> builder=SqlBuilder.build(DocPermis.class, params)
				.ids(ids)
				.where("delFlag=? and auditResult in [] and permisUser=? and ownerId in []");
		List<DocPermis> permisList=dataBaseDao.findList(builder);
		
		LogsUtil.add("分发权限信息到文档对象中");
		Map<Long, DocFile> fMap=new HashMap<>();
		
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
			DocFile file=fMap.get(p.getFileId());
			if(file!=null) {
				file.getPermis().add(p);
			}
		});
		
		LogsUtil.add("退出:批量加载用户文件权限信息方法");
		return permisList;
	}
	
}
