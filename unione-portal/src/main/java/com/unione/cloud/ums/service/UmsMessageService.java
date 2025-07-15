package com.unione.cloud.ums.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.ums.dto.UmsMessageSend;
import com.unione.cloud.ums.dto.UmsMessageMine;
import com.unione.cloud.ums.model.UmsMessage;
import com.unione.cloud.ums.model.UmsMessageStatus;
import com.unione.cloud.ums.model.UmsMessageTarget;
import com.unione.cloud.ums.model.UmsMessageWay;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UmsMessageService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionService;

    /**
     * 保存消息
     * 
     * @param entity
     * @return
     */
    public Results<Long> save(UmsMessageSend entity) {
        // 参数验证
        AssertUtil.service().notEmpty(entity.getWays(), "通知方式不能为空");

        // 消息初始化
        int len = 0;
        BeanUtils.setDefaultValue(entity, "isConfirm", 0);
        BeanUtils.setDefaultValue(entity, "confirmType", 1);
        BeanUtils.setDefaultValue(entity, "priority", 4);
        BeanUtils.setDefaultValue(entity, "delFlag", 0);
        BeanUtils.setDefaultValue(entity, "publicDate", DateUtil.date());

        if (entity.getId() == null) {
            AssertUtil.service().notEmpty(entity.getWays(), "通知目标不能为空");

            // 保存消息
            len = dataBaseDao.insert(entity);
            AssertUtil.service().isTrue(len > 0, "消息保存失败");

            // 保存通知方式
            List<UmsMessageWay> ways = entity.getWays().stream().map(w -> {
                UmsMessageWay way = new UmsMessageWay();
                way.setWay(w);
                way.setMessageId(entity.getId());
                return way;
            }).collect(Collectors.toList());
            int les[] = dataBaseDao.insertBatch(ways);
            log.debug("保存通知方式,message id:{},les:{}", entity.getId(), les);

            // 保存通知目标
            entity.getTargets().stream().forEach(target -> {
                target.setMessageId(entity.getId());
            });
            les = dataBaseDao.insertBatch(entity.getTargets());
            log.debug("保存通知目标,message id:{},les:{}", entity.getId(), les);

        } else {
            // 更新消息
            String[] fields = { "title", "bodyHtml", "bodyText", "fromId", "isConfirm", "confirmType", "bizId",
                    "bizParam", "priority", "offlineDate" };
            UmsMessage message = (UmsMessage) entity;
            message.setUserId(sessionService.getUserId());

            SqlBuilder<UmsMessage> sqlBuilder = SqlBuilder.build(message).field(fields);
            len = dataBaseDao.updateById(sqlBuilder);
            AssertUtil.service().isTrue(len > 0, "消息更新失败");

            // 更新通知方式
            if (!ObjectUtil.isEmpty(entity.getWays())) {
                // 加载已有通知方式
                UmsMessageWay way = new UmsMessageWay();
                way.setMessageId(entity.getId());
                List<Long> delIds = new ArrayList<>();
                dataBaseDao.findList(SqlBuilder.build(way)).stream().forEach(w -> {
                    if (!entity.getWays().contains(w.getWay())) {
                        delIds.add(w.getId());
                    } else {
                        entity.getWays().remove(w.getWay());
                    }
                });
                if (!ObjectUtil.isEmpty(entity.getWays())) {
                    // 新增
                    List<UmsMessageWay> ways = entity.getWays().stream().map(w -> {
                        UmsMessageWay tmp = new UmsMessageWay();
                        tmp.setWay(w);
                        tmp.setMessageId(entity.getId());
                        return tmp;
                    }).collect(Collectors.toList());
                    int les[] = dataBaseDao.insertBatch(ways);
                    log.debug("保存通知方式,message id:{},les:{}", entity.getId(), les);
                }
                if (!ObjectUtil.isEmpty(delIds)) {
                    // 删除
                    int les = dataBaseDao.deleteById(SqlBuilder.build(UmsMessageWay.class, delIds));
                    log.debug("删除通知方式,message id:{},les:{}", entity.getId(), les);
                }
            }

            // 保存通知目标
            if (!ObjectUtil.isEmpty(entity.getTargets())) {
                // 加载已有通知目标
                Map<Long, UmsMessageTarget> tmap = new HashMap<>();
                entity.getTargets().stream().forEach(t -> {
                    tmap.put(t.getTargetId(), t);
                });
                UmsMessageTarget target = new UmsMessageTarget();
                target.setMessageId(entity.getId());
                List<Long> delIds = new ArrayList<>();
                dataBaseDao.findList(SqlBuilder.build(target)).stream().forEach(w -> {
                    if (!tmap.containsKey(w.getTargetId())) {
                        delIds.add(w.getId());
                    } else {
                        tmap.remove(w.getTargetId());
                    }
                });
                if (!ObjectUtil.isEmpty(tmap)) {
                    // 新增
                    tmap.values().stream().forEach(w -> {
                        w.setMessageId(entity.getId());
                    });
                    int les[] = dataBaseDao.insertBatch(new ArrayList<>(tmap.values()));
                    log.debug("保存通知目标,message id:{},les:{}", entity.getId(), les);
                }
                if (!ObjectUtil.isEmpty(delIds)) {
                    // 删除
                    int les = dataBaseDao.deleteById(SqlBuilder.build(UmsMessageTarget.class, delIds));
                    log.debug("删除通知目标,message id:{},les:{}", entity.getId(), les);
                }
            }
        }

        return Results.build(len > 0, entity.getId());
    }

    /**
     * 发送消息
     * 
     * @param mid
     */
    public void send(Long mid) {
        log.info("进入：发送消息方法,message id:{}", mid);
        // 加载消息
        UmsMessage message = dataBaseDao.findById(SqlBuilder.build(UmsMessage.class, mid));
        AssertUtil.service().notNull(message, "消息不存在");

        // 加载通知对象
        UmsMessageTarget target = new UmsMessageTarget();
        target.setMessageId(mid);
        List<UmsMessageTarget> targets=dataBaseDao.findList(SqlBuilder.build(target));
        Set<Long> tuids=targets.stream().filter(t->ObjectUtil.equal(t.getTargetType(), 4))
            .map(t->t.getTargetId())
            .filter(t->t!=null)
            .collect(Collectors.toSet());
        if(!ObjectUtil.isAllEmpty(tuids)){
            // 加载用户信息
            Map<String,SysUser> umap=dataBaseDao.findMap(SqlBuilder.build(SysUser.class,tuids),"id");

            // 加载通知状态
            Map<Long,UmsMessageStatus> smap=new HashMap<>();
            dataBaseDao.findList(SqlBuilder.build(UmsMessageStatus.class,tuids).field("userId,viewSts")
                .where("messageId=? and userId in [ids]").params("messageId",mid))
                .stream().forEach(s->{
                    smap.put(s.getUserId(), s);
                });
            List<UmsMessageStatus> addStatus = tuids.stream()
            .filter(uid->!smap.containsKey(uid))
            .filter(uid->umap.containsKey(uid.toString()))
            .map(uid->{
                SysUser u=umap.get(uid.toString());
                UmsMessageStatus s=new UmsMessageStatus();
                s.setTenantId(u.getTenantId());
                s.setOrgId(u.getOrgId());
                s.setUserId(uid);
                s.setMessageId(mid);
                s.setViewSts(0);
                return s;
            }).collect(Collectors.toList());
            if(!ObjectUtil.isEmpty(addStatus)){
                int les[] = dataBaseDao.insertBatch(addStatus);
                log.debug("保存用户消息状态,message id:{},les:{}", mid,les);
            }
        }

        // 发送短信 TODO

        // 发送邮件 TODO

        log.info("退出：发送消息方法,message id:{}", mid);
    }


    /**
     * 加载我的消息
     * @param params
     * @return
     */
    public Results<List<UmsMessageMine>> mine(Params<UmsMessageMine> params) {
        log.debug("进入：加载我的消息");

        // 加载我的消息
        Results<List<UmsMessageMine>> result = dataBaseDao.findPages("loadMine","countMine",SqlBuilder.build(params));

        log.debug("退出：加载我的消息");
        return result;
    }


}
