package com.unione.cloud.common.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.VisitEntry;
import com.unione.cloud.common.model.CommVisitItem;
import com.unione.cloud.common.model.CommVisitTarget;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.RequestUtils;
import com.unione.cloud.core.util.RequestUtils.ClientLocation;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @标题 	访问登记 服务
 * @日期	2025-07-31 18:43:00
 * @版本	1.0.0
 **/
@Slf4j
@Service
public class VisitService {


    @Autowired
	private DataBaseDao dataBaseDao;

    @Autowired
	private CacheManager cacheManager;

    @Autowired
    private SessionService sessionService;

    /**
	 * 缓存时间：单位秒，默认7200秒
	 */
	@Value("${visit.cache.target.lifetime:7200}")
	private int    CACHE_LIFETIME;

    @Value("${visit.cache.queue.timeout:10}")
    private int    QUEUE_TIMEOUT;

    @Value("${visit.cache.queue.batchsize:100}")
    private int    QUEUE_BATCHSIZE;

    /**
     * 访问目标缓存队列
     */
    private BlockingQueue<CommVisitItem> queue = new ArrayBlockingQueue<>(2000);

    @Autowired
    private HttpServletRequest request;


    @PostConstruct
    private void postConstruct(){
        // 启动：访问目标缓存队列处理线程
        new Thread(() -> {
            List<CommVisitItem> list = new ArrayList<>();
            long count=0;
            while(true){
                try {
                    CommVisitItem item = queue.poll(QUEUE_TIMEOUT, TimeUnit.SECONDS);
                    if(item!=null){
                        if(list.isEmpty()){
                            count=0;
                        }
                        list.add(item);
                    }else{
                        count=count+1;
                    }
                    if(list.size()>=QUEUE_BATCHSIZE || !list.isEmpty() && count>6){
                        try{
                            dataBaseDao.insertBatchWithId(list);
                        }catch(Exception e){
                            log.error("保存访问登记信息失败,size:{}",list.size(),e);
                        }
                        count=0;
                        list.clear();
                    }
                } catch (Exception e) {
                   log.error("访问登记信息存储异常,queue size:{}",queue.size(), e);
                }
            }
        },"visit-target-queue-comsumer").start();
    }


    /**
	 * 	获得访问目标缓存对象
	 * @return
	 */
	private Cache<Long,CommVisitTarget> getTargetCache() {
		QuickConfig config = QuickConfig.newBuilder("COMM:VISIT:TARGET:LIFTE")
		    .expire(Duration.ofSeconds(CACHE_LIFETIME))
		    .cacheType(CacheType.BOTH)
		    .build();
		return cacheManager.getOrCreateCache(config);
	}


    /**
     * 加载访问目标
     * @param entry
     * @return
     */
    public CommVisitTarget loadTarget(VisitEntry entry){
        CommVisitTarget target = getTargetCache().get(entry.getTargetId());
        if(target == null){
            target = dataBaseDao.findOne(SqlBuilder.build(CommVisitTarget.class, entry.getTargetId()));
            if(target != null){
                getTargetCache().put(entry.getTargetId(), target);
            }else{
                target=new CommVisitTarget();
                target.setAppId(entry.getAppId());
                target.setTargetId(entry.getTargetId());
                target.setTargetType(entry.getTargetType());
                target.setTargetTitle(entry.getTargetTitle());
                target.setTargetUrl(entry.getTargetUrl());
                target.setParentId(-1L);
                target.setOrdered(0);
                target.setStatus(1);
                target.setDelFlag(0);
                dataBaseDao.insert(target);
                getTargetCache().put(entry.getTargetId(), target);
            }
        }
        AssertUtil.service().notNull(target, "访问目标不存在");
        return target;
    }


    /**
     * 访问登记方法
     * @param entry
     * @return
     */
    public Results<Void> entry(VisitEntry entry) {
        log.debug("进入：访问登记方法,app id:{}, target id:{}",entry.getAppId(),entry.getTargetId());
        CommVisitTarget target = loadTarget(entry);
        if(target!=null){
            CommVisitItem item = new CommVisitItem();
            item.setId(IdGenHolder.generate());
            item.setTenantId(sessionService.getTenantId());
            BeanUtils.setDefaultValue(item, "tenantId",-1L);
            item.setOrgId(sessionService.getOrgId());
             BeanUtils.setDefaultValue(item, "orgId",-1L);
            item.setUserId(sessionService.getUserId());
             BeanUtils.setDefaultValue(item, "userId",-1L);

            item.setAppId(entry.getAppId());
            item.setTargetId(entry.getTargetId());
            item.setVisitTime(DateUtil.date());
            item.setVisitYear(DateUtil.year(item.getVisitTime()));
            item.setVisitQuar(DateUtil.quarter(item.getVisitTime()));
            item.setVisitMonth(DateUtil.month(item.getVisitTime())+1);
            item.setVisitWeek(DateUtil.weekOfYear(item.getVisitTime()));
            item.setVisitDay(DateUtil.dayOfMonth(item.getVisitTime()));

            item.setVisitIp(RequestUtils.getClientIp(request));
            item.setExplorer(RequestUtils.getClientExplorer(request));
            item.setOsname(RequestUtils.getClientOs(request));

            ClientLocation location=RequestUtils.getClientLocation(request);
            item.setCountry(location.getCountry());
            item.setProvince(location.getProvince());
            item.setCity(location.getCity());
            try {
                queue.put(item);
                return Results.success();
            } catch (Exception e) {
                log.error("推送访问信息到队列失败,app id:{}, target id:{}",entry.getAppId(),entry.getTargetId(),e);
            }
        }
        return Results.failure();
    }

}
