package com.unione.cloud.common.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.common.dto.SelectorGroupDto;
import com.unione.cloud.common.dto.SelectorGroupParam;
import com.unione.cloud.common.dto.SelectorOrganDto;
import com.unione.cloud.common.dto.SelectorOrganParam;
import com.unione.cloud.common.dto.SelectorPostDto;
import com.unione.cloud.common.dto.SelectorPostParam;
import com.unione.cloud.common.dto.SelectorRoleDto;
import com.unione.cloud.common.dto.SelectorRoleParam;
import com.unione.cloud.common.dto.SelectorUserDto;
import com.unione.cloud.common.dto.SelectorUserParam;
import com.unione.cloud.common.dto.TreeNodeDto;
import com.unione.cloud.common.service.SelectorService;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


/**
 * @标题 	公共服务：选择器组件
 * @作者	Unione Cloud
 * @日期	2024-11-22 23:32:02
 * @版本	1.0
 */
@Slf4j
@RefreshScope
@RestController
@Tag(name = "公共服务：选择器组件")
@RequestMapping("/api/selector")
public class SelectorController {

   
    @Autowired
    private SelectorService selectorService;


    /**
     * 选择器:用户节点
     * @param type  
     * @param params
     * @return
     */
    @PostMapping("/user/node")
    @Action(title="选择器:用户节点",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:用户节点", description = "参数：目标类型不能为空[organ,post,group,role]")
    public Results<List<SelectorUserDto>> userNode(@RequestBody Params<SelectorUserParam> params) {
        log.debug("进入:用户节点查询接口,参数:{}", params);

        Results<List<SelectorUserDto>> results = selectorService.userNode(params);

        log.debug("退出:用户节点查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

     /**
     * 选择器:角色节点
     * @param params
     * @return
     */
    @PostMapping("/role/node")
    @Action(title="选择器:角色节点",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:角色节点", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorRoleDto>> roleNode(@RequestBody Params<SelectorRoleParam> params) {
        log.debug("进入:角色节点查询接口,参数:{}", params);

        Results<List<SelectorRoleDto>> results = selectorService.roleNode(params);

        log.debug("退出:角色节点查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }


    /**
     * 选择器:角色列表
     * @param params
     * @return
     */
    @PostMapping("/role/list")
    @Action(title="选择器:角色列表",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:角色列表", description = "参数：类型type=permis授权,use:角色使用")
    public Results<List<SelectorRoleDto>> roleList(@RequestBody Params<SelectorRoleParam> params) {
        log.debug("进入:角色列表查询接口,参数:{}", params);

        Results<List<SelectorRoleDto>> results = selectorService.roleList(params);

        log.debug("退出:角色列表查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }


    /**
     * 选择器:机构树
     * @param params
     * @return
     */
    @PostMapping("/organ/tree")
    @Action(title="选择器:机构树",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:机构树", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorOrganDto>> organTree(@RequestBody Params<SelectorOrganParam> params) {
        log.debug("进入:机构树查询接口,参数:type:{}, params:{}",params.getBody().getOtype(), params);

        Results<List<SelectorOrganDto>> results = selectorService.organTree(params);

        log.debug("退出:机构树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

     /**
     * 选择器:分组树
     * @param params
     * @return
     */
    @PostMapping("/group/tree")
    @Action(title="选择器:分组树",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:分组树", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorGroupDto>> groupTree(@RequestBody Params<SelectorGroupParam> params) {
        log.debug("进入:分组树查询接口,参数:type:{},param:{}",params.getBody().getGtype(), params);

        Results<List<SelectorGroupDto>> results = selectorService.groupTree(params);

        log.debug("退出:分组树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

    /**
     * 选择器:岗位树
     * @param params
     * @return
     */
    @PostMapping("/post/tree")
    @Action(title="选择器:岗位树",type = ActionType.Query,nolog = true)
    @Operation(summary = "选择器:岗位树", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorPostDto>> postTree(@RequestBody Params<SelectorPostParam> params) {
        log.debug("进入:岗位树查询接口,参数:type:{},param:{}",params.getBody().getPtype(), params);

        Results<List<SelectorPostDto>> results = selectorService.postTree(params);

        log.debug("退出:岗位树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

}
