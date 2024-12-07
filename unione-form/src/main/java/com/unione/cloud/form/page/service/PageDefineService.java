package com.unione.cloud.form.page.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.page.model.SysPageDefine;
import com.unione.cloud.form.security.UserFormRoles;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RefreshScope
public class PageDefineService {

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Value("${form.page.default.appid:1000}")
	private Long DEFAULT_APP_ID;
	
	/**
	 * 保存页面定义信息
	 * @param entity
	 * @return
	 */
	public Results<SysPageDefine> saveDefine(@Validated(Validator.save.class) @RequestBody SysPageDefine entity) {
		log.debug("进入:新增页面定义信息.entity:{}",entity);
		LogsUtil.set(LogType.Insert, "新增页面定义");
		AssertUtil.service().isTrue(sessionService.hasRole(UserFormRoles.FORM_ADMIN,
				UserFormRoles.FORM_CONFIG,
				UserFormRoles.FORM_DEV), "当前帐号无权限");
		if("new_".equals(entity.getSn())) {
			entity.setSn(null);
		}
		
		SysPageDefine tmp = null;
		if(!StringUtils.isEmpty(entity.getSn())) {
			SysPageDefine param=SysPageDefine.builder().sn(entity.getSn()).build();
			param.setTenantId(sessionService.getTenantId());
			tmp = dataBaseDao.findOne(SqlBuilder.build(param));
		}
		
		if(tmp!=null) {
			entity.setId(tmp.getId());
			// 更新
			String[] fields = {"title","component","summary","icon","picMax","picMid","picMix","types","trades","reviewPic","configs","isTmpl","isGlobal","descs"};
			SqlBuilder<SysPageDefine> sqlBuilder=SqlBuilder.build(entity).field(fields);
			int len = dataBaseDao.updateById(sqlBuilder);
			LogsUtil.add("保存数据,len:"+len);
		}else {
			// 新增
			// 参数处理
			if(StringUtils.isEmpty(entity.getSn())) {
				entity.setSn(RandomUtil.randomString(20));
			}
			BeanUtils.setDefaultValue(entity, "appId", DEFAULT_APP_ID);
			BeanUtils.setDefaultValue(entity, "isTmpl",0);
			BeanUtils.setDefaultValue(entity, "isGlobal",0);
			BeanUtils.setDefaultValue(entity, "status",1);
			entity.setVers(1);
			
			int len = dataBaseDao.insert(entity);
			AssertUtil.service().isTrue(len>0, "页面保存失败");
		}
		
		LogsUtil.success(entity.getId());
		log.debug("退出:新增页面定义信息.entity:{},result:true",entity);
		return Results.success(entity);
	}
	
}
