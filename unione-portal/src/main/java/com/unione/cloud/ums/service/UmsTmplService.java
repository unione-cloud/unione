package com.unione.cloud.ums.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.ums.dto.SmsEntity;
import com.unione.cloud.ums.dto.UmsTmplVar;
import com.unione.cloud.ums.model.UmsTmpl;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UmsTmplService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CacheManager cacheManager;

    @Value("${unione.ums.sms.default:sms-default}")
    private String TMPL_SMS_DEFAULT;

    @Value("${unione.ums.email.default:email-default}")
    private String TMPL_EMAIL_DEFAULT;

    @Value("${unione.ums.site.default:site-default}")
    private String TMPL_SITE_DEFAULT;

    @Value("${unione.cache.ums.tmpl.expire:600}")
    private int TMPL_CACHE_EXPIRE;

    private Cache<String, UmsTmpl> getTmplCache() {
        return cacheManager.getOrCreateCache(QuickConfig.newBuilder("UMS:TMPL:")
                .cacheType(CacheType.BOTH)
                .cacheNullValue(true)
                .expire(Duration.ofSeconds(TMPL_CACHE_EXPIRE))
                .build());
    }

    /**
     * 加载模版
     * 
     * @param sn
     * @return
     */
    public UmsTmpl loadTmpl(String sn) {
        Cache<String, UmsTmpl> cache = this.getTmplCache();
        UmsTmpl tmpl = cache.get(sn);
        if (tmpl == null) {
            redisService.doHpdl(new HpdlProcess<UmsTmpl>("UMS:TMPL:LOCK:" + sn) {
                @Override
                public UmsTmpl process() {
                    return dataBaseDao.findOne(SqlBuilder.build(UmsTmpl.class).params("sn", sn).params("tenantId",
                            sessionService.getTenantId()));
                }
            }, 100, 5);
        }
        if (tmpl != null) {
            cache.put(sn, tmpl);
        } else {
            tmpl = new UmsTmpl();
            cache.put(sn, tmpl);
        }
        AssertUtil.service().notNull(tmpl, "模板不存在")
                .notNull(tmpl.getId(), "模板不存在");
        return tmpl;
    }

    /**
     * 删除模版
     * 
     * @param sn
     */
    public void delTmpl(String sn) {
        Cache<String, UmsTmpl> cache = this.getTmplCache();
        cache.remove(sn);
    }

    /**
     * 解析CanvasEditor内容对象为纯文本，仅处理text类型的文本
     * 
     * @param IElement
     * @return
     */
    @SuppressWarnings("unchecked")
    public String toplainTxt(String IElement) {
        if (ObjectUtil.isEmpty(IElement)) {
            return "";
        }
        Map<String, Object> ele = JsonUtil.toBean(Map.class, IElement);
        List<Map<String, Object>> list = (List<Map<String, Object>>) ele.get("main");
        return toplainTxt(list);
    }

    @SuppressWarnings("unchecked")
    private String toplainTxt(List<Map<String, Object>> els) {
        StringBuffer txt = new StringBuffer();
        for (Map<String, Object> item : els) {
            String type = (String) item.get("type");
            if ("text".equals(type) || "title".equals(type)) {
                if (ObjectUtil.isEmpty(item.get("value"))) {
                    continue;
                }
                String text = (String) item.get("value");
                txt.append(text);
            }
            if(!ObjectUtil.isEmpty(item.get("valueList"))){
                txt.append(toplainTxt((List<Map<String, Object>>)item.get("valueList")));
            }
        }
        return txt.toString();
    }


    /**
     * 渲染模版内容
     * @param tmpl
     * @param sms
     * @return
     */
    public String renderContents(UmsTmpl tmpl,SmsEntity sms) {
        String content=sms.getContents();
        if(tmpl!=null){
            // 模版内容处理
            if(!ObjectUtil.isEmpty(tmpl.getBodyText())){
                sms.getVars().put("content", content);
                content=tmpl.getBodyText();
            }
            // 模版变量处理
            if(!ObjectUtil.isEmpty(tmpl.getVars())){
                List<UmsTmplVar> tvars=JsonUtil.toList(new TypeReference<List<UmsTmplVar>>() {}, content);
                for(UmsTmplVar var:tvars){
                    // 变量默认值处理
                    if(ObjectUtil.isEmpty(sms.getVars().get(var.getName())) && !ObjectUtil.isEmpty(var.getDefaultValue())){
                        sms.getVars().put(var.getName(), var.getDefaultValue());
                    }
                    // 必填验证处理
                    if(var.getRequired()!=null && var.getRequired()){
                        AssertUtil.service().notNull(sms.getVars().get(var.getName()), "变量 %s 不能为空", var.getName());
                    }
                }
            }
        }

        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(content);
        content = expression.getValue(sms.getVars(), String.class);

        return content;
    }

}
