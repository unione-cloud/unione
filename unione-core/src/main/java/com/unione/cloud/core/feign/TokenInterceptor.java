package com.unione.cloud.core.feign;

import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.unione.cloud.core.security.SessionService;

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
		
		// 使用sessionService服务获取token
		if(this.sessionService!=null){
			try {
				token=sessionService.getToken();
				if(!StringUtils.isEmpty(token)) {
					template.header(TOKEN_NAME, TOKEN_PREFIX+token+TOKEN_SUFIX);
					template.header(signature(token), "true");
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
	
	
	 /**
     * feign MD5签名
     * @param token
     * @return
     */
    public static String signature(String token) {
    	try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] resBytes = md.digest(token.getBytes());
			StringBuffer sBuffer = new StringBuffer();
	        for (int i = 0; i < resBytes.length; i++) {
	            sBuffer.append(byteToArrayString(resBytes[i]));
	        }
			return sBuffer.toString();
		} catch (Exception e) {
			logger.error("feign签名失败,token:{}",token,e);
		}
    	return null;
    }
    
	// 全局数组
    private final static String[] strDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }
	

}
