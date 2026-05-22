package com.unione.cloud.system.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.security.service.CaptchaService;
import com.unione.cloud.system.dto.UserRegister;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.model.SysUserRole;
import com.unione.cloud.ums.service.UmsSmsService;
import com.unione.cloud.web.logs.LogsUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册服务
 * 
 * @作者 Jeking Yang
 * @日期 2023年9月30日 下午10:36:27
 * @版本 1.0.0
 */
@Slf4j
@Service
@RefreshScope
public class RegisterService {

	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private UmsSmsService umsSmsService;

	@Autowired
	private SecretService secretService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private RedisService redisService;

	@Autowired
	private TenantService tenantService;

	/**
	 * 用户注册：默认租户ID
	 */
	@Value("${security.register.default.tenantId:1}")
	private Long REGISGER_DEFAULT_TENANT_ID;


	@Value("${security.register.default.maxUserCount:2}")
	private int REGISGER_DEFAULT_MAX_USER_COUNT=0;

	/**
	 * 用户注册：默认最大用户数量
	 */
	@Value("${security.register.default.maxOnlineCount:1}")
	private int REGISGER_DEFAULT_MAX_ONLINE_COUNT;

	/**
	 * 用户注册：默认最大组织数量
	 */
	@Value("${security.register.default.maxOrgCount:0}")
	private int REGISGER_DEFAULT_MAX_ORG_COUNT;

	/**
	 * 用户注册：默认最大组织用户数量
	 */
	@Value("${security.register.default.maxOrgUserCount:0}")
	private int  REGISGER_DEFAULT_MAX_ORG_USER_COUNT;

	/**
	 * 用户注册：默认时间限制天数
	 */
	@Value("${security.register.default.timeLimitDay:7}")
	private int REGISGER_DEFAULT_TIME_LIMIT_DAY;



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

	/**
	 * 用户注册：必填属性
	 */
	@Value("${security.register.required.fields:username,tel,realName,password,company}")
	private String REGISGER_REQUIRED_FIELDS;

	private Map<Integer, List<Long>> REGISGER_DEFAULT_ROLES = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Value("${security.register.default.roles:{1:[105,108],2:[107,108]}}")
	public void setRegisterDefaultRoles(String maps) {
		REGISGER_DEFAULT_ROLES = new HashMap<>();
		Map<String, List<Object>> map = JSONUtil.toBean(maps, Map.class);
		map.entrySet().stream().forEach(entry -> {
			List<Long> values = entry.getValue().stream().map(v -> Long.parseLong(v.toString()))
					.collect(Collectors.toList());
			REGISGER_DEFAULT_ROLES.put(Integer.parseInt(entry.getKey()), values);
		});
	}

	private Map<String, Object> REGISGER_DEFAULT_INFO = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Value("${security.register.default.info:{}}")
	public void setRegisterDefaultInfo(String info) {
		REGISGER_DEFAULT_INFO = JSONUtil.toBean(info, Map.class);
	}

