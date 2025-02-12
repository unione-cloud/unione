package com.unione.cloud.portal.system.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.unione.cloud.portal.system.model.BaseDictSetting;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	BaseDictSetting Controller 服务
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:06:12
 * @版本	1.0.0
 **/
@Slf4j
@RestController
@Tag(name = "BaseDictSetting 字典设置管理：基础：字典设置，租户，机构字典设置",description="BaseDictSetting")
@RequestMapping("/api/system/baseDictSetting")	 //TreeFeignApi
public class BaseDictSettingController implements PojoFeignApi<BaseDictSetting>{
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	
	@Override
	@Action(title="查询字典设置",type = ActionType.Query)
	public Results<List<BaseDictSetting>> find(Params<BaseDictSetting> params) {
		AssertUtil.service().notNull(params.getBody(),"请求参数body不能为空");
				
		Results<List<BaseDictSetting>> results = dataBaseDao.findPages(SqlBuilder.build(params));
				
		LogsUtil.add("分页数据统计，数据总量count:"+results.getTotal());
		LogsUtil.add("分页数据查询，记录数量size:"+results.getBody().size());
		return results;
	}


	@Override
	@Action(title="保存字典设置",type = ActionType.Save)
	public Results<Long> save(@Validated(Validator.save.class) BaseDictSetting entity) {
		// 参数处理
		int len = 0;
		if(entity.getId()==null) {
			len = dataBaseDao.insert(entity);
		}else {
			String[] fields = {"parentId","orgId","dictId","appId","appName","dictName","dictType","dictKey","dictValue","dictShow","ordered","isLeaf","status"};
			SqlBuilder<BaseDictSetting> sqlBuilder=SqlBuilder.build(entity).field(fields);
			len = dataBaseDao.updateById(sqlBuilder);
		}
		
		LogsUtil.save(len>0, entity.getId());
		log.debug("退出:保存字典设置.entity:{},result:true",entity);
		return Results.build(len>0, entity.getId());
	}


	@Override
	public Results<List<BaseDictSetting>> findByIds(Set<Long> ids) {
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		List<BaseDictSetting> rows = dataBaseDao.findByIds(SqlBuilder.build(BaseDictSetting.class,new ArrayList<>(ids)));
		return Results.success(rows);
	}


	@Override
	public Results<BaseDictSetting> detail(Long id) {
		// 参数处理
		AssertUtil.service().notNull(id,"参数id不能为空");
		
		LogsUtil.add("查找记录");
		BaseDictSetting tmp = dataBaseDao.findById(SqlBuilder.build(BaseDictSetting.class,id));
		AssertUtil.service().notNull(tmp, "记录未找到");
		return Results.success(tmp);
	}
	

	@Override
	@Action(title="删除字典设置",type = ActionType.Delete)
	public Results<Integer> delete(Set<Long> ids){
		Results<Integer> results = new Results<>();
		// 参数处理
		AssertUtil.service().isTrue(!ids.isEmpty(), "参数ids不能为空");
		
		// 执行删除
		LogsUtil.add("删除数ids:"+JSONUtil.toJsonStr(ids));
		int count = dataBaseDao.deleteById(SqlBuilder.build(BaseDictSetting.class,ids));
		LogsUtil.add("成功删除记录数量:"+count);
		
		results.setSuccess(count>0);
		results.setMessage(count>0?"操作成功":"操作失败");
		results.setBody(count);

		return results;
	}


}
