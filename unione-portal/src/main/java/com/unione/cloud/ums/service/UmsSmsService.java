package com.unione.cloud.ums.service;


import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.ums.dto.SmsCaptcha;
import com.unione.cloud.ums.dto.SmsEntity;
import com.unione.cloud.ums.model.UmsSmsBox;
import com.unione.cloud.ums.model.UmsSmsGtw;
import com.unione.cloud.ums.model.UmsTmpl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import cn.hutool.script.ScriptUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UmsSmsService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UmsTmplService umsTmplService;


    @Value("${unione.sms.gtw.default:default}")
    private String GTW_DEFAULT;


    @Value("${unione.cache.ums.gtw.expire:3600}")
    private int GTW_CACHE_EXPIRE;

    @Value("${unione.sms.captcha.expire:10}")
    private int CAPTCHA_EXPIRE;

    @Value("${unione.sms.captcha.len.length:6}")
    private int CAPTCHA_LENGTH;

    @Value("${unione.sms.captcha.enable:true}")
    private boolean CAPTCHA_ENABLE;

    /**
     * 获取短信网关缓存
     * 
     * @return
     */
    private Cache<String, UmsSmsGtw> getGtwCache() {
        return cacheManager.getOrCreateCache(QuickConfig.newBuilder("UMS:GTW:")
                .cacheType(CacheType.BOTH)
                .cacheNullValue(true)
                .expire(Duration.ofSeconds(GTW_CACHE_EXPIRE))
                .build());
    }

    private Cache<String, String> getCaptchaCache() {
        return cacheManager.getOrCreateCache(QuickConfig.newBuilder("UMS:SMSCAPTCHA:")
                .cacheType(CacheType.REMOTE)
                .expire(Duration.ofMinutes(CAPTCHA_EXPIRE))
                .build());
    }

    /**
     * 加载短信网关
     * 
     * @param sn
     * @return
     */
    public UmsSmsGtw loadGtw(String sn) {
        Cache<String, UmsSmsGtw> cache = this.getGtwCache();
        UmsSmsGtw gtw = cache.get(sn);
        if (gtw == null) {
            gtw=redisService.doHpdl(new HpdlProcess<UmsSmsGtw>("UMS:GTW:LOCK:" + sn) {
                @Override
                public UmsSmsGtw process() {
                    return dataBaseDao.findOne(SqlBuilder.build(UmsSmsGtw.class).where("sn", sn));
                }
            }, 100, 5);
        }
        if (gtw != null) {
            cache.put(sn, gtw);
        } else {
            gtw = new UmsSmsGtw();
        }
        AssertUtil.service().notNull(gtw, "短信网关不存在")
                .isTrue(gtw.getId() != null, "短信网关不存在")
                .isTrue(ObjectUtil.equal(gtw.getStatus(), 1), "短信网关不可用");
        return gtw;
    }

    /**
     * 删除短信网关缓存
     * 
     * @param sn
     */
    public void delGtw(String sn) {
        Cache<String, UmsSmsGtw> cache = this.getGtwCache();
        cache.remove(sn);
    }

    /**
     * 认证短信网关
     * 
     * @param gtw
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object auth(UmsSmsGtw gtw) {
        if (ObjectUtil.isEmpty(gtw.getAuthInfo()) || ObjectUtil.isEmpty(gtw.getAuthScript())) {
            return null;
        }
        try {
            ScriptContext context = new SimpleScriptContext();
            Map<String, Object> auth = new HashMap<>();
            if (!ObjectUtil.isEmpty(gtw.getAuthInfo())) {
                auth = JsonUtil.toBean(Map.class, gtw.getAuthInfo());
            }

            context.setAttribute("authConfig", auth, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("host", gtw.getUrl(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("url", gtw.getAuthApi(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);

            ScriptEngine scriptEngine = ScriptUtil.createJsEngine();
            scriptEngine.put("polyglot.js.allowAllAccess", true);

            Object ret = scriptEngine.eval(String.format("(function(){\n %s \n})()", gtw.getAuthScript()), context);
            return ret;
        } catch (Exception e) {
            log.error("短信网关认证失败,网关ID:{},auth api:{},script:{}", gtw.getId(), gtw.getAuthApi(), gtw.getAuthScript(), e);
            throw new ServiceException("短信网关认证失败", e);
        }
    }

    /**
     * 接收短信
     * 
     * @param sn
     * @param timesmtap 时间戳
     */
    public void receive(String sn, Long timesmtap) {
        UmsSmsGtw gtw = this.loadGtw(sn);
        if (ObjectUtil.isEmpty(gtw.getReceiveApi()) || ObjectUtil.isEmpty(gtw.getReceiveScript())) {
            return;
        }
        Object authInfo = this.auth(gtw);

        try {
            ScriptContext context = new SimpleScriptContext();
            context.setAttribute("authInfo", authInfo, ScriptContext.ENGINE_SCOPE);
             context.setAttribute("host", gtw.getUrl(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("url", gtw.getReceiveApi(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("timesmtap", timesmtap, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);
            ScriptEngine scriptEngine = ScriptUtil.createJsEngine();
            scriptEngine.put("polyglot.js.allowAllAccess", true);

            Object list = scriptEngine.eval(String.format("(function(){\n %s \n})()", gtw.getReceiveScript()),
                    context);
            log.info("短信网关接收返回:{}", list);

        } catch (Exception e) {
            log.error("短信网关接收失败,网关ID:{},receive api:{},script:{}", gtw.getId(), gtw.getReceiveApi(),
                    gtw.getReceiveScript(), e);
            throw new ServiceException("短信网关接收失败", e);
        }
    }

    /**
     * 获取短信回执
     * 
     * @param sn
     * @param timesmtap 时间戳
     */
    public void receipt(String sn, Long timesmtap) {
        UmsSmsGtw gtw = this.loadGtw(sn);
        if (ObjectUtil.isEmpty(gtw.getReceiptApi()) || ObjectUtil.isEmpty(gtw.getReceiptScript())) {
            return;
        }
        Object authInfo = this.auth(gtw);
        try {
            ScriptContext context = new SimpleScriptContext();
            context.setAttribute("authInfo", authInfo, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("host", gtw.getUrl(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("url", gtw.getReceiptApi(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("timesmtap", timesmtap, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);
            ScriptEngine scriptEngine = ScriptUtil.createJsEngine();
            scriptEngine.put("polyglot.js.allowAllAccess", true);

            Object list = scriptEngine.eval(String.format("(function(){\n %s \n})()", gtw.getReceiptScript()),
                    context);
            log.info("短信网关回执返回:{}", list);

        } catch (Exception e) {
            log.error("短信网关回执失败,网关ID:{},receipt api:{},script:{}", gtw.getId(), gtw.getReceiptApi(),
                    gtw.getReceiptScript(), e);
            throw new ServiceException("短信网关回执失败", e);
        }
    }


    private boolean send(UmsSmsGtw gtw,UmsTmpl tmpl, SmsEntity sms,Long batchId,StringBuffer sendLogs) {
        UmsSmsBox box=new UmsSmsBox();
        box.setBatchId(batchId);
        box.setSendSts(3);
        if (ObjectUtil.isEmpty(gtw.getSendApi()) || ObjectUtil.isEmpty(gtw.getSendScript())) {
            sendLogs.append(DateUtil.now()).append("\t短信网关配置异常，send api或send script为空！忽略，不发送该短信！\n");
            box.setSendLog(sendLogs.toString());
            dataBaseDao.update(SqlBuilder.build(box).where("batchId=?").field("sendSts","sendLog"));
            return false;
        }
        Object authInfo = this.auth(gtw);
        try {

            sendLogs.append(DateUtil.now()).append("\t初始化远程调用对象\n");
            ScriptContext context = new SimpleScriptContext();
            context.setAttribute("authInfo", authInfo, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("host", gtw.getUrl(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("url", gtw.getSendApi(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("sms", sms, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("tmpl", tmpl, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);
            ScriptEngine scriptEngine = ScriptUtil.createJsEngine();
            scriptEngine.put("polyglot.js.allowAllAccess", true);


            sendLogs.append(DateUtil.now()).append("\t远程调用：开始\n");
            Object res = scriptEngine.eval(String.format("(function(){\n %s \n})()", gtw.getSendScript()), context);
            sendLogs.append(DateUtil.now()).append("\t远程调用：结束,返回值：").append(res!=null?res.toString():"null").append("\n");
            log.info("短信网关发送返回:{}", res);
            // 更新发送结果：
           
            if(res!=null && "true".equalsIgnoreCase(res.toString())){
                box.setSendSts(2);
            }
            box.setSendLog(sendLogs.toString());
            dataBaseDao.update(SqlBuilder.build(box).where("batchId=?").field("sendSts","sendLog"));
            return ObjectUtil.equal(2, box.getSendSts());
        } catch (Exception e) {
            log.error("短信网关发送失败,网关ID:{},send api:{},script:{}", gtw.getId(), gtw.getSendApi(),
                    gtw.getSendScript(), e);
            sendLogs.append(DateUtil.now()).append("\tLocalizedMessage:").append(e.getLocalizedMessage()).append("\n").append("\n");
            sendLogs.append(DateUtil.now()).append("\tStackTrace:").append("\n");
            for(StackTraceElement stack:e.getStackTrace()) {
                sendLogs.append(stack.toString()).append("\n").append("\n");
            }
            box.setSendLog(sendLogs.toString());
            dataBaseDao.update(SqlBuilder.build(box).where("batchId=?").field("sendSts","sendLog"));
            throw new ServiceException("短信网关发送失败", e);
        }
    }

    /**
     * 发送短信
     * 
     * @param sms
     * @return
     */
    public Results<Long> send(SmsEntity sms) {
        AssertUtil.service().notNull(sms, new String[]{"scene","tels"},"参数%s不能为空")
            .notEmpty(sms.getTels(), "手机号不能为空");
        if(ObjectUtil.isEmpty(sms.getTmpl())){
            AssertUtil.service().notNull(sms.getContents(), "短信内容不能为空");
        }
        if(ObjectUtil.isEmpty(sms.getGateway())){
            sms.setGateway(GTW_DEFAULT);
        }
        if(sms.getBizId()==null){
            //如果为空，则生成一个业务ID
            sms.setBizId(IdGenHolder.generate());
        }

        StringBuffer sendLogs=new StringBuffer();
        sendLogs.append(DateUtil.now()).append("\t短信网关:发送开始\n");
        sendLogs.append(DateUtil.now()).append("\tgateway:").append(sms.getGateway())
            .append(",scene:").append(sms.getScene())
            .append(",bizId:").append(sms.getBizId()).append("\n");

        sendLogs.append(DateUtil.now()).append("\t加载网关信息\n");    
        UmsSmsGtw gtw=this.loadGtw(sms.getGateway());
        Long batchid=IdGenHolder.generate();
        sendLogs.append(DateUtil.now()).append("\t生成批次ID:").append(batchid).append("\n");

        UmsTmpl tmpl=ObjectUtil.isNotEmpty(sms.getTmpl())?umsTmplService.loadTmpl(sms.getTmpl()):null;
        String contents=umsTmplService.renderContents(tmpl, sms);

        // 生成短信记录
        sendLogs.append(DateUtil.now()).append("\t迭代手机号，生成短信记录:").append("\n");
        List<UmsSmsBox> boxes=sms.getTels().stream().map(tel->StrUtil.trimToNull(tel)).filter(tel->tel!=null).map(tel->{
            UmsSmsBox box=new UmsSmsBox();
            if(tmpl!=null){
                box.setTmplId(tmpl.getId());
            }
            box.setContents(contents);
            box.setTypes("send");
            box.setGtwId(gtw.getId());
            box.setBatchId(batchid);
            box.setScene(sms.getScene());
            box.setBizId(sms.getBizId());
            box.setTel(tel);
            box.setSendSts(1);
            box.setViewSts(1);
            box.setSendLog(sendLogs.toString());
            box.setSendTime(sms.getTimeSend());
            return box;
        }).collect(Collectors.toList());
        if(boxes.isEmpty()){
            return Results.failure("手机号不能为空");
        }

        // 保存短信记录
        dataBaseDao.insertBatch(boxes);

        if(sms.getTimeSend()==null){
            // 立刻发送
            boolean flag = this.send(gtw,tmpl, sms, batchid, sendLogs);
            return Results.build(flag);
        }
        return Results.success(batchid);
    }

    

    /**
     * 发送验证码短信
     * @param sms
     */
    public Results<Long> sendCaptcha(SmsCaptcha captcha) {
        AssertUtil.service().notNull(captcha, new String[]{"scene","tel"},"参数%s不能为空");
        if(!CAPTCHA_ENABLE){
            return Results.success();
        }
        String text=RandomUtil.randomNumbers(CAPTCHA_LENGTH);
        String key=captcha.getScene()+":"+captcha.getTel();

        SmsEntity sms=new SmsEntity();
        sms.setScene(captcha.getScene());
        sms.setTels(Arrays.asList(captcha.getTel()));
        sms.setTmpl(captcha.getTmpl());
        sms.getVars().put("code", text);
        sms.getVars().put("expMinute", String.valueOf(CAPTCHA_EXPIRE));
        sms.setContents("验证码：${code}，${expMinute}分钟内有效，请勿泄露并尽快验证。");
        Results<Long> results = this.send(sms);

        if(results.isSuccess()){
            getCaptchaCache().put(key, text);
        }

        return results;
    }

    /**
     * 校验验证码短信
     * @param captcha
     * @return
     */
    public boolean valiCaptcha(String scene, String tel,String captcha) {
        if(!CAPTCHA_ENABLE){
            return true;
        }
        String key=scene+":"+tel;
        Cache<String,String> cache=getCaptchaCache();
        String captchaCache=cache.get(key);
        cache.remove(key);
        return ObjectUtil.isNotEmpty(captchaCache)&&captchaCache.equals(captcha);
    }

    /**
     * 监测验证码是否有效
     * @param scene
     * @param tel
     * @return
     */
    public boolean enableCaptcha(String scene, String tel) {
        String key=scene+":"+tel;
        String captcha=getCaptchaCache().get(key);
        return ObjectUtil.isNotEmpty(captcha);
    }

}
