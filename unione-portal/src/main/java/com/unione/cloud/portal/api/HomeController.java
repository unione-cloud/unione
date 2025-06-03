package com.unione.cloud.portal.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.portal.dto.AppDto;
import com.unione.cloud.portal.dto.ResourceDto;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RefreshScope
@RestController
@Tag(name = "门户首页")
@RequestMapping("/api/portal")
public class HomeController {

	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	
	@PostMapping("/menus/{type}")
	@Action(title="加载门户菜单",type = ActionType.Query)
	@Operation(summary = "门户菜单", description = "同步加载当前用户拥有权限的菜单集合(树),参数type取值范围[pc,app]")
	public Results<List<AppDto>> menus(@PathVariable("type") String type){
		AssertUtil.service().notIn(type, Arrays.asList("pc","app"), "参数type取值范围[pc,app]");
		LogsUtil.add("加载门户菜单,type:%s",type);
		Map<String, Object> params=new HashMap<>();
		params.put("user", sessionService.getPrincipal());
		params.put("type", type);	
		params.put("isAdmin", sessionService.isAdmin());
		
		List<AppDto> appList = dataBaseDao.findList("portal.permision.loadAppPermisForUser",params, AppDto.class);
		List<Long> appIdsList = appList.stream().map(AppDto::getId).collect(Collectors.toList());
		LogsUtil.add("加载用户应用列表,app count:%s",appList.size());
		
		LogsUtil.add("加载用户资源列表");
		if(!ObjectUtil.isEmpty(appIdsList)){
			params.put("appIds", appIdsList);	
			List<ResourceDto> resList = dataBaseDao.findList("portal.permision.loadResorucePermisForUser",params, ResourceDto.class);
			List<ResourceDto> menuList=resList.stream()
					.filter(res->appIdsList.contains(res.getAppId()))
					.filter(res->ObjectUtil.equal(res.getTypes(),"btn") || ObjectUtil.equal(res.getTypes(),"menu"))
					.collect(Collectors.toList());
			List<ResourceDto> toolList=resList.stream()
					.filter(res->appIdsList.contains(res.getAppId()))
					.filter(res->ObjectUtil.equal(res.getTypes(),"tool"))
					.collect(Collectors.toList());
			
			LogsUtil.add("应用菜单数据处理,menu count:%s",menuList.size());
			Map<Long, ResourceDto> menuMap=new HashMap<>();
			Map<Long, List<ResourceDto>> appMenuMap=new HashMap<>();
			menuList.stream().forEach(res->{
				menuMap.put(res.getId(), res);
			});
			menuList.stream().forEach(res->{
				ResourceDto parent=menuMap.get(res.getParentId());
				if(parent!=null) {
					parent.getChildren().add(res);
				}else if(ObjectUtil.equal(res.getTypes(), "menu")){
					List<ResourceDto> list=appMenuMap.get(res.getAppId());
					if(list==null) {
						list=new ArrayList<>();
						appMenuMap.put(res.getAppId(), list);
					}
					list.add(res);
				}
			});
			
			LogsUtil.add("应用工具数据处理,menu count:%s",toolList.size());
			Map<Long, List<ResourceDto>> appToolMap=new HashMap<>();
			toolList.stream().forEach(res->{
				List<ResourceDto> list=appToolMap.get(res.getAppId());
				if(list==null) {
					list=new ArrayList<>();
					appToolMap.put(res.getAppId(), list);
				}
				list.add(res);
			});
			
			LogsUtil.add("应用数据处理");
			appList.stream().forEach(app->{
				List<ResourceDto> menus=appMenuMap.get(app.getId());
				if(menus!=null) {
					app.setMenus(menus);
				}
				List<ResourceDto> tools=appToolMap.get(app.getId());
				if(tools!=null) {
					app.setTools(tools);
				}
			});
		}
		
		return Results.success(appList);
	}
	
	
	
	
	
}
