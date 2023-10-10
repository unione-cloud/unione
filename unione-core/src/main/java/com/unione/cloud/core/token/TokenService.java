package com.unione.cloud.core.token;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.SM4;
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
public class TokenService{
	
	/**
	 * 	请求信息token名称
	 */
	@Value("${security.jwt.token:token}")
	private String REQUEST_TOKEN;
	
	@Value("${security.jwt.secret:com.unione.cloud.token}")
    private String JWT_SECRET;

    @Value("${security.jwt.expires:3600}")
    private Integer JWT_EXPIRES;
    
    /**
     * 	jwt issued 偏移量，单位秒，默认null
     */
    @Value("${security.jwt.issued.offset:}")
    private Integer JWT_ISSUED_OFFSET;
    

    @Value("${security.jwt.issuer:mins-token}")
    private String JWT_ISSUER;
    
    /**
     * 	自动刷新开关（统一在gateway中开启，其他服务中关闭）
     */
    @Value("${security.jwt.autoEnable:true}")
    private boolean autoEnable;
    
    /**
     * 	token自动续期时间（默认：token过期前10分钟）
     */
    @Value("${security.jwt.autoTime:10}")
    private int autoLiteTime;

    /**
     * 	Token Center Manage 令牌中心化管理，redis 数据库，默认：10
     */
    @Value("${security.tcm.db:10}")
    private int     tcmDb;

    /**
     * 	Token Center Manage 令牌中心化管理，key前缀
     */
    @Value("${security.tcm.key:TOKEN}")
    private String tcmKey;
    
    
    /**
     * Redis 服务
     */
    @Autowired
    private RedisService redisService;
    
    private JWTVerifier jwtv;
    
    
    private SM4 sm4;
    @Value("${security.sm4.key:hTXU0apMnNy38q1zb65FlQ==}")
    public void setKey(String key) {
    	sm4=SmUtil.sm4(Base64.decode(key.getBytes()));
    }
    
    
    /**
     * @param principal
     * @return
     */
    public String transform(UserPrincipal principal) {
    	try {
			Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
			Map<String, Object> header=new HashMap<>();
			header.put("typ", "JWT");
			header.put("alg", "HS256");
			
			// 获得有效时间
			Calendar ca=Calendar.getInstance();
			Date issued=ca.getTime();
			if(JWT_ISSUED_OFFSET!=null) {
				Calendar ica=Calendar.getInstance();
				ica.add(Calendar.SECOND, JWT_ISSUED_OFFSET);
				issued=ica.getTime();
			}
			ca.add(Calendar.SECOND, JWT_EXPIRES);
			Date expires=ca.getTime();
			ObjectMapper mapper=new ObjectMapper();
			com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
				.withHeader(header)
				.withExpiresAt(expires)
				.withIssuedAt(issued)
				.withIssuer(JWT_ISSUER)
				.withSubject(principal.getUsername())
				.withClaim("principal", mapper.writeValueAsString(principal));
			
			return builder.sign(algorithm);
		} catch (Exception e) {
			log.error("token生成失败,user name:{},sid:{}",principal.getUsername(),principal.getId(),e);
			throw new ServiceException("token生成失败",e);
		}
    }
    
    
    /**
     * 	根据 principal 生成token
     * @param principal
     * @return
     */
    public String build(UserPrincipal principal) {
		String token=this.transform(principal);
		
		SessionHolder.setUserPrincipal(principal);
		SessionHolder.setToken(token);
		
		return token;
    }
    
    
    /**
     * 	根据 principal 生成token
     * @param principal
     * @return
     */
    public String build4auth(UserPrincipal principal) {
    	log.debug("进入服务:根据 principal 生成token,principal:{}",principal);
		// 生成token
    	String token = this.build(principal);
		
		// redis 存放token
    	TcmEntry tcm=TcmEntry.builder()
    			.token(token)
    			.tenantId(principal.getTenantId())
    			.userId(principal.getId())
    			.userName(principal.getUsername())
    			.times(DateUtil.current())
    			.build();
    	
    	// token 签名处理
    	token = this.signature(principal.getUsername(), token);
    	
        this.redisService.put(tcmDb,String.format("%s:%s:%s",tcmKey,principal.getUsername(),token),tcm,Duration.ofSeconds(JWT_EXPIRES-30));
        SessionHolder.setToken(token);
        
        // 设置cookie
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes!=null) {
			HttpServletResponse response = attributes.getResponse();
			if(response!=null) {
				Cookie ck=new Cookie(REQUEST_TOKEN, token);
				ck.setPath("/");
				response.addCookie(ck);
			}
		}
          	
