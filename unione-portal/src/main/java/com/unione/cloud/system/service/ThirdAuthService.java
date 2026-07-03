package com.unione.cloud.system.service;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.dto.ThirdAuthDto;
import com.unione.cloud.system.model.Sys3rdAuth;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.script.ScriptUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 3rd认证服务
 */
@Slf4j
@Service
public class ThirdAuthService {

    @Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private SessionService sessionService;


    /**
     * 加载第三方系统认证
     * @param sn 编码
     * @return 认证信息
     */
    public ThirdAuthDto load(String sn){
        Sys3rdAuth authObj = dataBaseDao.findOne(SqlBuilder.build(Sys3rdAuth.class).where("sn=? and status = 1 and delFlag = 0 ").where("sn",sn));
        AssertUtil.service().notNull(authObj, "认证信息未找到")
            .isTrue(ObjectUtil.equal(authObj.getTenantId(), sessionService.getTenantId())||ObjectUtil.equal(authObj.getIsGlobal(), 1), "认证信息未找到");

        ThirdAuthDto auth = new ThirdAuthDto();
        if(!ObjectUtil.isEmpty(authObj.getDataJson())){
            auth = JsonUtil.toBean(ThirdAuthDto.class,authObj.getDataJson());
        }
        if(!ObjectUtil.isEmpty(authObj.getScriptTxt())){
            try {
                ScriptContext context = new SimpleScriptContext();

                context.setAttribute("auth", auth, ScriptContext.ENGINE_SCOPE);
                context.setAttribute("url", authObj.getUrl(), ScriptContext.ENGINE_SCOPE);
                context.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);

                ScriptEngine scriptEngine = ScriptUtil.createJsEngine();
                scriptEngine.put("polyglot.js.allowAllAccess", true);

                scriptEngine.eval(String.format("(function(){\n %s \n})()", authObj.getScriptTxt()), context);
            } catch (Exception e) {
                log.error("第三方认证处理失败,id:{},url:{},auth:{},script:{}", authObj.getId(), authObj.getUrl(), auth,authObj.getScriptTxt(), e);
                throw new ServiceException("第三方认证处理失败", e);
            }
        }
        return auth;
    }


}
