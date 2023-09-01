package com.unione.cloud.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.UserPrincipal;
import com.unione.cloud.core.security.UserRoles;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Api 权限工具类
 * @author Jeking Yang
 * @version 1.0.0
 */
@Slf4j
@Service
@RefreshScope
public class ApiPermisUtil {
	
	private static AntPathMatcher matcher = new AntPathMatcher();
	
	private static RedisService  redisService;
	
	/**
	 * 	api 信息缓存{APIURL:[urli,url2],url1:{api},url2:{api}}
	 */
	private static Cache<String, Object> apiCache;
	/**
	 * 	url 信息缓存{reqUrl:{api}}
	 */
	private static Cache<String, Object> urlCache;
	/**
	 * 	用户 api 权限缓存
	 */
	private static Cache<String, List<Long>> userPermisCache;
	/**
	 *  角色	api 权限缓存
	 */
	private static Cache<String, List<String>> rolePermisCache;
	
	/**
	 * 	 api => {path:api}				map集合
	 */
	private static final String PERMIS_API_INFO="PERMIS:API:INFO";
	
	/**
	 * 	 api path => [userid1,userid2] 	set集合
	 */
	private static final String PERMIS_API_USER="PERMIS:API:USER:%s";
	
	/**
	 * 	 api path => [roleid1,roleid2]	set集合
	 */
	private static final String PERMIS_API_ROLE="PERMIS:API:ROLE:%s";
	
	
	@Autowired
	public void setRedisService(RedisService redisService) {
		ApiPermisUtil.redisService = redisService;
	}
	
	
	/**
	 * 	API 权限验证开关，默认关闭
	 */
	private static boolean API_PERMIS_ENABLE;
	@Value("${permis.api.enable:false}")
	public void setApiPermisEnable(boolean flag) {
		API_PERMIS_ENABLE=flag;
	}
	
	/**
	 * 	API 权限强验证开关，默认关闭
	 * 	开启强验证：则所有url都需要能找到对应的api权限记录，且拥有权限才可以访问
	 * 	未开启强验证：则，url未匹配到api信息，则视为该api不需要授权访问
	 */
	private static boolean API_PERMIS_POWER_ENABLE;
	@Value("${permis.api.powerEnable:false}")
	public void setApiPermisPowerEnable(boolean flag) {
		API_PERMIS_POWER_ENABLE=flag;
	}
	
	private static String ADMIN_USER;
	@Value("${security.administrator:administrator}")
	public void setAdminUser(String username) {
		ADMIN_USER=username;
	}
	
	/**
	 * 	设置api 缓存时间，单位秒
	 * @param time
	 */
	@Value("${permis.api.cache.apitime:500}")
	public void setApiCacheTime(int time) {
		apiCache=CacheBuilder.newBuilder().expireAfterWrite(time, TimeUnit.SECONDS).build();
	}
	
