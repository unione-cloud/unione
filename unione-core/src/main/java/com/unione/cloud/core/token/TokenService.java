package com.unione.cloud.core.token;

import java.net.URI;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.util.RequestUtils;
import com.unione.cloud.core.util.RequestUtils.ClientLocation;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.SM4;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Token SessionService
 * 1、验证token（本地）
 * 2、验证token（redis）
 * 3、解析token信息
 */
@Slf4j
@Service
@RefreshScope
public class TokenService {

	/**
	 * 请求信息token名称
	 */
	@Value("${security.jwt.token:token}")
	private String REQUEST_TOKEN;

	@Value("${security.jwt.expires:3600}")
	private Integer JWT_EXPIRES;

	/**
	 * jwt issued 偏移量，单位秒，默认null
	 */
	@Value("${security.jwt.issued.offset:}")
	private Integer JWT_ISSUED_OFFSET;

	@Value("${security.jwt.issuer:unione-cloud}")
	private String JWT_ISSUER;

	/**
	 * Token Center Manage 令牌中心化管理，redis 数据库，默认：10
	 */
	@Value("${security.tcm.db:10}")
	private int tcmDb;

	/**
	 * Token Center Manage 令牌中心化管理，key前缀
	 */
	@Value("${security.tcm.key:TOKEN}")
	private String tcmKey;

	/**
	 * Token Center Manage 令牌中心化管理，自动刷新（统一在gateway中开启，其他服务中关闭）
	 */
	@Value("${security.tcm.autoEnable:false}")
	private boolean tcmAutoEnable;

	/**
	 * Token Center Manage 令牌中心化管理，token自动续期时间（默认：token过期前10分钟）
	 */
	@Value("${security.tcm.lifetime:10}")
	private int tcmAutoLiteTime;

	/**
	 * Token Center Manage 令牌中心化管理，ip验证，令牌ip验证，开启后，验证令牌ip和请求ip是否一致，如果不一致拒绝访问
	 */
	@Value("${security.tcm.ipVerify:true}")
	private boolean ipVerify;

	/**
	 * Redis 服务
	 */
	@Autowired(required = false)
	private RedisService redisService;

	private SM4 sm4;

	@Value("${security.sm4.key:hTXU0apMnNy38q1zb65FlQ==}")
	public void setKey(String key) {
		sm4 = SmUtil.sm4(Base64.decode(key.getBytes()));
	}


	/**
	 * 
	 * @param principal
	 * @return
	 */
	public String transform(UserPrincipal principal, String password) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(password);
			Map<String, Object> header = new HashMap<>();
			header.put("typ", "JWT");
			header.put("alg", "HS256");

			if(RequestContextHolder.getRequestAttributes()!=null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes){
				HttpServletRequest request=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
				String referer=request.getHeader("referer");
				String ctx="portal";
				try {
					String path = new URI(referer).getPath();
					if (path != null && !path.isEmpty()){
						String[] parts = path.split("/");
						for (String p : parts) {
							if (!p.isEmpty()) {
								ctx=p;
								break;
							}
						}
					}
				} catch (Exception e) {
				}
				if(ObjectUtil.isEmpty(ctx) || ctx.equals("/") || ctx.equals("login")){
					ctx="portal";
				}
				if(ctx.startsWith("/")){
					ctx=ctx.substring(1);
				}
				principal.setSysctx(ctx);
			}

			// 获得有效时间
			Calendar ca = Calendar.getInstance();
			Date issued = ca.getTime();
			if (JWT_ISSUED_OFFSET != null) {
				Calendar ica = Calendar.getInstance();
				ica.add(Calendar.SECOND, JWT_ISSUED_OFFSET);
				issued = ica.getTime();
			}
			ca.add(Calendar.SECOND, JWT_EXPIRES);
			Date expires = ca.getTime();
			ObjectMapper mapper = new ObjectMapper();
			com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
					.withHeader(header)
					.withExpiresAt(expires)
					.withIssuedAt(issued)
					.withIssuer(JWT_ISSUER)
					.withSubject(principal.getUsername())
					.withClaim("principal", mapper.writeValueAsString(principal));

