package com.unione.cloud.portal.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Updater;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.portal.common.model.DocFile;
import com.unione.cloud.portal.common.service.DocPermisService;
import com.unione.cloud.portal.system.model.SysApiInfo;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
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
@Tag(name = "doc管理：文件信息 管理服务")
@RequestMapping("/api/file")
public class DocFileController implements PojoFeignApi<DocFile>{
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
	
	
	@Override
	@ApiOperation(value="文档查询",description="文档综合查询接口")
	public Results<List<DocFile>> find(Params<DocFile> params) {
		log.debug("进入控制:查询文档文件列表方法,params:{}",params);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "查询文档文件列表");
			logSaveFlag=true;
		}
		// 参数处理
		params.getBody().setPermisEnable(FILE_PERMIS_ENABLE);
		if(FILE_PERMIS_ENABLE) {
			params.getBody().setPermisUser(sessionService.getUserId());
			if(ORG_PERMIS) {
				params.getBody().setPermisOrg(sessionService.getOrgId());
			}
			params.getBody().getPermisOwners().add(sessionService.getUserId());
			if(sessionService.getOrgId()!=null) {
				params.getBody().getPermisOwners().add(sessionService.getOrgId());
			}
//			if(sessionService.getUserRoles()!=null) {
//				params.getBody().getPermisOwners().addAll(sessionService.getUserRoles());
//			}
		}
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
			params.getBody().setDelFlag(0);
			if(ORG_PERMIS && FILE_PERMIS_ENABLE==false && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				params.getBody().setOrgId(sessionService.getOrgId());
			}
		}
		
		Results<List<DocFile>> result = dataBaseDao.findPages(SqlBuilder.build(params));
		
		// 加载文档权限集合
		if(FILE_PERMIS_ENABLE && !ObjectUtils.isEmpty(result.getBody()) && 
				params.getBody().getUserId()==null) {
			docPermisService.loadFilePermis(result.getBody());
		}
		
		if(logSaveFlag) {
			// 保存日志
			LogsUtil.success();
		}

		log.debug("退出控制:查询文档文件列表方法,params:{},result:{}",params,result.isSuccess());
		return result;
	}
	
	
	@PostMapping({"/find/mine"})
	@ApiOperation(value="我的文件",description= "只查询自己上传的文件")
	public Results<List<DocFile>> findMine(@RequestBody Params<DocFile> params) {
		log.debug("进入控制:我的文件列表方法,params:{}",params);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "我的文档");
			logSaveFlag=true;
		}
		
		// 参数处理
		params.getBody().setUserId(sessionService.getUserId());
		params.getBody().setDelFlag(0);
		
		
		Results<List<DocFile>> result = dataBaseDao.findPages(params);
		
		if(logSaveFlag) {
			LogsUtil.success();
		}
		log.debug("退出控制:我的文件列表方法,params:{}",params);
		return result;
	}
	
	
	@PostMapping("/find/mineShare")
	@ApiOperation(value="我共享的文件",description="")
	public Results<List<DocFile>> findMineShare(@RequestBody Params<DocFile> params) {
		log.debug("进入控制:查询我的共享文档方法,params:{}",params);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "我的共享文档");
			logSaveFlag=true;
		}
		
		// 参数处理
		params.getBody().setUserId(sessionService.getUserId());
		params.getBody().setShared(true);
		params.getBody().setDelFlag(0);
		params.getBody().setAuditStatus(2);//1待审，2通过，3拒绝
		
		Results<List<DocFile>> result = dataBaseDao.findPages(params);
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		log.debug("退出控制:查询我的共享文档方法");
		return result;
	}

	 
	@PostMapping("/find/shareMine")
	@ApiOperation(value="共享给我的文件",description="")
	public Results<List<DocFile>> findShareMine(@RequestBody Params<DocFile> params) {
		log.debug("进入控制:查询共享给我的文档方法,params:{}",params);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "共享给我的文档");
			logSaveFlag=true;
		}
		// 参数处理
		params.getBody().setUserId(null);
		params.getBody().setUnUserId(sessionService.getUserId());
		params.getBody().setDelFlag(0);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
		}
		params.getBody().setPermisEnable(true);
		params.getBody().setPermisUser(sessionService.getUserId());
		if(ORG_PERMIS) {
			params.getBody().setPermisOrg(sessionService.getOrgId());
		}
		params.getBody().getPermisOwners().add(sessionService.getUserId());
		if(sessionService.getOrgId()!=null) {
			params.getBody().getPermisOwners().add(sessionService.getOrgId());
		}
		if(sessionService.getUserRoles()!=null) {
			//params.getBody().getPermisOwners().addAll(sessionService.getUserRoles());
		}
		
		Results<List<DocFile>> result = dataBaseDao.findPages(params);
		
		// 加载文档权限集合
		if(!ObjectUtils.isEmpty(result.getBody())) {
			docPermisService.loadFilePermis(result.getBody());
		}
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		log.debug("退出控制:查询共享给我的文档方法");
		return result;
	}

	
	@PostMapping({"/find/other"})
	@ApiOperation(value="他人的公开文件",description= "查询他人的公开的文件，查询自己租户下其他人上传的公开文件")
	public Results<List<DocFile>> findOther(@RequestBody Params<DocFile> params) {
		log.debug("进入控制:他人的公开文件列表方法,params:{}",params);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "他人的公开文件列表");
			logSaveFlag=true;
		}
		
		// 参数处理
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			params.getBody().setTenantId(sessionService.getTenantId());
		}
		params.getBody().setUnUserId(sessionService.getUserId());
		params.getBody().setIsPublic(1);
		params.getBody().setDelFlag(0);
		
		Results<List<DocFile>> result = dataBaseDao.findPages(params);
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		log.debug("退出控制:他人的公开文件列表方法,params:{}",params);
		return result;
	}


	@Override
	public Results<Long> save(@Validated(Validator.save.class) DocFile entity) {
		log.debug("进入控制:新增文档文件信息.entity:{}",entity);
		Results<Long> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Insert, "新增文档文件");
			logSaveFlag=true;
		}
		
		// 参数处理
		//AssertUtil.service().notNull(entity, new String[] {"appId","name","title"},"参数%s不能为空");
		int len = 0;
		if(entity.getPermis()!=null && !entity.getPermis().isEmpty() || 
				entity.getIsPublic()!=null&&entity.getIsPublic()==1) {
			entity.setAuditStatus(1);
		}
		
		if(entity.getId()!=null&&entity.getId()>0){
			len = dataBaseDao.insertWithId(entity);
		}else {
			LogsUtil.add("设置默认属性");
			entity.setDelFlag(0);
			entity.setStatus(1);
			len = dataBaseDao.insert(entity);
		}
		LogsUtil.add("保存数据,len:"+len);
		
		// 保存文档权限
		if(FILE_PERMIS_ENABLE && entity.getPermis()!=null && !entity.getPermis().isEmpty()) {
			docPermisService.save(entity, entity.getPermis());
		}
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.save(len>0, entity.getId());
		}

		log.debug("退出控制:新增文档文件信息.entity:{},result:{}",entity,results.isSuccess());
		return results;
	}


	@Override
	public Results<Long> update(@Validated(Validator.update.class) DocFile entity) {
		log.debug("进入控制:修改文档文件信息方法，entity:{}",entity);
		Results<Long> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Update, "修改文档文件",entity.getId());
			logSaveFlag=true;
		}
		
		// 参数处理
		//AssertUtil.service().notNull(entity, new String[] {"sid","appId","name","title"},"参数%s不能为空");
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		String fields[] = {"dirId","ownerId","appCode","title","name","type","size","ordered","extData","descs","fileMeta","fileData"};
		
		entity.setLastUpdated(DateUtil.date());
		entity.setLastUpdatedBy(sessionService.getUserId());
		int len = dataBaseDao.update(Updater.build(entity).fields(fields));
		LogsUtil.add("保存数据,len:"+len);
		
		results.setBody(entity.getId());
		results.setSuccess(len>0);
		results.setMessage(len>0?"操作成功":"操作失败");
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.save(len>0, entity.getId());
		}

		log.debug("退出控制:修改文档文件信息方法，entity:{},result:{}",entity,results.isSuccess());
		return results;
	}



	@Override
	public Results<List<DocFile>> findByIds(Set<Long> ids) {
		log.debug("进入控制:批量查询文档文件信息方法，ids:{}",ids);
		Results<List<DocFile>> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "批量查询文档文件");
			logSaveFlag=true;
		}
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");
		
		// 参数处理
		DocFile entity=new DocFile();
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			entity.setDelFlag(0);
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		SqlBuilder<DocFile> builder=SqlBuilder.build(entity).ids(new ArrayList<>(ids));
		List<DocFile> rows= dataBaseDao.findList(builder);
		LogsUtil.add("批量查询数据:"+rows.size());
		
		results.setBody(rows);
		results.setSuccess(true);
		results.setMessage("操作成功");
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}

		log.debug("退出控制:批量查询文档文件信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


	@Override
	public Results<DocFile> detail(Long sid) {
		log.debug("进入控制:查看文档文件详细信息方法，sid:{}",sid);
		Results<DocFile> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Query, "查看文档文件详细",sid);
			logSaveFlag=true;
		}
		// 参数处理
		AssertUtil.service().notNull(sid,"参数sid不能为空");
		
		// 参数处理
		DocFile entity=new DocFile();
		entity.setId(sid);
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			entity.setDelFlag(0);
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		LogsUtil.add("查找记录");
		DocFile tmp = dataBaseDao.findById(entity);
		AssertUtil.service().notNull(tmp, "记录未找到");
					
		results.setBody(tmp);
		results.setSuccess(true);
		results.setMessage("操作成功");
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success(tmp.getId());
		}

		log.debug("退出控制:查看文档文件详细信息方法，sid:{},result:{}",sid,results.isSuccess());
		return results;
	}
	

	@Override
	public Results<Integer> delete(Set<Long> ids){
		log.debug("进入控制:删除文档文件信息方法，ids:{}",ids);
		Results<Integer> results = new Results<>();
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Delete, "删除文档文件");
			logSaveFlag=true;
		}
		
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");	
		
		// 参数处理
		DocFile entity=new DocFile();
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		// 执行删除
		LogsUtil.add("删除数ids:%s",JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(SysApiInfo.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);
		
		if(logSaveFlag) {
			//保存日志
			LogsUtil.save(count>0);
		}

		log.debug("退出控制:删除文档文件信息方法，ids:{},result:{}",ids,results.isSuccess());
		return results;
	}


	@PostMapping({"/set/owner/{ownerId}"})
	@ApiOperation(value="设置文件归属",description= "参数body中未文件id集合，返回body为成功数")
	public Results<Integer> setOwner(@RequestBody Set<Long> ids,@PathVariable("ownerId") Long ownerId){
		log.debug("进入控制:设置文件归属方法，ids:{},ownerId:{}",ids,ownerId);
		boolean logSaveFlag=false;
		if(LogsUtil.getEntry().getTypes()==null) {
			LogsUtil.set(LogType.Update, "设置文件归属");
			logSaveFlag=true;
		}
		// 参数处理
		AssertUtil.service().notEmpty(ids, "参数不能为空");
		
		// 参数处理
		DocFile entity=new DocFile();
		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.code())) {
			entity.setTenantId(sessionService.getTenantId());
			entity.setDelFlag(0);
			if(ORG_PERMIS && !sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.code())) {
				entity.setOrgId(sessionService.getOrgId());
			}
		}
		
		entity.setOwnerId(ownerId);
		entity.setLastUpdated(DateUtil.date());
		entity.setLastUpdatedBy(sessionService.getUserId());
		
		SqlBuilder<DocFile>	builder=SqlBuilder.build(entity).ids(new ArrayList<>(ids));	
		Integer len = dataBaseDao.updateById(builder);
		LogsUtil.add("设置文件归属,len:"+len);

		log.debug("退出控制:设置文件归属方法，ids:{},ownerId:{}",ids,ownerId);
		if(logSaveFlag) {
			//保存日志
			LogsUtil.success();
		}
		return Results.build(len>0, len);
	}


}
