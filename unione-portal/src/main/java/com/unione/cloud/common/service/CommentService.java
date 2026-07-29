package com.unione.cloud.common.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.model.CommCommentItem;
import com.unione.cloud.common.model.CommCommentSetting;
import com.unione.cloud.common.model.CommCommentTarget;
import com.unione.cloud.common.model.CommVisitTarget;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.HpdlProcess;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.util.SmqUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommentService {

    @Autowired
	private DataBaseDao dataBaseDao;

    @Autowired
	private CacheManager cacheManager;

    @Autowired
    private RedisService redisService;


    /**
	 * 缓存时间：单位秒，默认600秒
	 */
	@Value("${comment.cache.setting.lifetime:600}")
	private int    CACHE_SETTING_LIFETIME;

     /**
	 * 缓存时间：单位秒，默认600秒
	 */
	@Value("${comment.cache.item.lifetime:600}")
	private int    CACHE_ITEM_LIFETIME;


    
    /**
	 * 	获得目标缓存对象
	 * @return
	 */
	private Cache<String,CommCommentTarget> getTargetCache() {
		QuickConfig config = QuickConfig.newBuilder("COMM:MOMENT:TARGET:")
            .localExpire(Duration.ofSeconds(60))
		    .cacheType(CacheType.BOTH)
		    .build();
		return cacheManager.getOrCreateCache(config);
	}


     /**
	 * 	获得设置缓存对象
	 * @return
	 */
	private Cache<Long,CommCommentSetting> getSettingCache() {
		QuickConfig config = QuickConfig.newBuilder("COMM:MOMENT:SETTING:")
		    .expire(Duration.ofSeconds(CACHE_SETTING_LIFETIME))
            .localExpire(Duration.ofSeconds(60))
		    .cacheType(CacheType.BOTH)
		    .build();
		return cacheManager.getOrCreateCache(config);
	}


     /**
	 * 	获得设置缓存对象
	 * @return
	 */
	private Cache<Long,CommCommentItem> getItemCache() {
		QuickConfig config = QuickConfig.newBuilder("COMM:MOMENT:ITEM:")
		    .expire(Duration.ofSeconds(CACHE_ITEM_LIFETIME))
            .localExpire(Duration.ofSeconds(60))
		    .cacheType(CacheType.BOTH)
		    .build();
		return cacheManager.getOrCreateCache(config);
	}



    /**
     * 加载评论目标
     * @param type
     * @return
     */
    public CommCommentTarget loadTarget(String type){
        Cache<String,CommCommentTarget> cache=getTargetCache();
        CommCommentTarget entity=cache.get(type);
        if(entity==null){
            entity=redisService.doHpdl(new HpdlProcess<CommCommentTarget>(String.format("hpdl:moment:target:%s", type)) {
                @Override
                public CommCommentTarget process() {
                    CommCommentTarget tmp=cache.get(type);
                    if(tmp==null){
                        tmp = dataBaseDao.findOne(SqlBuilder.build(CommCommentTarget.class)
                            .where("targetType",type).dataPermis(PermisRule.ALL));
                        if(tmp==null){
                            tmp=new CommCommentTarget();
                            tmp.setAuditFlag(0);
                            tmp.setCloseFlag(0);
                            tmp.setRefLevel(4);
                            tmp.setAsyncFlag(1);
                            tmp.setLikeEnable(1);
                            tmp.setFileEnable(1);
                            tmp.setFileCount(2);
                            tmp.setFileType("*");
                        }
                        cache.put(type,tmp);
                    }
                    return tmp;
                }
            }, 500, 3);
        }
        return entity;
    }

    /**
     * 清除目标缓存
     * @param type
     */
    public void clearTarget(String type){
        getTargetCache().remove(type);
    }


    /**
     * 加载评论设置
     * @param tid
     * @return
     */
    public CommCommentSetting loadSetting(Long tid){
        Cache<Long,CommCommentSetting> cache=getSettingCache();
        CommCommentSetting entity=cache.get(tid);
        if(entity==null){
            entity=redisService.doHpdl(new HpdlProcess<CommCommentSetting>(String.format("hpdl:moment:setting:%s", tid)) {
                @Override
                public CommCommentSetting process() {
                    CommCommentSetting tmp=cache.get(tid);
                    if(tmp==null){
                        tmp = dataBaseDao.findOne(SqlBuilder.build(CommCommentSetting.class)
                            .where("targetId",tid).dataPermis(PermisRule.ALL));
                        if(tmp==null){
                            tmp=new CommCommentSetting();
                        }
                        cache.put(tid,tmp);
                    }
                    return tmp;
                }
            }, 500, 3);
        }
        if(entity!=null && entity.getId()==null){
            return null;
        }
        return entity;
    }


    /**
     * 清除设置缓存
     * @param tid
     */
    public void clearSetting(Long tid){
        getSettingCache().remove(tid);
    }


    /**
     * 加载评论明细
     * @param id
     * @return
     */
    public CommCommentItem loadItem(Long id){
        Cache<Long,CommCommentItem> cache=getItemCache();
        CommCommentItem entity=cache.get(id);
        if(entity==null){
            entity=redisService.doHpdl(new HpdlProcess<CommCommentItem>(String.format("hpdl:moment:item:%s", id)) {
                @Override
                public CommCommentItem process() {
                    CommCommentItem tmp=cache.get(id);
                    if(tmp==null){
                        tmp = dataBaseDao.findById(SqlBuilder.build(CommCommentItem.class,id).dataPermis(PermisRule.ALL));
                        if(tmp==null){
                            tmp=new CommCommentItem();
                        }
                        cache.put(id,tmp);
                    }
                    return tmp;
                }
            }, 500, 3);
        }
        if(entity!=null && entity.getId()==null){
            return null;
        }
        return entity;
    }


    /**
     * 清除明细缓存
     * @param id
     */
    public void clearItem(Long id){
        getItemCache().remove(id);
    }


    /**
     * 点赞评论
     * @param id
     */
    public void incLike(Long id){
        // 参数处理
		CommCommentItem item = loadItem(id);
		AssertUtil.service().notNull(item, "评论不存在");
		if(item.getLikeCount()==null){
			item.setLikeCount(0);
		}
		item.setLikeCount(item.getLikeCount()+1);
		getItemCache().put(id,item);
        SmqUtil.sync().send("comment:item", "u", item, "likeCount");
    }


}
