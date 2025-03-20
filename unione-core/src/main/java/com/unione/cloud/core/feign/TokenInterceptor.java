package com.unione.cloud.core.feign;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.security.SessionService;

import cn.hutool.crypto.digest.DigestUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @描述 <p>Feign Token Interceptor
 * @author Jeking Yang
 * @version 2.0.0
 */
@Component
public class TokenInterceptor implements RequestInterceptor {
	
	private static Logger logger=LoggerFactory.getLogger(TokenInterceptor.class);
	
	@Value("${feign.token.name:token}")
	private String TOKEN_NAME;
	
	@Value("${feign.token.prefix:}")
	private String TOKEN_PREFIX;
	
	@Value("${feign.token.sufix:}")
	private String TOKEN_SUFIX;
	
	@Value("${feign.debug:true}")
	private boolean FEIGN_DEBU;
	
	@Autowired(required = false)
	private SessionService sessionService;

	@Override
	public void apply(RequestTemplate template) {
		logger.debug("Feign Token Interceptor apply");
		logger.debug("Feign Token TOKEN_NAME:{},TOKEN_PRE:{},TOKEN_SUFX:{}",TOKEN_NAME,TOKEN_PREFIX,TOKEN_SUFIX);
		String token=null;
		
		if(this.sessionService!=null){
			try {
				// 设置token
				token=sessionService.getToken();
				if(!StringUtils.isEmpty(token)) {
					template.header(TOKEN_NAME, TOKEN_PREFIX+token+TOKEN_SUFIX);
					template.header(DigestUtil.md5Hex(token), "true");
				}
				
				// 设置请求信息
				String _unione_actionid=sessionService.getVar("_unione_actionid");
				Object _unione_requestid=sessionService.getVar("_unione_requestid");
				if(_unione_actionid!=null) {
					template.header("_unione_actionid", _unione_actionid+"");
				}
				if(_unione_requestid!=null) {
					template.header("_unione_requestid", _unione_requestid+"");
				}
			} catch (Exception e) {
				logger.error("feign 请求头设置失败",e);
			}
		}
		
		if(FEIGN_DEBU) {
			for(String var:template.getRequestVariables()) {
				logger.debug("RequestVariables:{}",var);
			}
		}
		
		logger.debug("Feign Token Interceptor apply Token :{}",token);
	}
	
	

}
