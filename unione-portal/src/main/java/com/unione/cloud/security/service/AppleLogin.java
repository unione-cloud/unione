package com.unione.cloud.security.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.model.SysUserBind;
import com.unione.cloud.system.model.SysUserRole;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.security.DefaultJwkParserBuilder;
import io.jsonwebtoken.io.ParserBuilder;
import io.jsonwebtoken.security.JwkParserBuilder;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@Slf4j
@Service
public class AppleLogin {


    @Autowired
    private DataBaseDao dataBaseDao;

    @Value("${security.bind.defaultTenantId:-1}")
	private Long DEFUALT_TENANT_ID;

	@Value("${security.bind.defaultOrgId:-1}")
	private Long DEFAULT_ORG_ID;

	@Value("${security.bind.defaultRoles:}")
	private String DEFAULT_ROLES;

    // 苹果开发者账号中配置的 Client ID（服务端 ID）
    @Value("${apple.serviceId:}")
    private String APPLE_SERVICE_ID;
    // Apple 公钥获取地址
    private final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证 Identity Token 的有效性（推荐方式）
     * @param identityToken 前端传入的 Identity Token
     * @param clientId 你的苹果 Client ID
     * @return 验证结果
     */
    public boolean verifyIdentityToken(String identityToken, String clientId) {
        try {
            if (StringUtils.isEmpty(identityToken)) {
                return false;
            }

            // 解析JWT token获取header中的kid
            String[] parts = identityToken.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String kid = headerNode.get("kid").asText();

            // 获取Apple公钥
            JsonNode keysNode = getApplePublicKeys();
            PublicKey publicKey = findPublicKey(keysNode, kid);
            
            if (publicKey == null) {
                log.error("未找到对应的Apple公钥, kid: {}", kid);
                return false;
            }

            // 验证JWT
            JwtParser parser = Jwts.parser().setSigningKey(publicKey).build();
            
            Jws<Claims> claims = parser.parseSignedClaims(identityToken);
            Claims body = claims.getPayload();
            
            // 验证aud是否匹配
            String aud = (new ArrayList<>(body.getAudience())).get(0);
            if (!clientId.equals(aud)) {
                log.error("aud不匹配, 期望: {}, 实际: {}", clientId, aud);
                return false;
            }

            // 验证iss是否为Apple
            String iss = body.getIssuer();
            if (!"https://appleid.apple.com".equals(iss)) {
                log.error("iss不匹配, 期望: https://appleid.apple.com, 实际: {}", iss);
                return false;
            }

            // 验证exp是否过期
            Long exp = body.getExpiration().getTime();
            Long now = System.currentTimeMillis();
            if (exp < now) {
                log.error("token已过期, exp: {}, now: {}", exp, now);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Identity Token 验证失败", e);
            throw new ServiceException("Identity Token 验证失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取Apple公钥
     */
    private JsonNode getApplePublicKeys() throws Exception {
        String response = restTemplate.getForObject(APPLE_PUBLIC_KEYS_URL, String.class);
        return objectMapper.readTree(response);
    }

    /**
     * 根据kid查找对应的公钥
     */
    private PublicKey findPublicKey(JsonNode keysNode, String kid) throws Exception {
        JsonNode keys = keysNode.get("keys");
        for (JsonNode key : keys) {
            if (kid.equals(key.get("kid").asText())) {
                String n = key.get("n").asText();
                String e = key.get("e").asText();
                return convertToPublicKey(n, e);
            }
        }
        return null;
    }

    /**
     * 将n和e转换为RSA公钥
     */
    private PublicKey convertToPublicKey(String n, String e) throws Exception {
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
        
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }


    /**
     * 微信登录
     * @param code
     * @return
     */
    public SysUser login(String code) {
        AppleOauthDto oauthDto = JsonUtil.toBean(AppleOauthDto.class, code);
        AssertUtil.service()
            .notNull(oauthDto,new String[]{"openid","identityToken"}, "属性%s不能为空")
            .isTrue(!ObjectUtil.isEmpty(APPLE_SERVICE_ID), "Apple Service ID 不能为空")
            .isTrue(verifyIdentityToken(oauthDto.getIdentityToken(), APPLE_SERVICE_ID), "Identity Token 验证失败");

        SysUser user = null;
        try {
            SysUserBind bind = new SysUserBind();
            bind.setPlatKey("apple");
            bind.setOpenId(oauthDto.getOpenid());
            SysUserBind tmp = dataBaseDao.findOne(SqlBuilder.build(bind));
            if (tmp != null) {
                // 已绑定
                user = dataBaseDao.findOne(SqlBuilder.build(SysUser.class).where("id", tmp.getUserId()));
            } else {
                // 首次绑定,自动创建帐号
                user = dataBaseDao.findOne(SqlBuilder.build(SysUser.class).where("username", oauthDto.getOpenid()));
                if (user == null) {
                    user = new SysUser();
                    user.setPwdSalt(RandomUtil.randomString(16));
                    user.setId(IdGenHolder.generate());
                    user.setUsername(oauthDto.getOpenid());
                    user.setAliasName(oauthDto.getRealName());
                    user.setRealName(oauthDto.getRealName());
                    user.setAliasName(oauthDto.getNickName());
                    if(ObjectUtil.isEmpty(user.getAliasName())){
                        user.setAliasName(oauthDto.getRealName());
                    }

                    String pwd = SmUtil.sm4(user.getPwdSalt().getBytes()).encryptHex(oauthDto.getOpenid());
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
                bind.setPlatData(code);
                bind.setTenantId(user.getTenantId());
                bind.setUserId(user.getId());
                bind.setCreatedBy(user.getId());
                bind.setLastUpdatedBy(user.getId());
                dataBaseDao.insert(bind);
            }
        } catch (Exception e) {
            log.error("Apple授权认证失败", e);
            throw new ServiceException("Apple授权认证失败",e);
        }
        return user;
    }


    @Data
    public static class AppleOauthDto{
        @Schema(title = "OPENID")
        private String openid;

        @Schema(title = "真实姓名")
        private String realName;

        @Schema(title = "昵称")
        private String nickName;

        @Schema(title = "邮箱")
        private String email;

        @Schema(title = "验证状态")
        private String verifyStatus;

        @Schema(title = "真实用户状态",description = "标识用户是否为真实的人 0：当前平台不支持，忽略该值；1：无法确认；2：用户真实性非常高")
        private String realUserStatus;

        @Schema(title = "访问令牌")
        private String accessToken;

        @Schema(title = "身份令牌")
        private String identityToken;

    }



}
