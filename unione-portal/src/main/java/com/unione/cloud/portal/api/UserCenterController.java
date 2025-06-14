package com.unione.cloud.portal.api;

import java.time.Duration;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SmUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RefreshScope
@RestController
@Tag(name = "统一门户: 个人中心")
@RequestMapping("/api/ucenter")
public class UserCenterController {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;

	@Autowired
	private SecretService secretService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private RedisService redisService;
	
	
	@PostMapping("/profile/load")
	@Action(title="获取我的信息",type = ActionType.Query)
	@Operation(summary="获取我的信息",description="获取当前用户信息,排除字段：pwdText,pwdSalt")
	public Results<SysUser> loadProfile() {
		Long userId = sessionService.getUserId();
		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getRealname());
		// 参数处理
		AssertUtil.service().notNull(userId,"当前用户未登录");
		
		SysUser tmp = dataBaseDao.findById(SqlBuilder.build(SysUser.class, userId));
		AssertUtil.service().notNull(tmp, "用户信息未找到");
		tmp.setPwdText(null);
		tmp.setPwdSalt(null);
		
		return Results.success(tmp);
	}

	@PostMapping("/profile/save")
	@Action(title="修改我的信息",type = ActionType.Save)
	@Operation(summary="修改我的信息",description="修改我的信息，只能修改基本信息，只能能修改：realName,aliasName,avatar,birthday,sex,qq,email,descs等信息")
	public Results<Void> saveProfile(@RequestBody SysUser entity){
		log.debug("进入控制:修改个人用户信息方法，entity:{}",entity);
		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getRealname());
		// 参数处理
		AssertUtil.service().notNull(entity, new String[] {"id"},"参数%s不能为空");
		
		String fields[] = {"realName","aliasName","avatar","birthday","sex","qq","email","qq","descs"};
		entity.setId(sessionService.getUserId());
		int count = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
		
		return Results.build(count>0);
	}


	@PostMapping("/profile/tel")
	@Action(title="修改我的信息:手机号",type = ActionType.Save)
	@Operation(summary="修改我的信息:手机号",description="修改手机号逻辑：首先验证旧手机号是否一致，然后再验证手机短信验证码是否正确，如果都正确才执行更新")
	public Results<Void> saveProfileTel(@RequestParam(value="oldTel",required = false) String oldTel,@RequestParam("newTel") String newTel,@RequestParam(value="smsCode",required = false) String smsCode){
		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getRealname());
		LogsUtil.add("修改电话号码，newTel:%s,oldTel:%s,smsCode:%s",oldTel,newTel,smsCode);
			
		// 参数处理
		AssertUtil.service().notNull("newTel", "参数newTel不能为空");
		
		// 判断预留号码
		Long userId = sessionService.getUserId();
		SysUser tmp = dataBaseDao.findById(SqlBuilder.build(SysUser.class, userId));
		AssertUtil.service().notNull(tmp, "记录未找到");
		
		if(StringUtils.isNotEmpty(tmp.getTel())) {
			AssertUtil.service().isTrue(tmp.getTel().equals(oldTel), "旧号码与预留的号码不一致");
		}

		// TODO
		// 校验验证码	
		
		String fields[] = {"tel"};
		SysUser entity = new SysUser();
		entity.setId(sessionService.getUserId());
		entity.setTel(newTel);
		int count = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
		
		return Results.build(count>0);
	}


	@PostMapping("/profile/sq")
	@Action(title="修改我的信息:密保问题",type = ActionType.Save)
	@Operation(summary="修改我的信息:密保问题",description="修改密保问题逻辑：验证手机短信验证码是否正确，如果都正确才执行更新")
	public Results<Void> saveProfileSq(@RequestParam("securityQuestion") String securityQuestion,@RequestParam(value="smsCode",required = false) String smsCode){

		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getRealname());
		// 参数处理
		AssertUtil.service().notNull(securityQuestion,"参数securityQuestion不能为空");

		// TODO
		// 校验验证码	
		
		String fields[] = {"securityQuestion"};
		SysUser entity = new SysUser();
		entity.setId(sessionService.getUserId());
		entity.setSecurityQuestion(securityQuestion);
		int count = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
			
		return Results.build(count>0);
	}

	@PostMapping("/profile/smfa")
	@Action(title="修改我的信息:密保问题",type = ActionType.Save)
	@Operation(summary="修改我的信息:MFA设备标识",description="修改MFA设备标识逻辑：验证手机短信验证码是否正确，如果都正确才执行更新")
	public Results<Void> saveProfileSmfa(@RequestParam("sucurityMfa") String sucurityMfa,@RequestParam(value="smsCode",required = false) String smsCode) {
		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getRealname());
			
		// 参数处理
		AssertUtil.service().notNull(sucurityMfa,"MFA设备标识参数不能为空");

		// TODO
		// 校验验证码	
		
		String fields[] = {"sucurityMfa"};
		SysUser entity = new SysUser();
		entity.setId(sessionService.getUserId());
		entity.setSucurityMfa(sucurityMfa);
		int count = dataBaseDao.updateById(SqlBuilder.build(entity).field(fields));
			
		return Results.build(count>0);
	}

	
	@PostMapping("/reset/pwd")
	@Action(title="重置密码",type = ActionType.ResetPwd)
	@Operation(summary = "重置密码", description = "修改当前账户的密码")
	public Results<Void> resetPwd(@RequestParam("oldPwd") String oldPwd,@RequestParam("newPwd") String newPwd,@RequestParam(value="smsCode",required=false) String smsCode){
		LogsUtil.setTarget(sessionService.getUserId(), sessionService.getUsername());
		// 参数处理
		AssertUtil.service()
			.notNull(oldPwd,"参数oldPwd不能为空")
			.notNull(newPwd,"参数newPwd不能为空");
		oldPwd=secretService.decrypt(oldPwd);
		newPwd=secretService.decrypt(newPwd);
		AssertUtil.service()
			.isTrue(!oldPwd.equals(newPwd), "新旧密码不能一致");
		
		LogsUtil.add("查找用户记录");
		SysUser user = dataBaseDao.findById(SqlBuilder.build(SysUser.class, sessionService.getUserId()));
		AssertUtil.service()
			.notNull(user, "记录未找到")
			.notNull(user.getPwdSalt(), "帐号异常，密码盐值为空")
			.notNull(user.getTel(), "帐号异常，手机号为空");
		
		LogsUtil.add("旧密码对比");
		String pwd = SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(oldPwd);
		AssertUtil.service()
			.notEq(pwd, user.getPwdText(),"旧密码验证失败");

		// TODO
		// 校验验证码		

		LogsUtil.add("修改密码");
		user.setPwdText(SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(newPwd));
		int len=dataBaseDao.updateById(SqlBuilder.build(user).field("pwdText"));

		return Results.build(len>0);
	}

	
	@PostMapping("/forget/pwd/step1")
	@Action(title="忘记密码[步骤1]",type = ActionType.ForgetPwd)
	@Operation(summary = "忘记密码[步骤1]", description = "找回密码第一步：1、验证帐号+手机号后四位，2、验证通过则后台发送短信验证码并返回skey(有效时间5分钟)")
	public Results<String> forgetPwdStep1(@RequestParam("username") String username,@RequestParam("tel") String tel){
		// 参数处理
		AssertUtil.service()
			.notNull(username,"参数username不能为空")
			.notNull(tel,"参数tel不能为空")
			.isTrue(tel.length()==4, "请正确输入手机号后四位");
		
		LogsUtil.add("验证username:%s",username);
		SysUser tmp=new SysUser();
		tmp.setUsername(username);
		tmp=dataBaseDao.findOne(tmp);
		AssertUtil.service().notNull(tmp, "帐号或手机号不正确")
			.notNull(tmp.getTel(), "帐号或手机号不正确")
			.isTrue(tmp.getTel().endsWith(tel), "帐号或手机号不正确");

		LogsUtil.setTarget(tmp.getId(), tmp.getUsername());
		UserPrincipal principal=new UserPrincipal();
		principal.setId(tmp.getId());
		principal.setUsername(tmp.getUsername());
		principal.setOrgId(tmp.getOrgId());
		principal.setTenantId(tmp.getTenantId());
		principal.setRealName(tmp.getRealName());
		tokenService.build(principal);
		
		// TODO
		// 发送短信验证码
		
		String skey=UUID.fastUUID().toString();
		redisService.put("FORGETPWD:SKEY:"+username, skey, Duration.ofMinutes(5));
		LogsUtil.add("生成skey并加入redis缓存,skey:%s",skey);
		
		return Results.success(skey);
	}

	
	@PostMapping("/forget/pwd/step2")
	@Action(title="忘记密码[步骤2]",type = ActionType.ForgetPwd)
	@Operation(summary = "忘记密码[步骤2]", description = "找回密码第二步：1、验证帐号+手机验证码，2、设置新密码。skey为第一步返回的回话key")
	public Results<Void> forgetPwdStep2(@RequestParam("username") String username,@RequestParam("pwd") String pwd,
			@RequestParam("captcha") String captcha,@RequestParam("skey") String skey){
		// 参数处理
		AssertUtil.service()
			.notNull(username,"参数username不能为空")
			.notNull(pwd,"参数pwd不能为空")
			.notNull(captcha,"参数captcha不能为空")
			.notNull(skey,"参数skey不能为空");
		
		String cskey=redisService.getObj("FORGETPWD:SKEY:"+username);
		LogsUtil.add("验证skey是否一致,cache skey:%s,skey:%s",cskey,skey);
		AssertUtil.service().isTrue(ObjectUtils.equals(cskey, skey), "请求错误,请重新获取验证码");
		
		LogsUtil.add("验证username");
		SysUser tmp=new SysUser();
		tmp.setUsername(username);
		tmp=dataBaseDao.findOne(tmp);
		AssertUtil.service().notNull(tmp, "帐号或手机号不正确")
			.notNull(tmp.getTel(), "帐号或手机号不正确")
			.notNull(tmp.getPwdSalt(), "帐号异常，密码盐值为空");

		LogsUtil.setTarget(tmp.getId(), tmp.getUsername());
		UserPrincipal principal=new UserPrincipal();
		principal.setId(tmp.getId());
		principal.setUsername(tmp.getUsername());
		principal.setOrgId(tmp.getOrgId());
		principal.setTenantId(tmp.getTenantId());
		principal.setRealName(tmp.getRealName());
		tokenService.build(principal);

		// TODO
		// 验证短信验证码
		LogsUtil.add("验证短信验证码是否一致,captcha:%s",captcha);
		
		LogsUtil.add("设置新密码");
		String newPwd=secretService.decrypt(pwd);
		tmp.setPwdText(SmUtil.sm4(tmp.getPwdSalt().getBytes()).encryptHex(newPwd));
		int len=dataBaseDao.updateById(SqlBuilder.build(tmp).field("pwdText"));
		
		return Results.build(len>0);
	}
	

}
