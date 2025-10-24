package com.unione.cloud.system.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.common.dto.TreeNodeDto;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.system.dto.OnlineDocDto;
import com.unione.cloud.system.dto.OnlineDocDto.DocVersion;
import com.unione.cloud.system.model.SysOnlineDoc;
import com.unione.cloud.system.model.SysOnlineDocItem;

@Service
public class OnlineDocService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private CacheManager cacheManager;

    @Value("${unione.cache.onlinedoc.expire:6000}")
    private int DOC_CACHE_EXPIRE = 6000;

    @Value("${unione.cache.onlinedoci.expire:60000}")
    private int DOCI_CACHE_EXPIRE = 60000;

    private Cache<Long, OnlineDocDto> getDocCache() {
        return cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:ONLINEDOC:")
                .cacheType(CacheType.BOTH)
                .cacheNullValue(true)
                .expire(Duration.ofSeconds(DOC_CACHE_EXPIRE))
                .build());
    }

    private Cache<Long, String> getContentCache() {
        return cacheManager.getOrCreateCache(QuickConfig.newBuilder("SYS:ONLINEDOCI:")
                .cacheType(CacheType.BOTH)
                .cacheNullValue(true)
                .expire(Duration.ofSeconds(DOCI_CACHE_EXPIRE))
                .build());
    }

    /**
     * 根据应用id加载在线文档
     * @param appId
     * @return
     */
    public OnlineDocDto loadDocByAppId(Long appId) {
        Cache<Long, OnlineDocDto> cache = getDocCache();
        OnlineDocDto doc = cache.get(appId);
        if (doc == null) {
            SysOnlineDoc tmp = dataBaseDao.findOne(SqlBuilder.build(SysOnlineDoc.class)
                    .field("id")
                    .where("appId=? and delFlag=0")
                    .where("appId", appId)
                    .sort(Sort.build("versNo", "desc")));
            if (tmp != null) {
                doc = this.loadDocByDocId(tmp.getId());
                cache.put(appId, doc);
            }
        }
        return doc;
    }

    /**
     * 加载在线文档详情
     * 
     * @param id
     * @return
     */
    public OnlineDocDto loadDocByDocId(Long id) {
        Cache<Long, OnlineDocDto> cache = getDocCache();
        OnlineDocDto doc = cache.get(id);
        if (doc == null) {
            // 加载文档信息
            OnlineDocDto tmp = dataBaseDao.findById(SqlBuilder.build(OnlineDocDto.class, id)
                .field("id,appId,title,versNo,icon,picMax,picMid,picMix,profile,releaseTime,archiveTime,status"));
            if (tmp == null) {
                tmp = new OnlineDocDto();
                cache.put(id, tmp);
            }
            AssertUtil.service().notNull(tmp.getId(), "记录未找到").notIn(tmp.getStatus(), Arrays.asList(3, 4), "文档未发布");

            // 加载文档大纲
            List<SysOnlineDocItem> items = dataBaseDao.findList(SqlBuilder.build(SysOnlineDocItem.class)
                    .field("id,parentId,title,iconName")
                    .where("docId=? and delFlag=0")
                    .where("docId", id)
                    .sort(Sort.build("ordered", "asc")));
            Map<Long, TreeNodeDto> nodeMap = new HashMap<>();
            List<TreeNodeDto> nodeList = items.stream().map(item -> {
                TreeNodeDto node = new TreeNodeDto();
                node.setId(item.getId());
                node.setPid(item.getParentId());
                node.setTitle(item.getTitle());
                node.setIconName(item.getIconName());
                nodeMap.put(item.getId(), node);
                return node;
            }).collect(Collectors.toList());
            List<TreeNodeDto> docTree = new ArrayList<>();
            tmp.setDocTree(docTree);
            nodeList.stream().forEach(n -> {
                TreeNodeDto parent = nodeMap.get(n.getPid());
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    docTree.add(n);
                }
            });

            // 加载文档版本
            List<DocVersion> versList = dataBaseDao.findList(SqlBuilder.build(SysOnlineDoc.class)
                    .field("id,versNo")
                    .where("appId=? and delFlag=0 and status in (3,4)")
                    .where("appId",tmp.getAppId())
                    .sort(Sort.build("versNo", "desc")))
                    .stream().map(row->{
                        DocVersion vers = new DocVersion();
                        vers.setVersId(row.getId());
                        vers.setVersNo(row.getVersNo());
                        return vers;
                    }).collect(Collectors.toList());
            tmp.setVersions(versList);

            cache.put(id, tmp);
            doc = tmp;
        }
        AssertUtil.service().notNull(doc, "记录未找到").notIn(doc.getStatus(), Arrays.asList(3, 4), "文档未发布");
        return doc;
    }

    /**
     * 刷新在线文档缓存
     * @param doc
     */
    public void refreshDoc(SysOnlineDoc doc){
        Cache<Long, OnlineDocDto> cache = getDocCache();
        cache.remove(doc.getAppId());
        cache.remove(doc.getId());
        this.loadDocByAppId(doc.getAppId());
        this.loadDocByDocId(doc.getId());
    }

    /**
     * 根据文档项id加载文档内容
     * @param id
     * @return
     */
    public String loadDocContent(Long id){
        Cache<Long, String> cache = getContentCache();
        String content = cache.get(id);
        if (content == null) {
            SysOnlineDocItem item = dataBaseDao.findOne(SqlBuilder.build(SysOnlineDocItem.class)
                    .field("id,contents")
                    .where("id=? and delFlag=0")
                    .where("id", id));
            if (item == null || item.getContents() == null) {
                content="{\"header\":[],\"main\":[{\"value\":\"文档内容为空\"}],\"footer\":[]}";
                cache.put(id, content);
            }else{
                content=item.getContents();
                cache.put(id, content);
            }
        }
        return content;
    }


    /**
     * 刷新文档内容缓存
     * @param id
     */
    public void refreshContent(SysOnlineDocItem item){
        Cache<Long, String> cache = getContentCache();
        cache.put(item.getId(), item.getContents());
    }


}
