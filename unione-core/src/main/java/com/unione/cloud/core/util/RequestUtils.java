package com.unione.cloud.core.util;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求工具类
 * @author Jeking Yang
 * @version 1.0.0
 */
@Slf4j
public class RequestUtils {


	private static Searcher ipsearcher;
	static{
		InputStream in = null;
		try {
			in = RequestUtils.class.getResourceAsStream("/xdb/ip2region.xdb");
			ipsearcher = Searcher.newWithBuffer(IoUtil.readBytes(in));
		} catch (Exception e) {
			log.error("ip2region.xdb文件加载失败,message:{}",e.getMessage());
		} finally{
			IoUtil.close(in);
		}
	}
	

	/**
	 * 获取client ip地址
	 * @param request
	 * @return
	 */
	public static String getClientIp(ServerHttpRequest request) {
		log.debug("进入：获取client ip方法");
		if(request==null){
			return null;
		}
		String ip = null;
		HttpHeaders headers = request.getHeaders();
		ip = StringUtils.trimToNull(headers.getFirst("X-Forwarded-For"));
		log.debug("get ip from head X-Forwarded-For,value:{}",ip);
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(headers.getFirst("X-Real-IP"));
			log.debug("get ip from head X-Real-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(headers.getFirst("WL-Proxy-Client-IP"));
			log.debug("get ip from head WL-Proxy-Client-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(headers.getFirst("Proxy-Client-IP"));
			log.debug("get ip from head Proxy-Client-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = Optional.ofNullable(request.getRemoteAddress())
                    .map(address -> address.getAddress().getHostAddress())
                    .orElse("");
			log.debug("get ip from remote addr,value:{}",ip);
            if ("127.0.0.1".equals(ip)||"localhost".equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    log.debug("get ip from local host,value:{}",ip);
                } catch (Exception e) {
                	log.warn("获取网卡本地ip地址失败,message:{}",e.getMessage());
                }
            }
		}
		if (StringUtils.isNotEmpty(ip)) {
			ip = ip.split(",")[0];
		}
		
		log.debug("退出：获取client ip方法,ip addr:{}",ip);
		return ip;
	}

	/**
	 * 获取client ip地址
	 * @param request
	 * @return
	 */
	public static String getClientIp(HttpServletRequest request) {
		log.debug("进入：获取client ip方法");
		if(request==null){
			return "Unknown";
		}
		String ip = null;
		ip = StringUtils.trimToNull(request.getHeader("X-Forwarded-For"));
		log.debug("get ip from head X-Forwarded-For,value:{}",ip);
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(request.getHeader("X-Real-IP"));
			log.debug("get ip from head X-Real-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(request.getHeader("WL-Proxy-Client-IP"));
			log.debug("get ip from head WL-Proxy-Client-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = StringUtils.trimToNull(request.getHeader("Proxy-Client-IP"));
			log.debug("get ip from head Proxy-Client-IP,value:{}",ip);
		}
		if(ip == null || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			log.debug("get ip from remote addr,value:{}",ip);
            if ("127.0.0.1".equals(ip)||"localhost".equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    log.debug("get ip from local host,value:{}",ip);
                } catch (Exception e) {
                	log.warn("获取网卡本地ip地址失败,message:{}",e.getMessage());
                }
            }
		}
		if (StringUtils.isNotEmpty(ip)) {
			ip = ip.split(",")[0];
		}
		
		log.debug("退出：获取client ip方法,ip addr:{}",ip);
		return StringUtils.trimToNull(ip);
	}

	/**
	 * 获取客户端操作系统名称
	 * @param request
	 * @return
	 */
	public static String getClientOs(HttpServletRequest request) {
	    if (request == null) {
	        return "Unknown";
	    }
	    String userAgent = request.getHeader("user-agent");
	    if (StringUtils.isEmpty(userAgent)) {
	        return "Unknown";
	    }
	    userAgent=userAgent.toLowerCase();
	    if (userAgent.contains("windows")) {
	        return "Windows";
	    } else if (userAgent.contains("mac os")) {
	        return "macOS";
	    } else if (userAgent.contains("harmonyos") || userAgent.contains("鸿蒙")) {
            return "HarmonyOS";
        } else if (userAgent.contains("linux")) {
	        if (userAgent.contains("android")) {
	            return "Android";
	        } else {
	            return "Linux";
	        }
	    } else if (userAgent.contains("ios")) {
	        return "iOS";
	    } else if (userAgent.contains("ipad")) {
	        return "iPadOS";
	    } else {
	        return "Unknown";
	    }
	}

	/**
	 * 获取客户端浏览器名称
	 * @param request
	 * @return
	 */
	public static String getClientExplorer(HttpServletRequest request) {
	    if (request == null) {
	        return "Unknown";
	    }
	    String userAgent = request.getHeader("user-agent");
	    if (StringUtils.isEmpty(userAgent)) {
	        return "Unknown";
	    }
	    userAgent=userAgent.toLowerCase();
	    if (userAgent.contains("edg")) {
	        return "Microsoft Edge";
	    } else if (userAgent.contains("chrome")) {
	        return "Google Chrome";
	    } else if (userAgent.contains("firefox")) {
	        return "Mozilla Firefox";
	    } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
	        return "Safari";
	    } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
	        return "Internet Explorer";
	    }  else if (userAgent.contains("360se")||userAgent.contains("360ee")) {
            return "360 Explorer";
        }
	    return "Unknown";
	}


	/**
	 * 获取客户端位置信息，根据客户端ip获取
	 * @param request
	 * @return
	 */
	public static ClientLocation getClientLocation(HttpServletRequest request){
		ClientLocation location=new ClientLocation();
		if(request==null){
			return location;
		}
		location.setVisitIp(getClientIp(request));

		// 根据ip获取位置信息
		if(!ObjectUtil.isEmpty(location.getVisitIp()) && ipsearcher!=null){
			location=getClientLocation(location.getVisitIp());
		}

		return location;
	}


	/**
	 * 根据ip获取位置信息
	 * @param ip
	 * @return
	 */
	public static ClientLocation getClientLocation(String ip){
		ClientLocation location=new ClientLocation();
		if(!ObjectUtil.isEmpty(ip) && ipsearcher!=null){
			try {
				//searchIpInfo 的数据格式： 国家|区域|省份|城市|ISP
				String info = ipsearcher.search(ip);
				if(StringUtils.isNotEmpty(info)){
					String[] infos = info.split("\\|");
					if(infos.length>=5 && !"0".equals(infos[0])){
						location.setCountry(infos[0]);
						location.setProvince(infos[2]);
						location.setCity(infos[3]);
					}
				}
			} catch (Exception e) {
				log.error("根据ip获取位置信息失败,ip:{},message:{}", location.getVisitIp(), e.getMessage());
			}
		}
		return location;
	}


	@Data
	public static class ClientLocation{
		@Schema(title="访问IP",description="长度为：50")
		private String visitIp="Unknown";
		@Schema(title="所在国家",description="长度为：200")
		private String country="Unknown";
		@Schema(title="所在省份",description="长度为：100")
		private String province="Unknown";
		@Schema(title="所在城市",description="长度为：100")
		private String city="Unknown";
	}

}