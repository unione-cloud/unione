package com.unione.cloud.gateway.feign;

import org.springframework.stereotype.Component;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.feign.hystrix.HystrixFactory;
import com.unione.cloud.core.security.UserPrincipal;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @类名 UniOneSSoClient 服务降级实现
 * @描述 1、可以设置默认降级实现
 *      2、也可以在Spring容器中注册自定义降级，bean name规范为 接口类名（首字母小写）
 * @作者 Jeking Yang
 * @版本 1.0.0
 */
@Slf4j
@Component
public class UniOneSSoClientHystrix extends HystrixFactory<UniOneSSoClient> {
	public UniOneSSoClientHystrix() {
		// 设置默认降级实现
		this.setDefaultFallback(new UniOneSSoClient() {
			@Override
			public Results<UserPrincipal> sso(UniOneSSoRequest ssoRequest) {
				log.error("UniOneSSoClient远程认证失败，data:{}",JSONUtil.toJsonStr(ssoRequest));
	            return Results.failure("UniOneSSoClient远程认证失败");
			}
		});
	}
}
