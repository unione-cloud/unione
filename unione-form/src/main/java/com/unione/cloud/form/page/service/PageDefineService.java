package com.unione.cloud.form.page.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.page.model.SysPageDefine;
import com.unione.cloud.form.page.model.SysPageHis;
import com.unione.cloud.form.page.model.SysPageRelease;
import com.unione.cloud.form.security.UserFormRoles;
import com.unione.cloud.web.logs.LogsUtil;
import com.unione.cloud.web.logs.LogsUtil.LogType;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RefreshScope
@Service("pageDefineService")
public class PageDefineService {

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SecretService secretService;
	
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
	
	
	/**
	 * 页面定义：签名
	 * @param define
	 * @return
	 */
	public String doSignature(SysPageDefine define) {
		StringBuffer tmp=new StringBuffer();
		tmp.append(define.getComponent())
		   .append(define.getConfigs());
		
		String signature=secretService.hash(tmp.toString());
		define.setSignature(signature);
		return signature;
	} 
	
	
	
	/**
	 * 发布页面定义
	 * @param ids
	 * @return
	 */
	@Transactional
	public Results<String> release(Set<Long> ids){
		log.info("进入：发布页面定义方法,ids:{}",ids);
		AssertUtil.service().notEmpty(ids, "参数数据定义id集合不能为空");		
		LogsUtil.add("ids:%s",JSONUtil.toJsonStr(ids));
		
		LogsUtil.add("加载数据定义列表");
		List<SysPageDefine> defines=dataBaseDao.findByIds(SqlBuilder.build(SysPageDefine.class).ids(ids));		
		
		LogsUtil.add("验证数据定义加载结果");
		defines.stream().forEach(d->ids.remove(d.getId()));
		AssertUtil.service().isTrue(ids.isEmpty(), "参数页面定义对象未找到,ids:"+JSONUtil.toJsonStr(ids));	
		
		LogsUtil.add("验证页面定义是否被删除");
		List<String> delNames = defines.stream()
			.filter(d->Objects.equals(1, d.getDelFlag()))
			.map(d->d.getTitle())
			.collect(Collectors.toList());
		AssertUtil.service().isTrue(delNames.isEmpty(), "页面定义已删除"+JSONUtil.toJsonStr(delNames));
		
		LogsUtil.add("加载页面定义发布列表");
		Map<Long,SysPageRelease> definesRelease=dataBaseDao.findList(SqlBuilder.build(SysPageRelease.class).ids(ids))
				.stream().collect(Collectors.toMap(SysPageRelease::getId, (v1)->v1));
		
		LogsUtil.add("迭代数据定义列表，处理发布逻辑");
		StringBuffer success=new StringBuffer();
		StringBuffer unchange=new StringBuffer();
		
		try {
			defines.stream().forEach(define->{
				SysPageRelease release=definesRelease.get(define.getId());
				if(release==null || !Objects.equals(release.getSignature(),define.getSignature())){
					// 未发布、签名不一致（有更新）
					doRelease(define,release);
					success.append(define.getTitle()).append(",");
				}else {
					// 数据定义未更新，不需要重新发布
					unchange.append(define.getTitle()).append(",");
				}
				
			});
		} catch (Exception e) {
			log.error("发布页面定义失败",e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return	Results.failure();
		};
		
		StringBuffer info=new StringBuffer();
		info.append("成功发布:").append(success.length()>0?success.substring(0,success.length()-1):"0");
		if(unchange.length()>0) {
			info.append(",无变化：").append(unchange.substring(0,unchange.length()-1));
		}
		LogsUtil.add("result:%s",info.toString());
		
		return Results.success(info.toString());
	}
	
	
	/**
	 * 	发布页面定义
	 * @param define
	 * @param relase
	 */
	private void doRelease(SysPageDefine define,SysPageRelease relase) {
		log.info("进入：发布页面定义方法,define id:{},release id:{}",define.getId(),relase!=null?relase.getId():"null");		
		LogsUtil.add("进入：发布页面定义方法,define id:%s,release id:%s",define.getId(),relase!=null?relase.getId():"null");
		LogsUtil.add("data define id:%s",define.getId());
		
		define.setPublishDate(DateUtil.date());
		define.setStatus(2);
				
		// 发布记录
		if(relase!=null) {
			LogsUtil.add("新版本发布，curent ver:%s",define.getVers());
			define.setVers(define.getVers()+1);
			dataBaseDao.deleteById(SqlBuilder.build(SysPageRelease.class).id(define.getId()));
		}else {
			define.setVers(1);
			LogsUtil.add("首次发布，curent ver:%s",define.getVers());
		}
		relase=new SysPageRelease();
		BeanUtils.copy(define, relase);
		int len = dataBaseDao.insertWithId(relase);
		AssertUtil.service().isTrue(len>0, "数据定义发布失败");
		
		// 发布历史
		SysPageHis defineHis=new SysPageHis();
		BeanUtils.copy(define, defineHis);
		defineHis.setPageId(define.getId());
		dataBaseDao.insert(defineHis);
		LogsUtil.add("保存页面定义历史，pid:%s,hid:%s",defineHis.getPageId(),defineHis.getId());		
		
		len = dataBaseDao.updateById(SqlBuilder.build(define).field("vers","publishDate","status"));
		AssertUtil.service().isTrue(len>0, "页面定义发布失败");
		
	}
	
	
	
	
	
	
	
}