	/**
	 * 	设置请求url缓存时间，单位秒
	 * @param time
	 */
	@Value("${permis.api.cache.urltime:120}")
	public void setUrlCacheTime(int time) {
		urlCache=CacheBuilder.newBuilder().expireAfterWrite(time, TimeUnit.SECONDS).build();
	}
	/**
	 * 	设置api 权限缓存时间，单位秒
	 * @param time
	 */
	@Value("${permis.api.cache.permistime:300}")
	public void setApiPermisCacheTime(int time) {
		userPermisCache=CacheBuilder.newBuilder().expireAfterWrite(time, TimeUnit.SECONDS).build();
		rolePermisCache=CacheBuilder.newBuilder().expireAfterWrite(time, TimeUnit.SECONDS).build();
	}
	
	
	/**
	 * 	验证用户权限
	 * @param userid
	 * @param apiUrl
	 * @return
	 */
	private static boolean validUserPsermis(Long userid,String apiUrl) {
		log.debug("进入:验证用户api权限方法，user id:{},api url:{}",userid,apiUrl);
		List<Long> users=userPermisCache.getIfPresent("USER_PERMIS_"+apiUrl);
		if(users==null) {
			Set<Long> uids = redisService.getSet(String.format(PERMIS_API_USER, apiUrl));
			if(uids!=null && !uids.isEmpty()) {
				users = new ArrayList<>(uids);
				userPermisCache.put("USER_PERMIS_"+apiUrl, users);
			}
		}
		
		if(users==null) {
			log.warn("api url:{},用户权限集合为空",apiUrl);
		}
		
		boolean flag=false;
		if(users!=null && users.contains(userid)) {
			flag=true;
		}
		
		log.debug("退出:验证用户api权限方法，user id:{},api url:{},结果:{}",userid,apiUrl,flag);
		return flag;
	}
	
	
	/**
	 * 	验证角色权限
	 * @param roleIds
	 * @param apiUrl
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static boolean validRolePsermis(List<String> roleIds,String apiUrl) {
		log.debug("进入:验证角色api权限方法，role ids:{},api url:{}",roleIds,apiUrl);
		List<String> roles=rolePermisCache.getIfPresent("ROLE_PERMIS_"+apiUrl);
		if(roles==null) {
			Set<String> rids = redisService.getSet(String.format(PERMIS_API_ROLE, apiUrl));
			if(rids!=null && !rids.isEmpty()) {
				roles = new ArrayList<>(rids);
				rolePermisCache.put("ROLE_PERMIS_"+apiUrl, roles);
			}
		}
		
		if(roles==null) {
			log.warn("api url:{},角色权限集合为空",apiUrl);
		}
		
		boolean flag=false;
		if(roles!=null) {
			List<Long> list = ListUtils.retainAll(roles, roleIds);
			flag=list!=null&&!list.isEmpty();
		}
		
		log.debug("退出:验证角色api权限方法，role id:{},api url:{},结果:{}",roleIds,apiUrl,flag);
		return flag;
	}
	
    
	/**
	 * 	验证用户是否拥有该path反问权限
	 * @param principal
	 * @param url
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean validPsermis(UserPrincipal principal,String url) {
		log.debug("进入:验证用户是否拥有该url反问权限方法,permis enable:{},user id:{},user name:{},请求url:{}",API_PERMIS_ENABLE,principal.getId(),principal.getUsername(),url);
		if(!API_PERMIS_ENABLE) {
			return true;
		}
		log.debug("开始验证url权限");
		
		// 超级管理员、系统运维人员过滤
		if(ObjectUtil.equals(ADMIN_USER, principal.getUsername()) || principal.getUserRoles().contains(UserRoles.SUPPER_ADMIN.value()) ||
				principal.getUserRoles().contains(UserRoles.SYSOPS_USER.value())) {
			log.info("api权限验证:当前用户为超级管理员或系统运维人员，所有api接口都可以访问,user id:{},user name:{},请求url:{}",principal.getId(),principal.getUsername(),url);
			return true;
		}
		
		log.debug("获取api url集合");
		List<String> apiUrls = (List<String>)apiCache.getIfPresent("APIURL");
		if(apiUrls==null) {
			List<Object> values = redisService.getMapOps().values(PERMIS_API_INFO);
			if(values!=null && !values.isEmpty()) {
				apiUrls=new ArrayList<>();
				for(Object obj:values) {
					try {
						if(obj instanceof Map) {
							Map<String, Object> api=(Map<String, Object>)obj;
							Long apiId=(Long)api.get("sid");
							String apiTitle=(String)api.get("title");
							String ctx=(String)api.get("ctx");
							String apiUrl=(String)api.get("url");
							apiCache.put(ctx+apiUrl, api);
							log.debug("成功获取api信息，api id:{},title:{},ctx:{},url:{}",apiId,apiTitle,ctx,apiUrl);
							apiUrls.add(ctx+apiUrl);
						}else {
							log.error("api信息缓存对象异常，类型不一致！,redis key:{},obj:{}",PERMIS_API_INFO,obj);
						}
					} catch (Exception e) {
						log.error("api缓存数据处理异常,redis key:{},obj:{}",PERMIS_API_INFO,obj,e);
					}
				};
				apiUrls.sort((v1,v2)->{
					if(v1!=null) {
						return v1.compareTo(v2);
					}
					return -1;
				});
				apiCache.put("APIURL",apiUrls);
				log.debug("成功获取api信息,count:{}",apiUrls.size());
			}else{
				if(API_PERMIS_POWER_ENABLE==false) {
					log.warn("api权限集合缓存为空，未开启强验证，所有请求将放行,redis key:{},user id:{},user name:{},请求url:{}",PERMIS_API_INFO,principal.getId(),principal.getUsername(),url);
					return true;
				}
				log.warn("api权限集合缓存为空，开启强验证，所有请求将限制,redis key:{},user id:{},user name:{},请求url:{}",PERMIS_API_INFO,principal.getId(),principal.getUsername(),url);
				return false;
			}
		}
		log.debug("获取api url集合,api url count:{}",apiUrls.size());
		
		log.debug("根据请求url，获取api对象");
		boolean fundedApi=false;
		try {
			// 首先从本地缓存中匹配请求url->api信息
			String reqUriMd5="REQURL_"+SmUtil.sm3().digestHex(url);
			if(urlCache.getIfPresent(reqUriMd5)!=null) {
				fundedApi=true;
				Map<String, Object> api=(Map<String, Object>)urlCache.getIfPresent(reqUriMd5);
				Long apiId=(Long)api.get("sid");
				String apiTitle=(String)api.get("title");
				String ctx=(String)api.get("ctx");
				String apiUrl=(String)api.get("url");
				Object permised=api.get("permised");
				if(permised==null || ObjectUtil.equals(permised, 0)) {
					return true;
				}
				log.info("api url权限验证[本地缓存]: 用户user id:{},user name:{},请求url:{},api id:{},api url:{},api title:{}",principal.getId(),principal.getUsername(),url,apiId,apiUrl,apiTitle);
				
				// 根据用户角色验证权限
				boolean hadRolePermis = validRolePsermis(principal.getUserRoles(), ctx+apiUrl);
				if(hadRolePermis==false) {
					// 根据用户id验证权限
					boolean hadUserPermis = validUserPsermis(principal.getId(), ctx+apiUrl);
					AssertUtil.service().isTrue(hadUserPermis, "当前用户无该api访问权限");
				}
				return true;
			}else {
				
				// 从api url中验证权限
				fundedApi = apiUrls.stream().anyMatch(x -> {
					boolean flag=matcher.match(x, url);
					if(flag) {
						// 成功匹配url，根据url匹配api
						Map<String, Object> api=(Map<String, Object>)apiCache.getIfPresent(x);
						AssertUtil.service().notNull(api, "根据请求url成功匹配api url:"+x+",根据api url获取api信息失败");
						Long apiId=(Long)api.get("sid");
						String apiTitle=(String)api.get("title");
						Object permised=api.get("permised");
						if(!x.endsWith("/**")) {
							// 非前缀匹配，进行url缓存
							urlCache.put(reqUriMd5, api);
						}
						if(permised==null || ObjectUtil.equals(permised, 0)) {
							return true;
						}
						log.info("api url权限验证: 用户user id:{},user name:{},请求url:{},api id:{},api url:{},api title:{}",principal.getId(),principal.getUsername(),url,apiId,x,apiTitle);
						
						// 根据用户角色验证权限
						boolean hadRolePermis = validRolePsermis(principal.getUserRoles(), x);
						if(hadRolePermis==false) {
							// 根据用户id验证权限
							boolean hadUserPermis = validUserPsermis(principal.getId(), x);
							if(hadUserPermis==false && x.endsWith("/**")) {
								log.info("当前用户无前缀匹配url权限,继续匹配,user id:{},user name:{}api url:{},req url:{}",principal.getId(),principal.getUsername(),x,url);
								return false;
							}
							AssertUtil.service().isTrue(hadUserPermis, "当前用户无该api访问权限");
						}
						return true;
					}
					return false;
				});
			}
			
		} catch (ServiceException e) {
			log.warn("url权限验证失败，当前用户无权限,用户user id:{},user name:{},请求url:{},error:{}",principal.getId(),principal.getUsername(),url,e.getMessage());
			return false;
		} catch (Exception e) {
			log.warn("url权限处理失败,用户user id:{},user name:{},请求url:{}",principal.getId(),principal.getUsername(),url,e);
		}
		
		if(fundedApi == false && API_PERMIS_POWER_ENABLE==false) {
			log.warn("用户user id:{},user name:{},请求url:{},url匹配失败，未找到api信息，未开启权限强验证，当前请求：放行",principal.getId(),principal.getUsername(),url);
			return true;
		}
		
		log.debug("退出:验证用户是否拥有该url反问权限方法,permis enable:{},user id:{},user name:{},请求url:{},result:{}",API_PERMIS_ENABLE,principal.getId(),principal.getUsername(),url,fundedApi);
		return fundedApi;
	}
	
	
	
	
    
}

