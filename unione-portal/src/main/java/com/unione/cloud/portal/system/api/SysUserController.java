//package com.unione.cloud.portal.system.api;
//
//import java.util.List;
//
//import org.beetl.sql.core.SQLManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.unione.cloud.beetsql.Updater;
//import com.unione.cloud.core.dto.Params;
//import com.unione.cloud.core.dto.Results;
//import com.unione.cloud.core.exception.AssertUtil;
//import com.unione.cloud.core.feign.PojoFeignApi;
//import com.unione.cloud.core.model.Validator;
//import com.unione.cloud.core.security.SessionService;
//import com.unione.cloud.core.security.UserRoles;
//import com.unione.cloud.portal.system.model.SysUser;
//import com.unione.cloud.util.LogsUtil;
//import com.unione.cloud.util.LogsUtil.LogType;
//
//import cn.hutool.json.JSONUtil;
//import io.swagger.annotations.Api;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * @标题 SysUser Controller 服务
// * 
// * @作者	Jeking Yang
// * @日期	2023-8-31 0:00:34
// * @版本	1.0.0
// **/
//@Slf4j
//@RestController
//@Api(tags = "系统管理：用户信息 管理服务",description="SysUser")
//@RequestMapping("/api/user")
//public class SysUserController implements PojoFeignApi<SysUser>{
//	
//	@Autowired
//	private SQLManager sqlManager;
//
//	/**
//	 * 用户会话对象
//	 */
//	@Autowired
//	private SessionService sessionService;
//
//	
//	@Override
//	public Results<List<SysUser>> find(Params<SysUser> params) {
//		log.debug("进入控制:查询SYS_USER列表方法,params:{}",params);
//		Results<List<SysUser>> results = new Results<>();
//		LogsUtil.set(LogType.Query, "查询SYS_USER列表");
//		// 参数处理
////			AssertUtil.service().notNull(params.getBody(), new String[] {"appId"},"参数%s不能为空");
//		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
//			params.getBody().setTenantId(sessionService.getTenantId());
//			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
//				params.getBody().setOrgId(sessionService.getOrgId());
//			}
//		}
//		
//		
//		results=sysUserDao.findListByPage(params);
//		LogsUtil.add("分页数据查询，数据总量count:"+results.getTotal());
//		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
//		
//		LogsUtil.success();
//		log.debug("退出控制:查询SYS_USER列表方法,params:{},result:{}",params,results.isSuccess());
//		return results;
//	}
//
//
//	@Override
//	public Results<Long> save(@Validated(Validator.save.class) SysUser entity) {
//		log.debug("进入控制:新增SYS_USER信息.entity:{}",entity);
//		LogsUtil.set(LogType.Insert, "新增SYS_USER");
//		// 参数处理
//		//AssertUtil.service().notNull(entity, new String[] {"appId","name","title"},"参数%s不能为空");
//		
//		sysUserDao.insert(entity);
//		
//		LogsUtil.success(entity.getSid(),entity.getTitle());
//		log.debug("退出控制:新增SYS_USER信息.entity:{},result:true",entity);
//		return Results.success(entity.getSid());
//	}
//
//
//	@Override
//	public Results<Long> update(@Validated(Validator.update.class) SysUser entity) {
//		log.debug("进入控制:修改SYS_USER信息方法，entity:{}",entity);
//		Results<Long> results = new Results<>();
//		LogsUtil.set(LogType.Modify, "修改SYS_USER",entity.getSid());
//		// 参数处理
//		//AssertUtil.service().notNull(entity, new String[] {"sid","appId","name","title"},"参数%s不能为空");
//		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
//			entity.setTenantId(sessionService.getTenantId());
//			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
//				entity.setOrgId(sessionService.getOrgId());
//			}
//		}
//		
//		String[] fields = {"userType","username","pwdText","pwdSalt","realName","aliasName","portrait","birthday","sex","email","qq","tel","securityQuestion","sucurityMfa","lastLoginTime","lastLoginIp","status","lockTime","descs"};
//		
//		int len = sysUserDao.updateById(Updater.build(entity).fields(fields));
//		LogsUtil.add("保存数据,len:"+len);
//		
//		results.setBody(entity.getSid());
//		results.setSuccess(len>0);
//		results.setMessage(len>0?"操作成功":"操作失败");
//		LogsUtil.save(len>0, entity.getSid(), entity.getTitle());
//
//		log.debug("退出控制:修改SYS_USER信息方法，entity:{},result:{}",entity,results.isSuccess());
//		return results;
//	}
//
//
//
//	@Override
//	public Results<List<SysUser>> findByIds(Params<List<Long>> params) {
//		log.debug("进入控制:批量查询SYS_USER信息方法，params:{}",params);
//		LogsUtil.set(LogType.Query, "批量查询SYS_USER");
//		// 参数处理
//		AssertUtil.service().isTrue(params.getBody()!=null && !params.getBody().isEmpty(), "参数body不能为空");
//		
//		// 参数处理
//		SysUser entity=new SysUser();
//		entity.setIds(params.getBody());
//		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
//			entity.setTenantId(sessionService.getTenantId());
//			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
//				entity.setOrgId(sessionService.getOrgId());
//			}
//		}
//		
//		List<SysUser> rows= sysUserDao.findListByIds(entity);
//		LogsUtil.add("批量查询数据:"+rows.size());
//		
//		LogsUtil.success();
//		log.debug("退出控制:批量查询SYS_USER信息方法，params:{},result:true",params);
//		return Results.success(rows);
//	}
//
//
//	@Override
//	public Results<SysUser> detail(Long sid) {
//		log.debug("进入控制:查看SYS_USER详细信息方法，sid:{}",sid);
//		LogsUtil.set(LogType.Query, "查看SYS_USER详细",sid);
//		// 参数处理
//		AssertUtil.service().notNull(sid,"参数sid不能为空");
//		
//		// 参数处理
//		SysUser entity=new SysUser();
//		entity.setSid(sid);
//		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
//			entity.setTenantId(sessionService.getTenantId());
//			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
//				entity.setOrgId(sessionService.getOrgId());
//			}
//		}
//		
//		LogsUtil.add("查找记录");
//		SysUser tmp = sysUserDao.findById(entity);
//		AssertUtil.service().notNull(tmp, "记录未找到");
//		
//		LogsUtil.success(tmp.getSid(), tmp.getTitle());
//		log.debug("退出控制:查看SYS_USER详细信息方法，sid:{},result:true",sid);
//		return Results.success(tmp);
//	}
//	
//
//	@Override
//	public Results<Long> delete(Params<List<Long>> params){
//		log.debug("进入控制:删除SYS_USER信息方法，params:{}",params);
//		Results<Long> results = new Results<>();
//		LogsUtil.set(LogType.Delete, "删除SYS_USER");
//		
//		// 参数处理
//		AssertUtil.service().isTrue(params.getBody()!=null && !params.getBody().isEmpty(), "参数body不能为空");
//		
//		// 参数处理
//		SysUser entity=new SysUser();
//		entity.setIds(params.getBody());
//		if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value())) {
//			entity.setTenantId(sessionService.getTenantId());
//			if(!sessionService.getUserRoles().contains(UserRoles.TENANT_ADMIN.value())) {
//				entity.setOrgId(sessionService.getOrgId());
//			}
//		}
//		
//		// 执行删除
//		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(entity.getIds()));
//		int count = sysUserDao.deleteLogicById(entity);
//		LogsUtil.add("成功删除记录数量:"+count);
//		
//		results.setSuccess(count>0);
//		results.setMessage(count>0?"操作成功":"操作失败");
//		results.setBody((long)count);
//		LogsUtil.save(count>0);
//
//		log.debug("退出控制:删除SYS_USER信息方法，params:{},result:{}",params,results.isSuccess());
//		return results;
//	}
//
//
//
//}
