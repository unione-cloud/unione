package com.unione.cloud.core.token;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.codec.binary.Base64;
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
	
	@Value("${security.jwt.secret:com.aifa.mins.token}")
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
	 * 	Token Center Manage 令牌中心化管理:开关，默认关闭
	 */
    @Value("${security.tcm.enable:false}")
	private boolean tcmEnable;
    
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
     * 	Token Center Manage 令牌中心化管理，自动刷新（统一在gateway中开启，其他服务中关闭）
     */
    @Value("${security.tcm.autoEnable:false}")
    private boolean tcmAutoEnable;
    
    /**
     * 	Token Center Manage 令牌中心化管理，token自动续期时间（默认：token过期前10分钟）
     */
    @Value("${security.tcm.lifetime:10}")
    private int tcmAutoLiteTime;
    
    
    /**
     * Redis 服务
     */
    @Autowired(required=false)
    private RedisService redisService;
    
    private JWTVerifier jwtv;
    
    
    public TokenService() {
		ConvertUtils.register(new Converter() {
			private SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd");
			private SimpleDateFormat format2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			private SimpleDateFormat format3=new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			@Override
			@SuppressWarnings("unchecked")
			public <T> T convert(Class<T> type, Object value) {
				if(value!=null) {
					if(value instanceof Date || value.getClass().equals(type)) {
						return (T)value;
					}
					if(value instanceof java.sql.Date) {
						java.sql.Date sdate=(java.sql.Date)value;
						return (T)new Date(sdate.getTime());
					}
					if(value instanceof String) {
						String str=StringUtils.trim(value.toString());
						try {
							if(str.length()==10) {
								// format=yyyy-MM-dd
								return (T)format1.parse(str);
							}
							if(str.length()==19) {
								// format=yyyy-MM-dd HH:mm:ss
								return (T)format2.parse(str);
							}
							return (T)format3.parse(str);
						} catch (ParseException e) {
							log.error("Date Parse Error,source:{}",str,e);
						}
					}
				}
				return null;
			}
		}, Date.class);
    }
    
    
    
    /**
     * 	
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
				.withClaim("sid", principal.getSid())
				.withClaim("orgId", principal.getOrgId())
				.withClaim("tenantId", principal.getTenantId())
				.withClaim("orgName", principal.getOrgName())
				.withClaim("username", principal.getUsername())
				.withClaim("realName", principal.getRealName())
				.withClaim("aliasName", principal.getAliasName())
				.withClaim("photo", principal.getPhoto())
				.withClaim("type", principal.getType())
				.withClaim("status", principal.getStatus())
				.withClaim("lastLoginIp", principal.getLastLoginIp())
				.withClaim("totalLoginCount", principal.getTotalLoginCount())
				.withClaim("attr", mapper.writeValueAsString(principal.getAttr()))
				.withArrayClaim("userRoles", principal.getUserRoles().toArray(new Long[principal.getUserRoles().size()]));
			
			if(principal.getLastLoginTime()!=null) {
				builder.withClaim("lastLoginTime", principal.getLastLoginTime().getTime());
			}
			
			return builder.sign(algorithm);
		} catch (Exception e) {
			log.error("token生成失败,user name:{},sid:{}",principal.getUsername(),principal.getSid(),e);
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
    	log.debug("进入服务:根据 principal 生成token,tcmEnable:{},principal:{}",tcmEnable,principal);
		// 生成token
    	String token = this.build(principal);
		
		String origToken=token;
		// redis 存放token
        if(tcmEnable){
        	token = this.signature(origToken);
        	TcmEntry tcm=TcmEntry.builder()
        			.token(origToken)
        			.tenantId(principal.getTenantId())
        			.userId(principal.getSid())
        			.userName(principal.getUsername())
        			.build();
            this.redisService.put(tcmDb,tcmKey+":"+token,tcm,Duration.ofSeconds(JWT_EXPIRES-30));
            SessionHolder.setToken(token);
		}
    	
    	log.debug("退出服务:根据 principal 生成token,tcmEnable:{},principal:{},token:{}",tcmEnable,principal,token);
    	return token;
    }
    
    /**
     * 	用户注销，清理token
     * @param token
     */
    public void clean4auth(String token) {
    	log.debug("用户注销，tcmEnable:{},清理token:{}",tcmEnable,token);
    	if(!StringUtils.isEmpty(token) && tcmEnable){
    		if(token.length()>=100) {
    			token = signature(token);
    		}
    		log.debug("中心化管理token，从redis中删除，db:{} - {}:{}",tcmDb,tcmKey,token);
            this.redisService.delete(tcmDb,tcmKey+":"+token);
		}
    }
    
    /**
     * 	获取有效的认证token
     * @param token
     * @return	
     */
    public String getAuthToken(String token) {
    	if(!StringUtils.isEmpty(token) && tcmEnable && token.length()<100){
    		log.info("中心化管理token，从redis中获取令牌，db:{} - {}:{}",tcmDb,tcmKey,token);
    		TcmEntry tcm=this.redisService.getObj(tcmDb,tcmKey+":"+token);
    		if(tcm!=null) {
    			token = tcm.getToken();
    		}else {
    			log.info("中心化管理token,当前token已失效或者注销:{}",token);
    			return null;
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
    	if(!StringUtils.isEmpty(token) && tcmEnable && token.length()<100){
    		log.info("中心化管理token，从redis中获取令牌，db:{} - {}:{}",tcmDb,tcmKey,token);
    		TcmEntry tcm=this.redisService.getObj(tcmDb,tcmKey+":"+token);
    		if(tcm!=null) {
    			return true;
    		}else {
    			log.info("中心化管理token,当前token已失效或者注销:{}",token);
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
			//token长度大于100则是未进行token签名的原生jwt令牌
			if(tcmEnable && token.length()<100){
				TcmEntry tcm=TcmEntry.builder()
	        			.token(newToken)
	        			.tenantId(principal.getTenantId())
	        			.userId(principal.getSid())
	        			.userName(principal.getUsername())
	        			.build();
	            this.redisService.put(tcmDb,tcmKey+":"+token,tcm,Duration.ofSeconds(JWT_EXPIRES-30));
	            newToken=token;
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
    	
		// token中心化管理，token长度大于100则是未进行token签名的原生jwt令牌
    	if(tcmEnable && token.length()<100){
    		TcmEntry tcm=this.redisService.getObj(tcmDb,tcmKey+":"+token);
    		if(tcm!=null) {
    			token = tcm.getToken();
    		}else {
    			log.info("中心化管理token,当前token已失效或者注销:{}",token);
    			return null;
    		}
			log.debug("开启token中心化管理，真实token:{}",token);
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
	            log.error("用户信息不能为空,token:{}",token);
	            return null;
			}
			
			// 获得用户信息json
	        String json=new String(Base64.decodeBase64(payload),"UTF-8");
	
	        ObjectMapper  mapper = new ObjectMapper();
            Map<String,Object> map=mapper.readValue(json, Map.class);
            if(map.get("attr")!=null) {
            	// 扩展属性信息处理
            	map.put("attr", mapper.readValue((String)map.get("attr"), Map.class));
            }
            if(map.get("userRoles")!=null) {
            	List<Object> rr=(List<Object>)map.get("userRoles");
            	List<Long> roles=new ArrayList<Long>();
            	for(Object r:rr) {
            		roles.add(Long.parseLong(r.toString()));
            	}
            	map.put("userRoles", roles);
            }
            if(map.get("lastLoginTime")!=null) {
            	map.put("lastLoginTime", new Date(Long.parseLong(map.get("lastLoginTime").toString())));
            }else {
            	map.remove("lastLoginTime");
            }
            principal=new UserPrincipal();
            BeanUtils.populate(principal, map);
            
            // 如果是中心化管理，则在token过期前自动续期
            if(tcmEnable && tcmAutoEnable && origToken.length()<100){
            	long timelife = jwt.getExpiresAt().getTime()-System.currentTimeMillis();
            	if(timelife<=(tcmAutoLiteTime*60*1000)) {
            		log.info("token中心化管理，token即将过期，剩余时间:{}ms，自动续期",timelife);
            		String newToken=transform(principal);
            		TcmEntry tcm=TcmEntry.builder()
    	        			.token(newToken)
    	        			.tenantId(principal.getTenantId())
    	        			.userId(principal.getSid())
    	        			.userName(principal.getUsername())
    	        			.build();
            		this.redisService.put(tcmDb,tcmKey+":"+origToken,tcm,Duration.ofSeconds(JWT_EXPIRES-30));
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

    /**
     * token MD5签名
     * @param token
     * @return
     */
    public String signature(String token) {
    	try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] resBytes = md.digest(token.getBytes());
			StringBuffer sBuffer = new StringBuffer();
	        for (int i = 0; i < resBytes.length; i++) {
	            sBuffer.append(byteToArrayString(resBytes[i]));
	        }
			return sBuffer.toString();
		} catch (Exception e) {
			log.error("token md5签名失败,token:{}",token,e);
		}
    	return null;
    }
    
	// 全局数组
    private final static String[] strDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    private String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class TcmEntry{
    	// 真实有效的令牌
    	private String token;
    	private Long   tenantId;
    	private Long   userId;
    	private String userName;
    }
}