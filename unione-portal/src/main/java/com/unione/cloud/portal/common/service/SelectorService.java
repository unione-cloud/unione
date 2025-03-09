package com.unione.cloud.portal.common.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.unione.cloud.portal.common.dto.SelectorNodeDto;
import com.unione.cloud.portal.common.dto.SelectorUserDto;
import com.unione.cloud.portal.common.dto.SelectorUserParam;
import com.unione.cloud.portal.system.model.SysGroup;
import com.unione.cloud.portal.system.model.SysOrgan;
import com.unione.cloud.portal.system.model.SysPost;
import com.unione.cloud.portal.system.model.SysRole;

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
    public Results<List<SelectorNodeDto>> roleNode(Integer type,Params<Void> params){
        log.debug("进入：查询角色节点方法,type:{},keyword:{}",type,params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysRole> queryOrgan = Params.build(SysRole.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setName(params.getKeywords());
        if(type!=null && type>0){
            queryOrgan.getBody().setTypes(type);
        }
        Results<List<SysRole>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
            .field("name","id")
            .where("status=1 and types=? and name like [%?%]"));

        if(results.isSuccess()){
            for(SysRole organ:results.getBody()){
                SelectorNodeDto dto=new SelectorNodeDto();
                dto.setId(organ.getId());
                dto.setTitle(organ.getName());
                dto.setNtype("role");
                list.add(dto);
            }
        }

        log.debug("退出：查询角色节点方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }
    

    /**
     * 查询机构树
     * @param params
     * @return
     */
    public Results<List<SelectorNodeDto>> organTree(Integer type,Params<Long> params){
        log.debug("进入：查询机构树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysOrgan> queryOrgan = Params.build(SysOrgan.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setParentId(params.getBody());
        queryOrgan.getBody().setName(params.getKeywords());
        if(type!=null && type>0){
            queryOrgan.getBody().setTypes(type);
        }
        Results<List<SysOrgan>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
            .field("name","id","parentId")
            .where("status=1 and parentId=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

        if(results.isSuccess()){
            for(SysOrgan organ:results.getBody()){
                SelectorNodeDto dto=new SelectorNodeDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setNtype("organ");
                list.add(dto);
            }
        }

        log.debug("退出：查询机构树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
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
    public Results<List<SelectorNodeDto>> groupTree(Integer type, Params<Long> params){
        log.debug("进入：查询分组树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysGroup> queryGroup = Params.build(SysGroup.class);
        queryGroup.setPage(params.getPage());
        queryGroup.setPageSize(params.getPageSize());
        queryGroup.getBody().setParentId(params.getBody());
        queryGroup.getBody().setName(params.getKeywords());
        if(type!=null && type>0){
            queryGroup.getBody().setTypes(type);
        }
        Results<List<SysGroup>> results=dataBaseDao.findPages(SqlBuilder.build(queryGroup)
            .field("name","id","parentId")
            .where("status=1 and parentId=? and types=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

        if(results.isSuccess()){
            for(SysGroup organ:results.getBody()){
                SelectorNodeDto dto=new SelectorNodeDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setNtype("organ");
                list.add(dto);
            }
        }

        log.debug("退出：查询分组树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
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
    public Results<List<SelectorNodeDto>> postTree(Integer type, Params<Long> params){
        log.debug("进入：查询岗位树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysPost> queryPost = Params.build(SysPost.class);
        queryPost.setPage(params.getPage());
        queryPost.setPageSize(params.getPageSize());
        queryPost.getBody().setParentId(params.getBody());
        queryPost.getBody().setName(params.getKeywords());
        queryPost.getBody().setTypes(type);
        Results<List<SysPost>> results=dataBaseDao.findPages(SqlBuilder.build(queryPost)
            .field("name","id","parentId")
            .where("status=1 and parentId=? and types=? and name like [%?%]")
            .sort(Sort.build("ordered", "desc")));

        if(results.isSuccess()){
            for(SysPost organ:results.getBody()){
                SelectorNodeDto dto=new SelectorNodeDto();
                dto.setId(organ.getId());
                dto.setPid(organ.getParentId());
                dto.setTitle(organ.getName());
                dto.setNtype("organ");
                list.add(dto);
            }
        }

        log.debug("退出：查询岗位树方法,type:{},parentId:{},keyword:{}",type,params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }
}
