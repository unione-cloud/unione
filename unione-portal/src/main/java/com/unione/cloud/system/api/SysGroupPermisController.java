package com.unione.cloud.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.feign.PojoFeignApi;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.dto.GroupPermisDto;
import com.unione.cloud.system.model.SysGroup;
import com.unione.cloud.system.model.SysGroupPermis;
import com.unione.cloud.system.model.SysResource;
import com.unione.cloud.system.model.SysRole;
import com.unione.cloud.system.model.SysRolePermis;
import com.unione.cloud.system.model.SysUserPermis;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	GroupPermisDto Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "系统管理：分组权限",description="GroupPermisDto")
@RequestMapping("/api/system/groupPermis")	 //TreeFeignApi
public class SysGroupPermisController implements PojoFeignApi<GroupPermisDto>{
	
	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;
	
	
	@Override
	@Action(title="查询分组权限",type = ActionType.Query)
	public Results<List<GroupPermisDto>> find(Params<GroupPermisDto> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<GroupPermisDto>> results = dataBaseDao.findPages(params);
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		
		return results;
	}


	@Override
	@Action(title="保存分组权限",type = ActionType.Save,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Long> save(@Validated(Validator.save.class) GroupPermisDto entity) {
		Results<Long> results=Results.success();
		AtomicInteger addCount = new AtomicInteger(0);
		AtomicInteger editCount = new AtomicInteger(0);
		AtomicInteger delCount = new AtomicInteger(0);
		if(entity.getId()==null) {
			// 参数处理
			if(entity.getGroupId()!=null && entity.getResId()!=null){
				// 单个添加：
				AssertUtil.service().notNull(entity.getAppId(), "资源id不能为空");
				AssertUtil.service().notNull(entity.getResType(), "资源类型不能为空");
				BeanUtils.setDefaultValue(entity, "enDilivery",0);
				
				SysGroup group=dataBaseDao.findById(SqlBuilder.build(SysGroup.class,entity.getGroupId()));
				AssertUtil.service().notNull(group, "分组不存在");
				SysResource res=dataBaseDao.findById(SqlBuilder.build(SysResource.class,entity.getResId()));
				AssertUtil.service().notNull(res, "资源不存在");
				
				int len = dataBaseDao.insert(entity);
				addCount.addAndGet(len);
			}else{
				// 批量添加：
				if(entity.getGroupId()!=null){
					// 权限分配，批量添加用户权限
					SysGroup target=dataBaseDao.findById(SqlBuilder.build(SysGroup.class,entity.getGroupId()));
					AssertUtil.service().notNull(target, "分组不存在");
					LogsUtil.setTarget(target.getId(), target.getName());

					// 批量添加：
					if(entity.getAddPermis()!=null){
						// 加载当前分组已有权限列表
						Set<Long> rids = entity.getAddPermis().stream().map(row->row.getResId()).collect(Collectors.toSet());
						Set<Long> hdMap=new HashSet<>();
						if(rids.size()>0) {
							dataBaseDao.findList(SqlBuilder.build(SysGroupPermis.class)
								.where("groupId=? and resId in [resIds]")
								.params("groupId", entity.getGroupId())
								.params("resIds", rids))
								.stream().forEach(row->{
									hdMap.add(row.getResId());
								});
						}
						// 过滤已存在的权限
						List<SysGroupPermis> adds = entity.getAddPermis().stream().filter(row->!hdMap.contains(row.getResId())).map(row->{
							BeanUtils.setDefaultValue(row, "enDilivery",0);
							row.setGroupId(entity.getGroupId());
							return row;
						}).collect(Collectors.toList());
						LogsUtil.add("新增分组权限，数量：%s,可新增:%s",entity.getAddPermis().size(),adds.size());
						if(adds.size()>0) {
							int lens[] = dataBaseDao.insertBatch(adds);
							int len = Arrays.stream(lens).sum();
							addCount.addAndGet(len);
						}
					}
					// 批量修改：
					if(entity.getEditPermis()!=null){
						entity.getEditPermis().stream()
							.filter(row->ObjectUtil.equal(row.getUserId(), entity.getUserId()))
							.filter(row->row.getId()!=null)
							.forEach(row->{
							BeanUtils.setDefaultValue(row, "enDilivery",0);
							if(!sessionService.isAdmin() && !sessionService.hasRole(UserRoles.SUPPER_ADMIN)) {
								row.setTenantId(sessionService.getTenantId());
								if(!sessionService.hasRole(UserRoles.TENANT_ADMIN) && 
									!sessionService.hasRole(UserRoles.SYSOPS_USER)) {
									row.setOrgId(sessionService.getOrgId());
								}
							}
							int len = dataBaseDao.updateById(SqlBuilder.build(row).field("enDilivery"));
							editCount.addAndGet(len);
						});
					}
					// 批量删除：
					if(entity.getDelPermis()!=null){
						SysRolePermis rps=new SysRolePermis();
						if(!sessionService.isAdmin() && !sessionService.hasRole(UserRoles.SUPPER_ADMIN)) {
							rps.setTenantId(sessionService.getTenantId());
							if(!sessionService.hasRole(UserRoles.TENANT_ADMIN) && 
								!sessionService.hasRole(UserRoles.SYSOPS_USER)) {
									rps.setOrgId(sessionService.getOrgId());
							}
						}
						List<SysRolePermis> list = dataBaseDao.findByIds(SqlBuilder.build(rps).ids(entity.getDelPermis()));
						List<Long> dels = list.stream()
							.filter(row->ObjectUtil.equal(row.getUserId(), entity.getUserId()))
							.map(SysRolePermis::getId).collect(Collectors.toList());
						LogsUtil.add("删除分组权限，数量：%s,可删除:%s",entity.getDelPermis().size(),dels.size());
						if(dels.size()>0) {
							int len = dataBaseDao.deleteById(SqlBuilder.build(SysRolePermis.class, dels));
							delCount.addAndGet(len);
						}
					}
				}else{
					// 资源分配，批量添加分组：
					SysResource target=dataBaseDao.findById(SqlBuilder.build(SysResource.class,entity.getResId()));
					AssertUtil.service().notNull(target, "资源不存在");
					LogsUtil.setTarget(target.getId(), target.getTitle());

					// 批量添加：
					if(entity.getAddPermis()!=null){
						// 加载当前资源已有分组列表
						Set<Long> uids = entity.getAddPermis().stream().map(row->row.getGroupId()).collect(Collectors.toSet());
						Set<Long> hdMap=new HashSet<>();
						if(uids.size()>0) {
							dataBaseDao.findList(SqlBuilder.build(SysGroupPermis.class)
								.where("resId=? and groupId in [groupIds]")
								.params("resId", entity.getResId())
								.params("groupId", uids))
								.stream().forEach(row->{
									hdMap.add(row.getUserId());
								});
						}
						// 过滤已存在的权限
						List<SysGroupPermis> adds = entity.getAddPermis().stream().filter(row->!hdMap.contains(row.getGroupId())).map(row->{
							BeanUtils.setDefaultValue(row, "enDilivery",0);
							row.setResId(entity.getResId());
							row.setResType(entity.getResType());
							row.setAppId(entity.getAppId());
							return row;
						}).collect(Collectors.toList());
						LogsUtil.add("新增分组权限，数量：%s,可新增:%s",entity.getAddPermis().size(),adds.size());
						if(adds.size()>0) {
							int lens[] = dataBaseDao.insertBatch(adds);
							int len = Arrays.stream(lens).sum();
							addCount.addAndGet(len);
						}
					}
					// 批量修改：
					if(entity.getEditPermis()!=null){
						entity.getEditPermis().stream()
							.filter(row->ObjectUtil.equal(row.getResId(), entity.getResId()))
							.filter(row->row.getId()!=null)
							.forEach(row->{
							BeanUtils.setDefaultValue(row, "enDilivery",0);
							if(!sessionService.isAdmin() && !sessionService.hasRole(UserRoles.SUPPER_ADMIN)) {
								row.setTenantId(sessionService.getTenantId());
								if(!sessionService.hasRole(UserRoles.TENANT_ADMIN) && 
									!sessionService.hasRole(UserRoles.SYSOPS_USER)) {
									row.setOrgId(sessionService.getOrgId());
								}
							}
							int len = dataBaseDao.updateById(SqlBuilder.build(row).field("enDilivery"));
							editCount.addAndGet(len);
						});
					}
					// 批量删除：
					if(entity.getDelPermis()!=null){
						SysRolePermis rps=new SysRolePermis();
						if(!sessionService.isAdmin() && !sessionService.hasRole(UserRoles.SUPPER_ADMIN)) {
							rps.setTenantId(sessionService.getTenantId());
							if(!sessionService.hasRole(UserRoles.TENANT_ADMIN) && 
								!sessionService.hasRole(UserRoles.SYSOPS_USER)) {
									rps.setOrgId(sessionService.getOrgId());
							}
						}
						List<SysRolePermis> list = dataBaseDao.findByIds(SqlBuilder.build(rps).ids(entity.getDelPermis()));
						List<Long> dels = list.stream()
							.filter(row->ObjectUtil.equal(row.getResId(), entity.getResId()))
							.map(SysRolePermis::getId).collect(Collectors.toList());
						LogsUtil.add("删除分组权限，数量：%s,可删除:%s",entity.getDelPermis().size(),dels.size());
						if(dels.size()>0) {
							int len = dataBaseDao.deleteById(SqlBuilder.build(SysRolePermis.class, dels));
							delCount.addAndGet(len);
						}
					}
				}
			}
		}else {
			// 权限单独修改
			String[] fields = {"enDilivery"};
			int len = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
			editCount.addAndGet(len);
		}
		
		String message=String.format("新增:%s,修改:%s,删除:%s",addCount.get(),editCount.get(),delCount.get());
		results.setMessage(message);
		LogsUtil.add(message);
		return results;
	}



	@Override
	public Results<List<GroupPermisDto>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<GroupPermisDto> rows = dataBaseDao.findByIds(SqlBuilder.build(GroupPermisDto.class,new ArrayList<>(ids)));
		LogsUtil.add("批量查询数据:"+rows.size());
		
		return Results.success(rows);
	}


	@Override
	public Results<GroupPermisDto> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		GroupPermisDto tmp = dataBaseDao.findById(SqlBuilder.build(GroupPermisDto.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除分组权限",type = ActionType.Delete,roles = {UserRoles.ORGANADMIN,UserRoles.SYS3PAUTH})
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		int count = dataBaseDao.deleteById(SqlBuilder.build(GroupPermisDto.class,ids));
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