	/**
	 * 用户注册
	 * @param param
	 * @return
	 */
	@Transactional
	public Results<Void> doRegister(UserRegister param){
		log.info("进入：用户注册方法,username:{},realName:{}",param.getUsername(),param.getRealName());
		LogsUtil.add("进入：用户注册方法,username:%s,realName:%s",param.getUsername(),param.getRealName());
		AssertUtil.service()
			.isTrue(REGISGER_ENABLE, "用户注册功能未开启");
		List<String> required=Arrays.asList(REGISGER_REQUIRED_FIELDS.split(","));
		if(!ObjectUtil.isEmpty(REGISGER_REQUIRED_FIELDS)){
			AssertUtil.service().notNull(param, REGISGER_REQUIRED_FIELDS.split(","), "属性%s不能为空");
		}
		if(REGISTER_TEL_CAPTCHA && required.contains("tel")) {
			//短信验证码
			AssertUtil.service().isTrue(umsSmsService.valiCaptcha("register",param.getTel(),param.getCaptcha()),"短信验证码不正确");
		}else{
			// 图形验证码
			AssertUtil.service().isTrue(captchaService.validate(param.getCaptcha()),"验证码不正确");
		}

		try{
			Long userId = IdGenHolder.generate();
			SysUser user=new SysUser();
			BeanUtils.setDefaultValue(user, REGISGER_DEFAULT_INFO);	
			BeanUtils.copyProperties(param, user);
			user.setId(userId);
			user.setUserType(2);		//用户类型，字典USERTYPE 1管理员，2普通用户，9其他	

			if(ObjectUtil.isEmpty(user.getUsername()) && !ObjectUtil.isEmpty(user.getTel())){
				//帐号为空，使用手机号作为帐号
				user.setUsername(param.getTel());
			}

			Long tenantId=REGISGER_DEFAULT_TENANT_ID;
			if(!ObjectUtil.isEmpty(param.getCompany())){
				//加载租户
				SysTenant tenant=tenantService.loadTenant(param.getCompany().trim());
				if(tenant!=null){
					tenantId=tenant.getId();
				}else{
					tenantId = redisService.doHpdl(new HpdlProcess<Long>(String.format("security:register:tenant:%s", param.getCompany())) {
						@Override
						public Long process() {
							SysTenant tenant=dataBaseDao.findOne(SqlBuilder.build(SysTenant.class)
								.where("name=?")
								.where("name", param.getCompany().trim()));
							if(tenant==null) {
								tenant=new SysTenant();
								tenant.setId(IdGenHolder.generate());
								tenant.setName(param.getCompany());
								tenant.setRegisteWay(1);
								tenant.setAdminId(userId);
								tenant.setMaxUserCount(REGISGER_DEFAULT_MAX_USER_COUNT);
								tenant.setMaxUserOnline(REGISGER_DEFAULT_MAX_ONLINE_COUNT);
								tenant.setMaxOrganCount(REGISGER_DEFAULT_MAX_ORG_COUNT);
								tenant.setMaxOrganUserCouint(REGISGER_DEFAULT_MAX_ORG_USER_COUNT);
								tenant.setTimeLimitStart(DateUtil.date());
								if(REGISGER_DEFAULT_TIME_LIMIT_DAY>0){
									tenant.setTimeLimitEnd(DateUtil.date().offset(DateField.DAY_OF_YEAR, REGISGER_DEFAULT_TIME_LIMIT_DAY));
								}
								tenant.setSn(String.valueOf(tenant.getId()));
								tenant.setDelFlag(0);
								tenant.setStatus(1);
								tenant.setCreatedBy(userId);
								tenant.setLastUpdatedBy(userId);
								int len = dataBaseDao.insertWithId(tenant);
								if(len<=0){
									return null;
								}
								user.setUserType(1);
							}
							return tenant.getId();
						}
					}, 500, 3);
					AssertUtil.service().notNull(tenantId,"租户信息保存失败");
				}
			}
			
			LogsUtil.add("验证用户账号和手机号是否已存在,usrename:%s,tel:%s",param.getUsername(),param.getTel());
			SqlBuilder<SysUser> untelBuilder=SqlBuilder.build(user).field("id,username,tel").where("username=? or tel=?");
			List<SysUser> untelList = dataBaseDao.findList(untelBuilder);
			untelList.stream().forEach(row->{
				AssertUtil.service()
					.isTrue(ObjectUtil.notEqual(row.getUsername(), param.getUsername()),"账号已存在")
					.isTrue(ObjectUtil.notEqual(row.getTel(),param.getTel()),"手机号已存在");			
			});
			
			LogsUtil.add("设置默认属性");
			user.setTenantId(tenantId);
			user.setOrgId(tenantId);
			user.setDelFlag(0);
			user.setStatus(1);	//用户状态，字典USERSTATUS 1正常，2禁用，3注销，4锁定	
			user.setAuditSts(REGISGER_AUDIT_ENABLE?1:2);	//审核状态，字典USERAUDITSTS 1待审核，2审核通过，3审核不通过	
			user.setCreatedBy(userId);
			user.setLastUpdatedBy(userId);	
			
			
			LogsUtil.add("生成用户密码盐并对密码进行加密处理");
			user.setPwdSalt(RandomUtil.randomString(16));
			String randomPwd=null;
			if(ObjectUtil.isEmpty(user.getPwdText())){
				// 密码为空，生成随机密码，注册成功后通过短信发送到手机上
				randomPwd=RandomUtil.randomNumbers(6);
				user.setPwdText(randomPwd);
			}else{
				// 密码解密处理
				user.setPwdText(secretService.decrypt(user.getPwdText()));
			}
			// 对用户密码进行加盐加密处理
			String pwd = SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(user.getPwdText());
			user.setPwdText(pwd);
			LogsUtil.add("保存用户信息");
			int len = dataBaseDao.insertWithId(user);
			AssertUtil.service().isTrue(len>0,"用户信息保存失败");

			List<Long> roles=REGISGER_DEFAULT_ROLES.get(user.getUserType());
			LogsUtil.add("分配用户角色,roles:%s",roles);
			if(roles!=null) {
				List<SysUserRole> userRoles = roles.stream().map(roleId->{
					SysUserRole ur=new SysUserRole();
					ur.setTenantId(user.getTenantId());
					ur.setOrgId(user.getOrgId());
					ur.setRoleId(roleId);			
					ur.setUserId(user.getId());
					ur.setEnDilivery(0);
					ur.setCreatedBy(user.getId());
					ur.setLastUpdatedBy(user.getId());
					return ur;
				}).collect(Collectors.toList());
				dataBaseDao.insertBatch(userRoles);
				LogsUtil.add("成功分配用户角色,roles:%s",roles);
			}

			// 如果是手机号注册，发送随机密码到手机号
			if(!ObjectUtil.isEmpty(randomPwd)){
				// LogsUtil.add("发送随机密码到手机号,randomPwd:%s,tel:%s",randomPwd,param.getTel());
				// TODO
			}

			// 生成用户token
			UserPrincipal principal=new UserPrincipal();
			BeanUtil.copyProperties(user, principal);
			String token = tokenService.transform(principal,user.getPwdText());
			SessionHolder.setToken(token);
        	SessionHolder.setUserPrincipal(principal);

			return Results.success();
		}catch(Exception e) {
			log.error("用户注册失败,username:{},realName:{}",param.getUsername(),param.getRealName(),e);
			LogsUtil.add("用户注册失败,username:%s,realName:%s",param.getUsername(),param.getRealName());
			LogsUtil.error(e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

			if(e instanceof ServiceException){
				return Results.failure(e.getMessage());
			}
		}
		return Results.failure("用户注册失败");
	}

}