    	log.debug("退出服务:根据 principal 生成token,principal:{},token:{}",principal,token);
    	return token;
    }
    
    /**
     * 	用户注销，清理token
     * @param token
     */
    public void clean4auth(String token) {
    	log.debug("用户注销，清理token:{}",token);
    	if(!StringUtils.isEmpty(token)){
    		if(token.length()<=120) {
    			try {
					String username=sm4.decryptStr(token).split("@")[0];
					log.debug("中心化管理token，从redis中删除，db:{} - {}:{}:{}",tcmDb,tcmKey,username,token);
					this.redisService.delete(tcmDb,String.format("%s:%s:%s",tcmKey,username,token));
				} catch (Exception e) {
					log.error("用户注销异常，token非法",e);
				}
    		}
		}
    	
    	// 清空cookie
    	ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes!=null) {
			HttpServletResponse response = attributes.getResponse();
			if(response!=null) {
				Cookie ck=new Cookie(REQUEST_TOKEN, null);
				ck.setPath("/");
				ck.setMaxAge(0);
				response.addCookie(ck);
			}
		}
    }
    
    
	/**
     * 	获取有效的认证token
     * @param token
     * @return	
     */
    public String getAuthToken(String token) {
    	if(!StringUtils.isEmpty(token) && token.length()<120){
    		try {
				String username=sm4.decryptStr(token).split("@")[0];
				log.info("中心化管理token，从redis中获取令牌，db:{} - {}:{}:{}",tcmDb,tcmKey,username,token);
				TcmEntry tcm=this.redisService.getObj(tcmDb,String.format("%s:%s:%s",tcmKey,username,token));
				if(tcm!=null) {
					token = tcm.getToken();
				}else {
					log.info("中心化管理token,当前token已失效或者注销:{}",token);
					return null;
				}
			} catch (Exception e) {
				log.error("	获取有效的认证token失败，token非法",e);
			}
		}
    	return token;
    }
    
    
    /**
     * 	判断token是否为中心化管理令牌
     * @param token
     * @return
     */
    public boolean isAuthToken(String token) {
    	if(!StringUtils.isEmpty(token) && token.length()<120){
    		try {
				String username=sm4.decryptStr(token).split("@")[0];
				log.info("中心化管理token，从redis中获取令牌，db:{} - {}:{}:{}",tcmDb,tcmKey,username,token);
				TcmEntry tcm=this.redisService.getObj(tcmDb,String.format("%s:%s:%s",tcmKey,username,token));
				if(tcm!=null) {
					return true;
				}else {
					log.info("中心化管理token,当前token已失效或者注销:{}",token);
				}
			} catch (Exception e) {
				log.error("判断token是否为中心化管理令牌失败，token非法",e);
			}
		}
    	return false;
    }
    
    
    /**
     * 	刷新token
     * @param token
     * @return
     */
    public String refresh(String token) {
    	log.debug("进入服务:刷新token,token:{}",token);
    	String newToken=null;
    	
		// 1、验证token
		UserPrincipal principal=this.toPrincipal(token);
		
		// 2、生成token
		if(principal!=null) {
			newToken=this.build(principal);
			//token中心化管理
			TcmEntry tcm=TcmEntry.builder()
					.token(newToken)
					.tenantId(principal.getTenantId())
					.userId(principal.getId())
					.userName(principal.getUsername())
					.times(DateUtil.current())
					.build();
			
			this.redisService.delete(tcmDb,String.format("%s:%s:%s",tcmKey,principal.getUsername(),token));			
			newToken = this.signature(principal.getUsername(), newToken);
            this.redisService.put(tcmDb,String.format("%s:%s:%s",tcmKey,principal.getUsername(),newToken),tcm,Duration.ofSeconds(JWT_EXPIRES-30));
			
			// 设置cookie
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if(attributes!=null) {
				HttpServletResponse response = attributes.getResponse();
				if(response!=null) {
					Cookie ck=new Cookie(REQUEST_TOKEN, newToken);
					ck.setPath("/");
					response.addCookie(ck);
				}
			}
		}
    	
    	log.debug("进入服务:刷新token,token:{},newToken:{}",token,newToken);
    	return newToken;
    }
    
	/**
	 * 	验证token并获取UserPrincipal信息
	 * @param token
	 * @return
	 */
    @SuppressWarnings("unchecked")
	public UserPrincipal toPrincipal(String token) {
    	log.debug("进入服务:验证token并获取UserPrincipal信息,token:{}",token);
    	AssertUtil.service().notNull(token, "token不能为空");
    	UserPrincipal principal=null;
    	String origToken=token;
    	
		// token中心化管理，token长度大于120则是未进行token签名的原生jwt令牌
    	if(token.length()<120){
    		try {
				String username=sm4.decryptStr(token).split("@")[0];
				TcmEntry tcm=this.redisService.getObj(tcmDb,String.format("%s:%s:%s",tcmKey,username,token));
				if(tcm!=null) {
					token = tcm.getToken();
				}else {
					log.info("中心化管理token,当前token已失效或者注销:{}",token);
					return null;
				}
				log.debug("开启token中心化管理，真实token:{}",token);
			} catch (Exception e) {
				log.error("验证token并获取UserPrincipal信息失败，token非法,token:{}",token,e);
			}
		}
		
    	try {
	    	if(jwtv==null) {
	    		jwtv = JWT.require(Algorithm.HMAC256(JWT_SECRET)).build();
	    	}
	        DecodedJWT jwt = null;
	        try {
	            jwt=jwtv.verify(token);
	        } catch (Exception e) {
	        	String uri=null;
	        	ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
	        	if(attributes!=null) {
	        		HttpServletRequest request = attributes.getRequest();
	        		if(request!=null) {
	        			uri = request.getRequestURI();
	        		}
	        	}
	            log.error("token验证失败，uri:{},token:{}",uri,token,e);
	            return null;
	        }
	       
	        // 解析payload
	        String payload = jwt.getPayload();
	        if(StringUtils.isEmpty(payload)){
	            log.error("token信息异常，payload不能为空,token:{}",token);
	            return null;
			}
			
			// 获得用户信息json
	        String json=Base64.decodeStr(payload, "UTF-8");
	
	        ObjectMapper  mapper = new ObjectMapper();
            Map<String,Object> map=mapper.readValue(json, Map.class);
            if(ObjectUtil.isEmpty(map.get("principal"))) {
            	log.error("token信息异常，principal不能为空,token:{}",token);
 	            return null;
            }
            
            // 转换成用户认证凭证对象
            principal=mapper.readValue(map.get("principal").toString(), UserPrincipal.class);
            
            // 如果是开启了自动续期，则在token过期前自动续期
            if(autoEnable){
            	long timelife = jwt.getExpiresAt().getTime()-System.currentTimeMillis();
            	if(timelife<=(autoLiteTime*60*1000)) {
            		String newToken=transform(principal);
            		log.info("token中心化管理，token即将过期，剩余时间:{}ms，自动续期",timelife);
            		TcmEntry tcm=TcmEntry.builder()
    	        			.token(newToken)
    	        			.tenantId(principal.getTenantId())
    	        			.userId(principal.getId())
    	        			.userName(principal.getUsername())
    	        			.times(DateUtil.current())
    	        			.build();
            		this.redisService.put(tcmDb,String.format("%s:%s:%s", tcmKey,principal.getUsername(),origToken),tcm,Duration.ofSeconds(JWT_EXPIRES-30));
            	}
            }
            
        } catch (Exception e) {
        	principal=null;
            log.error("验证token并获取UserPrincipal信息失败,token:{}",token,e);
        } finally {
        	log.debug("退出服务:验证token并获取UserPrincipal信息,token:{},principal:{}",token,principal);
        }

        return principal;
    }
    
    
    private String signature(String username,String token) {
    	token=String.format("%s@%s",username,DigestUtil.md5Hex(token));
    	return sm4.encryptBase64(token);
    }

    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class TcmEntry{
    	/**
    	 * 真实令牌
    	 */
    	private String token;
    	/**
    	 * 租户id
    	 */
    	private Long   tenantId;
    	/**
    	 * 用户id
    	 */
    	private Long   userId;
    	/**
    	 * 用户名称
    	 */
    	private String userName;
    	/**
    	 * 创建时间：毫秒
    	 */
    	private Long   times;
    }
    
    
    
}