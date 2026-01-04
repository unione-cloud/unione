package com.unione.cloud.common.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.DocFileDto;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.common.service.DocFileService;
import com.unione.cloud.common.service.DocPermisService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.service.CodeTreeService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "doc管理：文件信息 管理服务")
@RequestMapping("/api/common/file")
public class DocFileController implements DocFileService{

	/**
	 * 数据访问对象
	 */
	@Autowired
	private DataBaseDao dataBaseDao;

	/**
	 * 用户会话对象
	 */
	@Autowired
	private SessionService sessionService;
	
	
	@Autowired
	private DocPermisService docPermisService;

	@Autowired
	private CodeTreeService codeTreeService;

	/**
	 * 文档权限级别：tenant(租户)、organ(机构)、user(用户)
	 */
	@Value("${doc.permis.level:tenant}")
	private String PERMIS_LEVEL;
	
	private String DOCTREECODE="DOCTREE";
	
	
	@Override
	@Action(title="查询文件列表",type = ActionType.Query)
	@Operation(summary="查询文件列表",description="文档综合查询接口")
	public Results<List<DocFile>> find(Params<DocFile> params) {
		// 参数处理
		Params<DocFileDto> query=new Params<>();
		BeanUtil.copyProperties(params, query, "body");
		BeanUtil.copyProperties(params.getBody(), query.getBody());

		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			query.getBody().setPermisEnable(true);
			query.getBody().setPermisTypes(Arrays.asList("view","download","edit"));
			query.getBody().setPermisUser(sessionService.getUserId());
			if("organ".equals(PERMIS_LEVEL)) {
				query.getBody().setPermisOrg(sessionService.getOrgId());
			}
			query.getBody().getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				query.getBody().getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				query.getBody().getPermisRoles().addAll(sessionService.getUserRoles());
			}
		}
		query.getBody().setDelFlag(0);
		
		Results<List<DocFileDto>> result = dataBaseDao.findPages("findDocList","countDocList",SqlBuilder.build(query));
		return Results.success(result.getBody().stream().map(item->BeanUtil.copyProperties(item, DocFile.class)).collect(Collectors.toList()));
	}
	

	
	@PostMapping({"/find/mine"})
	@Action(title="查询我的文件",type = ActionType.Query)
	@Operation(summary="查询我的文件",description= "只查询自己上传的文件")
	public Results<List<DocFile>> findMine(@RequestBody Params<DocFileDto> params) {
		// 参数处理
		params.getBody().setDelFlag(0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
			if ("organ".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					params.getBody().setOrgId(sessionService.getOrgId());
				}
			} else if ("user".equals(PERMIS_LEVEL)) {
				if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
					params.getBody().setUserId(sessionService.getUserId());
				}
			}
		}
		
		Results<List<DocFileDto>> result = dataBaseDao.findPages(SqlBuilder.build(params)
		.where("delFlag=0 and isPublic=? and isShare=? and auditStatus=? and dirId=? and (userId=? or orgId=?) and lvSn like [lvSn%] and title like [%title%] and type in [incTypes] and type not in [ninTypes]"));
		
		return Results.success(result.getBody().stream().map(item->BeanUtil.copyProperties(item, DocFile.class)).collect(Collectors.toList()));
	}
	
	
	@PostMapping("/find/mineShare")
	@Operation(summary="查询我共享的文件",description="包括：我共享")
	@Action(title="查询我共享的文件",type = ActionType.Query)
	public Results<List<DocFile>> findMineShare(@RequestBody Params<DocFile> params) {
		// 参数处理
		params.getBody().setIsShare(1);
		params.getBody().setDelFlag(0);
		params.getBody().setAuditStatus(2);//1待审，2通过，3拒绝
		params.getBody().setTenantId(sessionService.getTenantId());
		if ("organ".equals(PERMIS_LEVEL)) {
			if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				params.getBody().setOrgId(sessionService.getOrgId());
			}
		} else if ("user".equals(PERMIS_LEVEL)) {
			if (!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				params.getBody().setUserId(sessionService.getUserId());
			}
		}
		
		Results<List<DocFile>> result = dataBaseDao.findPages(SqlBuilder.build(params));
		
		return result;
	}

	 
	@PostMapping("/find/shareMine")
	@Operation(summary="查询共享给我的文件",description="")
	@Action(title="查询共享给我的文件",type = ActionType.Query)
	public Results<List<DocFileDto>> findShareMine(@RequestBody Params<DocFileDto> params) {
		// 参数处理
		params.getBody().setDelFlag(0);
		params.getBody().setTenantId(sessionService.getTenantId());
		params.getBody().setPermisTypes(Arrays.asList("view","download","edit"));
		params.getBody().setPermisUser(sessionService.getUserId());
		params.getBody().getPermisOwners().add(sessionService.getUserId());
		if ("organ".equals(PERMIS_LEVEL)) {
			params.getBody().setPermisOrg(sessionService.getOrgId());
		}
		params.getBody().getPermisOwners().add(sessionService.getOrgId());
		if(sessionService.getUserRoles()!=null) {
			params.getBody().getPermisRoles().addAll(sessionService.getUserRoles());
		}
		
		Results<List<DocFileDto>> result = dataBaseDao.findPages("findShareMine","countShareMine",SqlBuilder.build(params));
		
		// 加载文档权限集合
		if(!ObjectUtil.isEmpty(result.getBody())) {
			docPermisService.loadFilePermis(result.getBody());
		}
		
		return result;
	}

	
	@PostMapping("/find/other")
	@Action(title="查询公开文件列表",type = ActionType.Query)
	@Operation(summary="查询公开文件列表",description= "查询他人的公开的文件，查询自己租户下其他人上传的公开文件")
	public Results<List<DocFile>> findOther(@RequestBody Params<DocFile> params) {
		// 参数处理
		params.getBody().setTenantId(sessionService.getTenantId());
		params.getBody().setUserId(sessionService.getUserId());
		if ("organ".equals(PERMIS_LEVEL)) {
			params.getBody().setOrgId(sessionService.getOrgId());
		}
		
		Results<List<DocFile>> result = dataBaseDao.findPages(SqlBuilder.build(params)
			.where("delFlag=0 and isPublic=1 and AUDIT_STATUS=2 and tenantId=? and userId!=? and orgId!=? and lvSn like [lvSn%] and title like [%title%]"));
		
		return result;
	}

	
	@Override
	@Action(title="保存文件信息",type=ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) DocFileDto entity) {
		
		// 参数处理
		AssertUtil.service().notNull(entity, new String[] {"title"},"参数%s不能为空");
		int len = 0;
		if(entity.getPermis()!=null && !entity.getPermis().isEmpty() || 
				entity.getIsPublic()!=null&&entity.getIsPublic()==1) {
			entity.setAuditStatus(1);
		}
		
		if(entity.getId()!=null&&entity.getId()>0){
			// 权限验证
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
			
			String fields[] = {"title","name","ordered","extData","descs","fileMeta","fileData"};
			len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
		}else {
			entity.setDelFlag(0);
			entity.setStatus(1);
			entity.setIsShare(0);
			entity.setLvNo(-1);
			BeanUtils.setDefaultValue(entity, "dirId", -1L);
			if(entity.getDirId()!=null&&entity.getDirId()>0) {
				DocFile dir=dataBaseDao.findById(SqlBuilder.build(DocFile.class).id(entity.getDirId()));
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

				entity.setLvNo(dir.getLvNo()+1);
				entity.setLvSn(codeTreeService.generate(DOCTREECODE, dir.getLvSn(), dir.getLvNo()+1));
			}else if("dir".equals(entity.getType()) && ObjectUtil.equals(entity.getDirId(), -1L)) {
				entity.setLvNo(0);
				entity.setLvSn(codeTreeService.generate(DOCTREECODE));
			}

			len = dataBaseDao.insert(entity);

		}
		
		// 保存文档权限
		if(!"tenant".equals(PERMIS_LEVEL) && entity.getPermis()!=null && !entity.getPermis().isEmpty()) {
			docPermisService.save(entity, entity.getPermis());
		}
		
		return Results.build(len>0, entity.getId());
	}


	@Override
	public Results<List<DocFile>> findByIds(Set<Long> ids) {
		log.debug("进入控制:批量查询文档文件信息方法，ids:{}",ids);
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");
		
		// 参数处理
		DocFileDto entity=new DocFileDto();
		entity.setDelFlag(0);
		entity.setIds(new ArrayList<>(ids));

		// 权限验证
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisEnable(true);
			entity.setPermisTypes(Arrays.asList("view","download","edit"));
			entity.setPermisUser(sessionService.getUserId());
			if("organ".equals(PERMIS_LEVEL)) {
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
		
		SqlBuilder<DocFileDto> builder=SqlBuilder.build(entity).ids(new ArrayList<>(ids));
		List<DocFileDto> rows= dataBaseDao.findList(builder);
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows.stream().map(item->BeanUtil.copyProperties(item, DocFile.class)).collect(Collectors.toList()));
	}


	@Override
	public Results<DocFile> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		// 参数处理
		DocFileDto entity=new DocFileDto();
		entity.setId(id);
		entity.setDelFlag(0);

		// 权限验证
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisEnable(true);
			entity.setPermisTypes(Arrays.asList("view","download","edit"));
			entity.setPermisUser(sessionService.getUserId());
			if("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			Long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			AssertUtil.service().isTrue(valid>0, "文件记录未找到或当前用户无操作权限");
		}
		
		LogsUtil.add("查找记录");
		DocFileDto tmp = dataBaseDao.findById(SqlBuilder.build(entity));
		AssertUtil.service().notNull(tmp, "记录未找到");
					
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除文件",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");	
		
		// 参数处理
		DocFileDto entity=new DocFileDto();
		entity.setIds(new ArrayList<>(ids));
		// 权限验证
		if(!"tenant".equals(PERMIS_LEVEL) && !sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setPermisTypes(Arrays.asList("edit"));
			entity.setPermisUser(sessionService.getUserId());
			if("organ".equals(PERMIS_LEVEL)) {
				entity.setPermisOrg(sessionService.getOrgId());
			}
			entity.getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				entity.getPermisOwners().add(sessionService.getOrgId());
			}
			if(sessionService.getUserRoles()!=null) {
				entity.getPermisRoles().addAll(sessionService.getUserRoles());
			}
			Long valid = dataBaseDao.count("validPermis",SqlBuilder.build(entity));
			AssertUtil.service().isTrue(valid>0, "当前用户无权限访问该目录");
		}
		
		// 执行删除
		LogsUtil.add("删除数ids:%s",JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteLogicById(SqlBuilder.build(entity).ids(ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		return Results.build(count>0, count);
	}


	@PostMapping({"/set/owner/{ownerId}"})
	@Action(title="设置文件归属",type = ActionType.Save)
	@Operation(summary="设置文件归属",description = "参数body中未文件id集合，返回body为成功数")
	public Results<Integer> setOwner(@RequestBody Set<Long> ids,@PathVariable("ownerId") Long ownerId){
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");
		
		// 参数处理
		DocFile entity=new DocFile();
		entity.setDelFlag(0);
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
		
		entity.setOwnerId(ownerId);
		entity.setLastUpdated(DateUtil.date());
		entity.setLastUpdatedBy(sessionService.getUserId());
		
		SqlBuilder<DocFile>	builder=SqlBuilder.build(entity).ids(new ArrayList<>(ids));	
		Integer len = dataBaseDao.updateById(builder);
		LogsUtil.add("设置文件归属,len:"+len);

		return Results.build(len>0, len);
	}

}
