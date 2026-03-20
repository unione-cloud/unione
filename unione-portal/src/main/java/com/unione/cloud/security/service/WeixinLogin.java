package com.unione.cloud.security.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.model.SysUserBind;
import com.unione.cloud.system.model.SysUserRole;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;

@Slf4j
@Service
public class WeixinLogin {

    @Autowired
    private WxOAuth2Service wxOAuth2Service;

    @Autowired
    private DataBaseDao dataBaseDao;

    @Value("${security.bind.defaultTenantId:-1}")
	private Long DEFUALT_TENANT_ID;

	@Value("${security.bind.defaultOrgId:-1}")
	private Long DEFAULT_ORG_ID;

	@Value("${security.bind.defaultRoles:}")
	private String DEFAULT_ROLES;

    /**
     * 微信登录
     * @param code
     * @return
     */
    public SysUser login(String code) {
        SysUser user = null;
        try {
            WxOAuth2AccessToken accessToken = wxOAuth2Service.getAccessToken(code);
            String openId = accessToken.getOpenId();
            SysUserBind bind = new SysUserBind();
            bind.setPlatKey("weixin");
            bind.setOpenId(openId);
            SysUserBind tmp = dataBaseDao.findOne(SqlBuilder.build(bind));
            if (tmp != null) {
                // 已绑定
                user = dataBaseDao.findOne(SqlBuilder.build(SysUser.class).where("id", tmp.getUserId()));
            }
            if(user==null){
                // 首次绑定,自动创建帐号
                WxOAuth2UserInfo userInfo = wxOAuth2Service.getUserInfo(accessToken, "zh_CN");
                user = dataBaseDao.findOne(SqlBuilder.build(SysUser.class).where("username", userInfo.getOpenid()));
                if (user == null) {
                    user = new SysUser();
                    user.setPwdSalt(RandomUtil.randomString(16));
                    user.setId(IdGenHolder.generate());
                    user.setUsername(userInfo.getOpenid());
                    user.setAliasName(userInfo.getNickname());
                    if(ObjectUtil.isEmpty(user.getAliasName())){
                        user.setAliasName("匿名");
                    }
                    if(!ObjectUtil.isEmpty(userInfo.getHeadImgUrl())){
                        user.setAvatar(userInfo.getHeadImgUrl());
                    }
                    user.setSex(userInfo.getSex());
                    String pwd = SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(userInfo.getOpenid());
                    user.setPwdText(pwd);
                    user.setTenantId(DEFUALT_TENANT_ID);
                    user.setOrgId(DEFAULT_ORG_ID);
                    user.setUserType(9);
                    user.setStatus(1);
                    user.setDelFlag(0);
                    user.setCreatedBy(user.getId());
                    user.setLastUpdatedBy(user.getId());
                    dataBaseDao.insertWithId(user);

                    if (!ObjectUtil.isEmpty(DEFAULT_ROLES)) {
                        for (String role : DEFAULT_ROLES.trim().split(",")) {
                            if (StringUtils.isBlank(role)) {
                                continue;
                            }
                            SysUserRole userRole = new SysUserRole();
                            userRole.setTenantId(user.getTenantId());
                            userRole.setUserId(user.getId());
                            userRole.setRoleId(Long.parseLong(role));
                            userRole.setEnDilivery(0);
                            userRole.setCreatedBy(user.getId());
                            userRole.setLastUpdatedBy(user.getId());
                            dataBaseDao.insertWithId(userRole);
                        }
                    }
                }

                // 保存绑定帐号信息
                if(tmp==null){
                    bind.setPlatData(JsonUtil.toJson(userInfo));
                    bind.setUnionId(userInfo.getUnionId());
                    bind.setTenantId(user.getTenantId());
                    bind.setUserId(user.getId());
                    bind.setCreatedBy(user.getId());
                    bind.setLastUpdatedBy(user.getId());
                    dataBaseDao.insert(bind);
                }else{
                    tmp.setPlatData(JsonUtil.toJson(userInfo));
                    tmp.setUnionId(userInfo.getUnionId());
                    tmp.setTenantId(user.getTenantId());
                    tmp.setUserId(user.getId());
                    tmp.setLastUpdatedBy(user.getId());
                    dataBaseDao.updateById(SqlBuilder.build(tmp).field("platData,unionId,tenantId,userId"));
                }
            }

        } catch (WxErrorException e) {
            log.error("获取微信授权信息失败", e);
            throw new ServiceException("获取微信授权信息失败");
        } catch (Exception e) {
            log.error("微信授权认证失败", e);
            throw new ServiceException("微信授权认证失败",e);
        }
        return user;
    }

}
