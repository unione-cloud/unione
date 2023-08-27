package com.unione.cloud.core.util;

import java.net.InetAddress;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * 请求工具类
 * @author Jeking Yang
 * @version 1.0.0
 */
@Slf4j
public class RequestUtils {
	
	public static String getClientIp(ServerHttpRequest request) {
		log.debug("进入：获取client ip方法");
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
            if ("127.0.0.1".equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    log.debug("get ip from local host,value:{}",ip);
                } catch (Exception e) {
                	log.warn("获取网卡本地ip地址失败");
                }
            }
		}
		if (StringUtils.isNotEmpty(ip)) {
			ip = ip.split(",")[0];
		}
		
		log.debug("退出：获取client ip方法,ip addr:{}",ip);
		return ip;
	}

}
