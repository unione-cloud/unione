package com.unione.cloud.core.generator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.AssertUtil;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @描述 Id默认生成器
 * @作者 Jeking Yang
 * @版本 1.0.0
 */
@Slf4j
@Service
public class DefaultIdGenerator implements IdGenerator {
    
    private static Snowflake snowflake;
    
    @Autowired
    public void setEnvironment(Environment environment) {
    	if(snowflake==null) {
    		String serviceName=environment.getProperty("spring.application.name");
    		String host = NetUtil.getLocalhostStr();
    		String port=environment.getProperty("server.port");
    		log.info("================ {} {}:{} init Snow Flake =================",serviceName,host,port);
    		AssertUtil.service().isTrue(!StringUtils.isEmpty(serviceName), "配置spring.application.name不为空")
	    		.isTrue(!StringUtils.isEmpty(host), "服务host获取失败")
	    		.isTrue(!StringUtils.isEmpty(port), "配置server.port不能为空");
    		
    		Long workerId=0L;
    		for(char c:serviceName.toCharArray()) {
    			workerId=workerId+c;
    		}
    		Long datacenterId=NetUtil.ipv4ToLong(host)+Integer.parseInt(port);
    		
    		log.info("create Snow Flake workerId:{},datacenterId:{}",workerId,datacenterId);
    		snowflake = IdUtil.getSnowflake(workerId%32, datacenterId%32);
    		
    		log.info("================ {} {}:{} inited Snow Flake ===============",serviceName,host,port);
    	}
    }

    /**
     * 	获得主键
     */
    public Long generate(){
        log.debug("进入服务:请求生成主键");
        Long id=snowflake.nextId();
        log.debug("退出服务:请求生成主键,id:{}",id);
        return id;
    }

    
}