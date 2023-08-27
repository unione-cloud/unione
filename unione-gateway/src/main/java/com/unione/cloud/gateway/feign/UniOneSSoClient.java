package com.unione.cloud.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.security.UserPrincipal;

import lombok.Data;

/**
 * 	SSO认证登录服务
 * @作者	Jeking Yang
 * @版本	1.0.0
 */
@FeignClient(name="${unione.cloud.server.name:unione-portal}", contextId="uniOneSSo",
	path = "/api/login",
	url="${unione.cloud.server.ip:}${unione.cloud.server.url:}",
	fallbackFactory = UniOneSSoClientHystrix.class)
public interface UniOneSSoClient {
	
	@Data
	public static class UniOneSSoRequest{
		private String authToken;
		private String authCode;
		private String realmName;
		private String authIp;
	}

	@PostMapping({"/sso"})
	public Results<UserPrincipal> sso(@RequestBody UniOneSSoRequest ssoRequest);
	
}
