package com.unione.cloud.portal.common.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.portal.common.dto.SelectorNodeDto;
import com.unione.cloud.portal.common.dto.SelectorUserDto;
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
     * @param type
     * @param params
     * @return
     */
    public Results<List<SelectorUserDto>> userNode(String type,Params<Long> params){
        


        return Results.success();
    }


     /**
     * 查询角色节点
     * @param type
     * @param params
     * @return
     */
    public Results<List<SelectorNodeDto>> roleNode(Integer type,Params<Long> params){
        log.debug("进入：查询角色节点方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysRole> queryOrgan = Params.build(SysRole.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setName(params.getKeywords());
        if(type!=null && type>0){
            queryOrgan.getBody().setTypes(type);
        }
        Results<List<SysRole>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
            .field("name","id","parentId")
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
    public Results<List<SelectorNodeDto>> organTree(Params<Long> params){
        log.debug("进入：查询机构树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysOrgan> queryOrgan = Params.build(SysOrgan.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setParentId(params.getBody());
        queryOrgan.getBody().setName(params.getKeywords());
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

        log.debug("退出：查询机构树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
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
        log.debug("进入：查询分组树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysGroup> queryOrgan = Params.build(SysGroup.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setParentId(params.getBody());
        queryOrgan.getBody().setName(params.getKeywords());
        if(type!=null && type>0){
            queryOrgan.getBody().setTypes(type);
        }
        Results<List<SysGroup>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
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

        log.debug("退出：查询分组树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
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
        log.debug("进入：查询岗位树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        List<SelectorNodeDto> list=new ArrayList<>();
        
        Params<SysPost> queryOrgan = Params.build(SysPost.class);
        queryOrgan.setPage(params.getPage());
        queryOrgan.setPageSize(params.getPageSize());
        queryOrgan.getBody().setParentId(params.getBody());
        queryOrgan.getBody().setName(params.getKeywords());
        queryOrgan.getBody().setTypes(type);
        Results<List<SysPost>> results=dataBaseDao.findPages(SqlBuilder.build(queryOrgan)
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

        log.debug("退出：查询岗位树方法,parentId:{},keyword:{}",params.getBody(),params.getKeywords());
        return Results.success(list)
            .setTotal(results.getTotal())
            .setPage(results.getPage())
            .setPageSize(results.getPageSize());
    }
}
