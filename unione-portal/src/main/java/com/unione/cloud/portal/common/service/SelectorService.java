package com.unione.cloud.portal.common.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.portal.common.dto.SelectorGroupDto;
import com.unione.cloud.portal.common.dto.SelectorGroupParam;
import com.unione.cloud.portal.common.dto.SelectorOrganDto;
import com.unione.cloud.portal.common.dto.SelectorOrganParam;
import com.unione.cloud.portal.common.dto.SelectorPostDto;
import com.unione.cloud.portal.common.dto.SelectorPostParam;
import com.unione.cloud.portal.common.dto.SelectorRoleDto;
import com.unione.cloud.portal.common.dto.SelectorRoleParam;
import com.unione.cloud.portal.common.dto.SelectorUserDto;
import com.unione.cloud.portal.common.dto.SelectorUserParam;
import com.unione.cloud.portal.common.dto.TreeNodeDto;
import com.unione.cloud.portal.system.model.SysGroup;
import com.unione.cloud.portal.system.model.SysOrgan;
import com.unione.cloud.portal.system.model.SysPost;
import com.unione.cloud.portal.system.model.SysUserRole;

import lombok.extern.slf4j.Slf4j;

/**
 * @标题 公共服务：选择器组件服务
 * @作者 Unione Cloud
 * @日期 2024-11-22 23:32:02
 * @版本 1.0
 */
