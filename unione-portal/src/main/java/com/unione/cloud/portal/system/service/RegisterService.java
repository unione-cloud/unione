package com.unione.cloud.portal.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.portal.security.service.CaptchaService;
import com.unione.cloud.portal.system.dto.UserRegister;
import com.unione.cloud.portal.system.model.SysUser;
import com.unione.cloud.portal.system.model.SysUserRole;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 	注册服务
 * @作者	Jeking Yang
 * @日期	2023年9月30日 下午10:36:27
 * @版本	1.0.0
 */
@Slf4j
@Service
public class RegisterService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private CaptchaService captchaService;	
	
	
	/**
	 * 用户注册：是否开启注册功能
	 */
	@Value("${security.register.enable:true}")
	private boolean REGISGER_ENABLE;
	
	/**
	 * 用户注册：是否启用手机验证码
	 */
	@Value("${security.register.tel.enable:true}")
	private boolean REGISTER_TEL_CAPTCHA;
	

	/**
	 * 用户注册：是否开启审核
	 */
	@Value("${security.register.audit.enable:true}")
	private boolean REGISGER_AUDIT_ENABLE;
	
	
	private Map<Integer,List<Long>> REGISGER_DEFAULT_ROLES=new HashMap<>();	
	@SuppressWarnings("unchecked")
	@Value("${security.register.default.roles:{1:[10],2:[11,22]}}")
	public void setRegisterDefaultRoles(String maps) {
		REGISGER_DEFAULT_ROLES=new HashMap<>();	
		Map<String,List<Object>> map=JSONUtil.toBean(maps, Map.class);
		map.entrySet().stream().forEach(entry->{
			List<Long> values=entry.getValue().stream().map(v->Long.parseLong(v.toString())).collect(Collectors.toList());
			REGISGER_DEFAULT_ROLES.put(Integer.parseInt(entry.getKey()),values);
		});
	}
	
	
	private Map<String,Object> REGISGER_DEFAULT_INFO=new HashMap<>();
	@SuppressWarnings("unchecked")
	@Value("${security.register.default.info:{}}")
	public void setRegisterDefaultInfo(String info) {
		REGISGER_DEFAULT_INFO=JSONUtil.toBean(info, Map.class);
	}	
	
	
	/**
	 * 用户注册
	 * @param param
	 * @return
	 */
	public Results<Void> doRegister(UserRegister param){
		log.info("进入：用户注册方法,username:{},realName:{}",param.getUsername(),param.getRealName());
		LogsUtil.add("进入：用户注册方法,username:%s,realName:%s",param.getUsername(),param.getRealName());
		AssertUtil.service()
			.isTrue(REGISGER_ENABLE, "用户注册功能未开启")
			.notNull(param, new String[] {"username","pwdText"},"属性%s不能为空")
			.isTrue(captchaService.validate(param.getCaptcha()),"验证码不正确");
		
		if(REGISTER_TEL_CAPTCHA) {
			//TODO 手机短信验证码接入逻辑
		}
		
		Long userId = IdGenHolder.generate();
		SysUser user=new SysUser();
		BeanUtils.copyProperties(param, user);
		user.setId(userId);
		
		LogsUtil.add("验证用户账号和手机号是否已存在,usrename:%s,tel:%s",param.getUsername(),param.getTel());
		SqlBuilder<SysUser> untelBuilder=SqlBuilder.build(user).field("id,username,tel").where("username=? or tel=?");
		List<SysUser> untelList = dataBaseDao.findList(untelBuilder);
		untelList.stream().forEach(row->{
			AssertUtil.service()
				.isTrue(ObjectUtil.notEqual(row.getUsername(), param.getUsername()),"账号已存在")
				.isTrue(ObjectUtil.notEqual(row.getTel(),param.getTel()),"手机号已存在");			
		});
		
		LogsUtil.add("设置默认属性");
		user.setStatus(1);	//用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定	
		user.setAuditSts(REGISGER_AUDIT_ENABLE?1:2);	//审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过	
		user.setCreatedBy(userId);
		user.setLastUpdatedBy(userId);	
		BeanUtils.setDefaultValue(user, "userType", 2);	//用户类型，字典USERTYPE 1管理员，2普通用户，9其他	
		BeanUtils.setDefaultValue(user, REGISGER_DEFAULT_INFO);	
		
		LogsUtil.add("生成用户密码盐并对密码进行加密处理");
		user.setPwdSalt(Base64.encode(KeyUtil.generateKey("SM4").getEncoded()));
		String pwd = SmUtil.sm4(Base64.decode(user.getPwdSalt())).encryptHex(user.getPwdText());
		user.setPwdText(pwd);
		LogsUtil.add("保存用户信息");
		dataBaseDao.insertWithId(user);
		
		List<Long> roles=REGISGER_DEFAULT_ROLES.get(user.getUserType());
		LogsUtil.add("分配用户角色,roles:%s",roles);
		if(roles!=null) {
			List<SysUserRole> userRoles = roles.stream().map(roleId->{
				SysUserRole ur=new SysUserRole();
				ur.setRoleId(roleId);			
				ur.setUserId(user.getId());
				ur.setCreatedBy(user.getId());
				ur.setLastUpdatedBy(user.getId());
				return ur;
			}).collect(Collectors.toList());
			dataBaseDao.insertBatch(userRoles);
			LogsUtil.add("成功分配用户角色,roles:%s",roles);
		}
		
		return Results.success();
	}	
	

}