			return builder.sign(algorithm);
		} catch (Exception e) {
			log.error("token生成失败,user name:{},id:{}", principal.getUsername(), principal.getId(), e);
			throw new ServiceException("token生成失败", e);
		}
	}

	/**
	 * 根据 principal 生成token
	 * 
	 * @param principal
	 * @return
	 */
	public String build(UserPrincipal principal) {
		String password = RandomUtil.randomString(20);
		return this.build4auth(principal, password);
	}

	/**
	 * 根据 principal 生成token
	 * 
	 * @param principal
	 * @return
	 */
	public String build4auth(UserPrincipal principal, String password) {
		// 生成token
		String token = this.transform(principal, password);

		// redis 存放token
		TcmEntry tcm = TcmEntry.builder()
				.token(token)
				.type("user")
				.tenantId(principal.getTenantId())
				.orgId(principal.getOrgId())
				.userId(principal.getId())
				.userName(principal.getUsername())
				.password(password)
				.times(DateUtil.current())
				.build();
		if(RequestContextHolder.getRequestAttributes()!=null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes){
			HttpServletRequest request=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			tcm.setOs(RequestUtils.getClientOs(request));
			tcm.setDevice(RequestUtils.getClientExplorer(request));
			tcm.setIpAddr(RequestUtils.getClientIp(request));
			ClientLocation location=RequestUtils.getClientLocation(request);
			if(!ObjectUtil.isEmpty(location.getCity())){
				tcm.setIpCity(String.format("%s/%s/%s", location.getCountry(), location.getProvince(), location.getCity()));
			}
		}

		// token 签名处理
		String tmp[] = this.signature(principal, token);
		token=tmp[0];
		tcm.setId(tmp[1]);
		principal.setSessionId(tmp[1]);

		this.redisService.put(tcmDb, String.format("%s:%s:%s:%s", tcmKey,principal.getTenantId(), principal.getUsername(), tmp[1]), tcm,
				Duration.ofSeconds(JWT_EXPIRES - 30));
		SessionHolder.setToken(token);
		SessionHolder.setUserPrincipal(principal);

		// 设置cookie
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletResponse response = attributes.getResponse();
			if (response != null) {
				Cookie ck = new Cookie(REQUEST_TOKEN, token);
				ck.setPath("/");
				response.addCookie(ck);
			}
		}

		return token;
	}


	public void clean4auth(UserPrincipal principal) {
		String token=String.format("%s@%s@%s", principal.getTenantId(), principal.getUsername(), principal.getSessionId());
		this.clean4auth(token);
	}

	/**
	 * 用户注销，清理token
	 * 
	 * @param token
	 */
	public void clean4auth(String token) {
		if (!StringUtils.isEmpty(token)) {
			try {
				if(!token.contains("@")){
					token=sm4.decryptStr(token);
				}
				String tinfo[]=token.split("@");
				String tenantId=tinfo[0];
				String username = tinfo[1];
				String md5=tinfo[2];
				this.redisService.delete(tcmDb, String.format("%s:%s:%s:%s", tcmKey,tenantId, username, md5));
			} catch (Exception e) {
				log.error("用户注销异常，token非法", e);
			}
		}

		// 清空cookie
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletResponse response = attributes.getResponse();
			if (response != null) {
				Cookie ck = new Cookie(REQUEST_TOKEN, null);
				ck.setPath("/");
				ck.setMaxAge(0);
				response.addCookie(ck);
			}
		}
	}

	/**
	 * 获取当前用户tcm对象
	 * 
	 * @param token
	 * @return
	 */
	public TcmEntry getTcm(String token) {
		if (!StringUtils.isEmpty(token)) {
			try {
				if(!token.contains("@")){
					token=sm4.decryptStr(token);
				}
				String tinfo[]=token.split("@");
				String tenantId=tinfo[0];
				String username = tinfo[1];
				String md5=tinfo[2];
				TcmEntry tcm = this.redisService.getObj(tcmDb, String.format("%s:%s:%s:%s", tcmKey,tenantId, username, md5));
				if (tcm == null) {
					log.info("中心化管理token，从redis中获取令牌失败，db:{} - key:{}:{}:{}", tcmDb, tcmKey,tenantId, username, md5);
				}
				return tcm;
			} catch (Exception e) {
				log.error("	获取当前用户tcm对象失败,token:{}", token, e);
			}
		}
		return null;
	}

	/**
	 * 刷新token
	 * 
	 * @param token
	 * @return
	 */
	public String refresh(String token) {
		String newToken = null;

		// 获取tcm对象
		TcmEntry tcm = this.getTcm(token);
		if (tcm == null) {
			return null;
		}

		// 1、验证token
		UserPrincipal principal = this.toPrincipal(token, tcm);

		// 2、生成token
		if (principal != null) {
			newToken = this.build4auth(principal, tcm.getPassword());
		}

		return newToken;
	}

	/**
	 * 验证token并获取UserPrincipal信息
	 * 
	 * @param token
	 * @return
	 */
	public UserPrincipal toPrincipal(String token) {
		TcmEntry tcm = this.getTcm(token);
		if (tcm == null) {
			log.info("当前令牌非法或已失效,token:{}", token);
			return null;
		}
		return this.toPrincipal(tcm.getToken(), tcm);
	}

	/**
	 * 解析token并转换成用户认证凭证对象
	 * @param token
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected UserPrincipal resolveToken(String token) {
		try{
			DecodedJWT jwt = JWT.decode(token);
			// 解析payload
			String payload = jwt.getPayload();
			if (StringUtils.isEmpty(payload)) {
				log.error("用户信息不能为空,token:{}", token);
				return null;
			}
	
			// 获得用户信息json
			String json = Base64.decodeStr(payload, "UTF-8");
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(json, Map.class);
			if (ObjectUtil.isEmpty(map.get("principal"))) {
				log.error("token信息异常，principal不能为空,token:{}", token);
				return null;
			}
	
			// 转换成用户认证凭证对象
			return mapper.readValue(map.get("principal").toString(), UserPrincipal.class);
		}catch(Exception e){
			log.error("解析token异常,token:{}",token,e);
		}
		return null;
	}

	/**
	 * 验证token并获取UserPrincipal信息
	 * 
	 * @param token
	 * @return
	 */
	private UserPrincipal toPrincipal(String token, TcmEntry tcm) {
		log.debug("进入服务:验证token并获取UserPrincipal信息,token:{}", token);
		AssertUtil.service().notNull(token, "token不能为空")
				.notNull(tcm, "tcm对象为空");
		UserPrincipal principal = null;
		String origToken = token;

		if(ipVerify){
			if(RequestContextHolder.getRequestAttributes()!=null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes){
				HttpServletRequest request=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
				if(!tcm.getIpAddr().equals(RequestUtils.getClientIp(request))){
					String key=String.format("%s:%s:%s:%s", tcmKey,tcm.getTenantId(), tcm.getUserName(), tcm.getId());
					log.warn("中心化管理token，从redis中获取令牌失败，ip验证不通过，db:{} - key:{},token ip:{},request ip:{}", tcmDb, key, 
						tcm.getIpAddr(), RequestUtils.getClientIp(request));
					return null;
				}
			}
		}

		try {
			JWTVerifier jwtv = JWT.require(Algorithm.HMAC256(tcm.getPassword())).build();
			DecodedJWT jwt = null;

			try {
				jwt = jwtv.verify(token);
			} catch (Exception e) {
				String uri = null;
				ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
						.getRequestAttributes();
				if (attributes != null) {
					HttpServletRequest request = attributes.getRequest();
					if (request != null) {
						uri = request.getRequestURI();
					}
				}
				log.error("token验证失败，uri:{},token:{}", uri, token, e);
				return null;
			}

			// 解析token并转换成用户认证凭证对象
			principal = this.resolveToken(token);
			if (principal == null) {
				return null;
			}

			// 如果是开启了自动续期，则在token过期前自动续期
			if (tcmAutoEnable) {
				long timelife = jwt.getExpiresAt().getTime() - System.currentTimeMillis();
				if (timelife <= (tcmAutoLiteTime * 60 * 1000)) {
					String newToken = transform(principal, tcm.getPassword());
					log.info("token中心化管理，token即将过期，剩余时间:{}ms，自动续期", timelife);
					tcm = TcmEntry.builder()
							.id(tcm.getId())
							.type(tcm.getType())
							.token(newToken)
							.tenantId(principal.getTenantId())
							.userId(principal.getId())
							.userName(principal.getUsername())
							.times(DateUtil.current())
							.build();
					if(RequestContextHolder.getRequestAttributes()!=null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes){
						HttpServletRequest request=((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
						tcm.setOs(RequestUtils.getClientOs(request));
						tcm.setDevice(RequestUtils.getClientExplorer(request));
						tcm.setIpAddr(RequestUtils.getClientIp(request));
						ClientLocation location=RequestUtils.getClientLocation(request);
						if(!ObjectUtil.isEmpty(location.getCity())){
							tcm.setIpCity(String.format("%s/%s/%s", location.getCountry(), location.getProvince(), location.getCity()));
						}
					}
					this.redisService.put(tcmDb, String.format("%s:%s:%s:%s", tcmKey,principal.getTenantId(), principal.getUsername(), origToken),
							tcm, Duration.ofSeconds(JWT_EXPIRES - 30));
				}
			}

			principal.setSessionId(tcm.getId());
		} catch (Exception e) {
			principal = null;
			log.error("验证token并获取UserPrincipal信息失败,token:{}", token, e);
		} finally {
			log.debug("退出服务:验证token并获取UserPrincipal信息,token:{},principal:{}", token, principal);
		}
		
		return principal;
	}

	/**
	 * token MD5签名
	 * 
	 * @param token
	 * @return [sign, md5]
	 */
	private String[] signature(UserPrincipal principal, String token) {
		String md5=DigestUtil.md5Hex(token);
		token = String.format("%s@%s@%s",principal.getTenantId(), principal.getUsername(), md5);
		return new String[]{sm4.encryptHex(token), md5};
	}

	public String decrypt(String token) {
		return sm4.decryptStr(token);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Accessors(chain = true)
	public static class TcmEntry {
		private String id;
		/**
		 * 令牌类型：user:用户令牌,server:服务令牌
		 */
		private String type;
		/**
		 * 真实令牌
		 */
		private String token;
		/**
		 * 租户id
		 */
		private Long tenantId;
		/**
		 * 机构id
		 */
		private Long orgId;
		/**
		 * 用户id
		 */
		private Long userId;
		/**
		 * 用户名称
		 */
		private String userName;
		/**
		 * 创建时间：毫秒
		 */
		private Long times;
		/**
		 * 用户密码（加密）
		 */
		private String password;

		/**
		 * 操作系统
		 */
		private String os;

		/**
		 * 设备信息
		 */
		private String device;

		/**
		 * ip地址
		 */
		private String ipAddr;

		/**
		 * ip城市
		 */
		private String ipCity;
	}
	
}