@Slf4j
@Service
public class SelectorService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionService;
   
    /**
     * 查询用户节点
     * @param params
     * @return
     */
    public Results<List<SelectorUserDto>> userNode(Params<SelectorUserParam> params){
        log.debug("进入：查询用户节点方法,target type:{},target id:{},keyword:{}",params.getBody().getNtype(),params.getBody(),params.getKeywords());
        AssertUtil.service()
            .notIn(params.getBody().getNtype(), Arrays.asList("organ","role","group","post"), "参数type有效值：organ-机构，role-角色，post-岗位,group-分组")
            .notNull(params.getBody().getPid(), "参数pid不能为空");
        
        // 查询用户列表
        Params<SelectorUserDto> query=Params.build(SelectorUserDto.class)
            .setPage(params.getPage())
            .setPageSize(params.getPageSize())
            .setKeywords(params.getKeywords());
            query.getBody().setPid(params.getBody().getPid());
        Results<List<SelectorUserDto>> results = dataBaseDao.findPages(String.format("find%sUserList", params.getBody().getNtype()),String.format("count%sUser", params.getBody().getNtype()),query);

        // 验证用户是否已经在目标中
        if(Objects.nonNull(params.getBody().getTargetType()) && Objects.nonNull(params.getBody().getTargetId())){
            List<Long> uids = results.getBody().stream().map(SelectorUserDto::getId).collect(Collectors.toList());
            if(uids.size()>0){
                params.getBody().setIds(uids);
                Map<String,SelectorUserDto> hadMap = dataBaseDao.findMap(String.format("check%sUser", params.getBody().getTargetType()),params.getBody(),SelectorUserDto.class,"id");
                results.getBody().forEach(user->{
                    if(hadMap.containsKey(user.getId().toString())){
                        user.setChecked(true);
                    }
                });
            }
        }
        results.getBody().forEach(user->{
            user.setNtype("user");
        });

        log.debug("退出：查询用户节点方法,target type:{},target id:{},keyword:{},result:{}",params.getBody().getNtype(),params.getBody(),params.getKeywords(),results.isSuccess());
        return results;
    }


     /**
     * 查询角色节点
     * @param type
     * @param params
     * @return
     */
    public Results<List<SelectorRoleDto>> roleNode(Params<SelectorRoleParam> params){
        log.debug("进入：查询角色节点方法,type:{},keyword:{}",params.getBody().getRtype(),params.getKeywords());
        
        SqlBuilder<SelectorRoleDto> builder=SqlBuilder.build(SelectorRoleDto.class);
        builder.params("tenantId", sessionService.getTenantId());
        builder.params("orgId", sessionService.getOrgId());
        builder.params("keywords", params.getKeywords());
        builder.page(params.getPage()).pageSize(params.getPageSize());
        if(params.getBody().getRtype()!=null && params.getBody().getRtype()>0){
            builder.params("types", params.getBody().getRtype());
        }
        Results<List<SelectorRoleDto>> results=dataBaseDao.findPages("selectRolePage4Use","countRole4Use",builder);
       
        // 验证角色是否已经在目标中
        if(Objects.nonNull(params.getBody().getTargetType()) && Objects.nonNull(params.getBody().getTargetId())){
            List<Long> uids = results.getBody().stream().map(SelectorRoleDto::getId).collect(Collectors.toList());
            if(uids.size()>0){
                params.getBody().setIds(uids);
                Map<String,SelectorRoleDto> hadMap = dataBaseDao.findMap(String.format("check%sRole", params.getBody().getTargetType()),params.getBody(),SelectorRoleDto.class,"id");
                results.getBody().forEach(user->{
                    if(hadMap.containsKey(user.getId().toString())){
                        user.setChecked(true);
                    }
                });
            }
        }
        results.getBody().forEach(role->{
            role.setNtype("role");
        });

        log.debug("退出：查询角色节点方法,type:{},keyword:{}",params.getBody().getRtype(),params.getKeywords());
        return results;
    }


    
    /**
     * 查询角色列表
     * @param type      类型type=permis授权,use:角色使用
     * @param params
     * @return
     */
    public Results<List<SelectorRoleDto>> roleList(Params<SelectorRoleParam> params){
        log.debug("进入：查询角色列表方法,type:{},target id:{}",params.getBody().getTargetType(),params.getBody().getTargetId());
        
        SqlBuilder<SelectorRoleDto> builder=SqlBuilder.build(SelectorRoleDto.class);
        builder.params("tenantId", sessionService.getTenantId());
        builder.params("orgId", sessionService.getOrgId());

        String sqlName="selectRole4Use";
        if(Objects.equals(params.getBody().getTargetType(), "permis") || Objects.equals(params.getBody().getTargetType(), "assign")){
            sqlName="selectRole4Auth";
            if(sessionService.hasRole(UserRoles.TENANT_ADMIN)){
                builder.params("isTenantAdmin", true);
            }else if(sessionService.hasRole(UserRoles.ORGAN_ADMIN)){
                builder.params("isOrganAdmin", true);
            }
        }
       
        // 执行数据查询
        List<SelectorRoleDto> rows=dataBaseDao.findList(sqlName,builder);
        rows.forEach(role->{
            role.setNtype("role");
        });

        // 角色授权模式下，加载当前用户有角色列表
        if(Objects.equals(params.getBody().getTargetType(), "permis") && !Objects.isNull(params.getBody().getTargetId())){
            // 加载当前用户有角色列表
            SqlBuilder<SysUserRole> query=SqlBuilder.build(SysUserRole.class);
            query.params("userId", params.getBody().getTargetId());
            Map<Long,SysUserRole> uRoles=dataBaseDao.findList(query).stream()
                .collect(Collectors.toMap(SysUserRole::getRoleId, role->role));
            rows.forEach(role->{
                SysUserRole uRole=uRoles.get(role.getId());
                if(uRole!=null){
                    role.setChecked(true);
                    role.setEnDilivery(uRole.getEnDilivery());
                }
            });
        }
        
        log.debug("退出：查询角色列表方法,type:{},target id:{},len:{}",params.getBody().getTargetType(),params.getBody().getTargetId(),rows.size());
        return Results.success(rows).setTotal(rows.size());
    }
    

    /**
     * 查询机构树
     * @param params
     * @return
     */
    public Results<List<SelectorOrganDto>> organTree(Params<SelectorOrganParam> params){
        log.debug("进入：查询机构树方法,type:{},parentId:{},keyword:{}",params.getBody().getOtype(),params.getBody(),params.getKeywords());
        List<SelectorOrganDto> list=new ArrayList<>();
        
        Params<SysOrgan> queryOrgan = Params.build(SysOrgan.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setParentId(params.getBody().getPid());
        queryOrgan.getBody().setName(params.getKeywords());
        if(params.getBody().getOtype()!=null && params.getBody().getOtype()>0){
            queryOrgan.getBody().setTypes(params.getBody().getOtype());
        }
        Results<List<SysOrgan>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
            .field("name","id","parentId","sn","types","descs")
            .where("status=1 and parentId=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

         // 验证机构是否已经在目标中
         Map<String,SelectorOrganDto> hadMap = null;
         if(Objects.nonNull(params.getBody().getTargetType()) && Objects.nonNull(params.getBody().getTargetId())){
            List<Long> uids = results.getBody().stream().map(SysOrgan::getId).collect(Collectors.toList());
            if(uids.size()>0){
                params.getBody().setIds(uids);
                hadMap = dataBaseDao.findMap(String.format("check%sOrgan", params.getBody().getTargetType()),params.getBody(),SelectorOrganDto.class,"id");
            }
        }

        if(results.isSuccess()){
            for(SysOrgan organ:results.getBody()){
                SelectorOrganDto dto=new SelectorOrganDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setSn(organ.getSn());
                dto.setOtype(organ.getTypes());
                dto.setDescs(organ.getDescs());
                dto.setNtype("organ");
                if(hadMap!=null && hadMap.containsKey(organ.getId().toString())){
                    dto.setChecked(true);
                }
                list.add(dto);
            }
        }

        log.debug("退出：查询机构树方法,type:{},parentId:{},keyword:{}",params.getBody().getOtype(),params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }

   

    /**
     * 查询分组树
     * @param params
     * @return
     */
    public Results<List<SelectorGroupDto>> groupTree(Params<SelectorGroupParam> params){
        log.debug("进入：查询分组树方法,type:{},parentId:{},keyword:{}",params.getBody().getGtype(),params.getBody(),params.getKeywords());
        List<SelectorGroupDto> list=new ArrayList<>();
        
        Params<SysGroup> queryGroup = Params.build(SysGroup.class);
        queryGroup.setPage(params.getPage());
        queryGroup.setPageSize(params.getPageSize());
        queryGroup.getBody().setParentId(params.getBody().getPid());
        queryGroup.getBody().setName(params.getKeywords());
        if(params.getBody().getGtype()!=null && params.getBody().getGtype()>0){
            queryGroup.getBody().setTypes(params.getBody().getGtype());
        }
        Results<List<SysGroup>> results=dataBaseDao.findPages(SqlBuilder.build(queryGroup)
            .field("name","id","parentId")
            .where("status=1 and parentId=? and types=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

         // 验证分组是否已经在目标中
         Map<String,SelectorGroupDto> hadMap = null;
         if(Objects.nonNull(params.getBody().getTargetType()) && Objects.nonNull(params.getBody().getTargetId())){
            List<Long> uids = results.getBody().stream().map(SysGroup::getId).collect(Collectors.toList());
            if(uids.size()>0){
                params.getBody().setIds(uids);
                hadMap = dataBaseDao.findMap(String.format("check%sGroup", params.getBody().getTargetType()),params.getBody(),SelectorGroupDto.class,"id");
            }
        }    
        if(results.isSuccess()){
            for(SysGroup organ:results.getBody()){
                SelectorGroupDto dto=new SelectorGroupDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setNtype("group");
                if(hadMap!=null && hadMap.containsKey(organ.getId().toString())){
                    dto.setChecked(true); 
                }
                list.add(dto);
            }
        }

        log.debug("退出：查询分组树方法,type:{},parentId:{},keyword:{}",params.getBody().getGtype(),params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }


    /**
     * 查询岗位树
     * @param type
     * @param params
     * @return
     */
    public Results<List<SelectorPostDto>> postTree(Params<SelectorPostParam> params){
        log.debug("进入：查询岗位树方法,type:{},parentId:{},keyword:{}",params.getBody().getPtype(),params.getBody(),params.getKeywords());
        List<SelectorPostDto> list=new ArrayList<>();
        
        Params<SysPost> queryPost = Params.build(SysPost.class);
        queryPost.setPage(params.getPage());
        queryPost.setPageSize(params.getPageSize());
        queryPost.getBody().setParentId(params.getBody().getPid());
        queryPost.getBody().setName(params.getKeywords());
        if(params.getBody().getPtype()!=null && params.getBody().getPtype()>0){
            queryPost.getBody().setTypes(params.getBody().getPtype());
        }
        Results<List<SysPost>> results=dataBaseDao.findPages(SqlBuilder.build(queryPost)
            .field("name","id","parentId")
            .where("status=1 and parentId=? and types=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

        // 验证分组是否已经在目标中
        Map<String,SelectorPostDto> hadMap = null;
         if(Objects.nonNull(params.getBody().getTargetType()) && Objects.nonNull(params.getBody().getTargetId())){
            List<Long> uids = results.getBody().stream().map(SysPost::getId).collect(Collectors.toList());
            if(uids.size()>0){
                params.getBody().setIds(uids);
                hadMap = dataBaseDao.findMap(String.format("check%sPost", params.getBody().getTargetType()),params.getBody(),SelectorPostDto.class,"id");
            }
        }    

        if(results.isSuccess()){
            for(SysPost organ:results.getBody()){
                SelectorPostDto dto=new SelectorPostDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setNtype("organ");
                if(hadMap!=null && hadMap.containsKey(organ.getId().toString())){
                    dto.setChecked(true); 
                }
                list.add(dto);
            }
        }

        log.debug("退出：查询岗位树方法,type:{},parentId:{},keyword:{}",params.getBody().getPtype(),params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }
}
