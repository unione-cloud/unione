package com.unione.cloud.gateway.feign;

import org.springframework.stereotype.Component;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.feign.hystrix.HystrixFactory;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @类名 UniOneLogsApi 服务降级实现
 * @描述 1、可以设置默认降级实现
 *      2、也可以在Spring容器中注册自定义降级，bean name规范为 接口类名（首字母小写）
 * @作者 Jeking Yang
 * @版本 1.0.0
 */
@Slf4j
@Component
public class UniOneLogsHystrix extends HystrixFactory<UniOneLogsApi> {

    public UniOneLogsHystrix() {
        // 设置默认降级实现
        this.setDefaultFallback(new UniOneLogsApi() {
			@Override
			public Results<Long> save(UniOneLogs logs) {
				log.error("UniOneLogsApi远程保存失败，data:{}",JSONUtil.toJsonStr(logs));
	            return Results.failure("UniOneLogsApi远程保存失败，本地输出日志");
			}
        });
    }
    
}