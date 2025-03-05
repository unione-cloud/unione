package com.unione.cloud.portal.common.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.portal.common.dto.SelectorNodeDto;
import com.unione.cloud.portal.common.dto.SelectorUserDto;
import com.unione.cloud.portal.common.service.SelectorService;

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
    @PostMapping("/{type}/user")
    @Operation(summary = "选择器:用户节点", description = "参数：目标类型不能为空[organ,post,group,role]")
    public Results<List<SelectorUserDto>> userNode(@PathVariable("type") String type, @RequestBody Params<Long> params) {
        log.debug("进入:用户节点查询接口,参数:{}", params);

        Results<List<SelectorUserDto>> results = selectorService.userNode(type,params);

        log.debug("退出:用户节点查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

     /**
     * 选择器:角色节点
     * @param params
     * @return
     */
    @PostMapping("/role/node/{type}")
    @Operation(summary = "选择器:角色节点", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorNodeDto>> roleNode(@PathVariable("type") Integer type,@RequestBody Params<Long> params) {
        log.debug("进入:角色节点查询接口,参数:{}", params);

        Results<List<SelectorNodeDto>> results = selectorService.roleNode(type, params);

        log.debug("退出:角色节点查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }



    /**
     * 选择器:机构树
     * @param params
     * @return
     */
    @PostMapping("/organ/tree")
    @Operation(summary = "选择器:机构树", description = "")
    public Results<List<SelectorNodeDto>> organTree(@RequestBody Params<Long> params) {
        log.debug("进入:机构树查询接口,参数:{}", params);

        Results<List<SelectorNodeDto>> results = selectorService.organTree(params);

        log.debug("退出:机构树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

     /**
     * 选择器:分组树
     * @param params
     * @return
     */
    @PostMapping("/group/tree/{type}")
    @Operation(summary = "选择器:分组树", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorNodeDto>> groupTree(@PathVariable("type") Integer type,@RequestBody Params<Long> params) {
        log.debug("进入:分组树查询接口,参数:{}", params);

        Results<List<SelectorNodeDto>> results = selectorService.groupTree(type,params);

        log.debug("退出:分组树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

    /**
     * 选择器:岗位树
     * @param params
     * @return
     */
    @PostMapping("/post/tree/{type}")
    @Operation(summary = "选择器:岗位树", description = "参数：类型type<=0：全部，大于0：指定类型")
    public Results<List<SelectorNodeDto>> postTree(@PathVariable("type") Integer type,@RequestBody Params<Long> params) {
        log.debug("进入:岗位树查询接口,参数:{}", params);

        Results<List<SelectorNodeDto>> results = selectorService.postTree(type,params);

        log.debug("退出:岗位树查询接口,参数:{},result:{}", params,results.isSuccess());
        return results;
    }

}
