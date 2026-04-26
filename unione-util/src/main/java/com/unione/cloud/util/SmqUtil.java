package com.unione.cloud.util;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.generator.IdGenHolder;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.util.SpringCtxUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 简易消息队列，基于redis，支持DB和API两种模式，支持集群，确保按序一次消费，支持同步消费和并发消费
 */
@Slf4j
public class SmqUtil {

    @Getter
    private boolean sync;
    @Getter
    private boolean auto;
    @Getter
    private String name;
    private DataBaseDao dataBaseDao;
    private RedisService redisService;
    private int db;
    private int consume_timeout;
    private long process_timeout;
    private List<Integer> duplicateKeyErrorCodes = List.of();

    private static ConcurrentMap<String, SmqUtil> smqMap = new ConcurrentHashMap<>();

    private static final String SMQ_NAMES = "smq:names";
    private static final String SMQ_LOCK = "smq:lock:%s";
    private static final String SMQ_INFO = "smq:info:%s";
    private static final String SMQ_QUEUE = "smq:queue:%s";
    private static final String SMQ_ERROR = "smq:error:%s";

    private SmqUtil() {
        dataBaseDao = SpringCtxUtil.getBean("dataBaseDao");
        redisService = SpringCtxUtil.getBean("redisService");
        Environment env = SpringCtxUtil.getBean(Environment.class);
        db = Integer.parseInt(env.getProperty("unione.smq.db", "9"));
        consume_timeout = Integer.parseInt(env.getProperty("unione.smq.comsume.timeout", "2000"));
        process_timeout = Integer.parseInt(env.getProperty("unione.smq.process.timeout", "2000"));
        String duplicateKeyErrorCodesStr = env.getProperty("unione.smq.duplicateKey.errorCodes",
                "1062,2627,2601,23505,51000,23505");
        if (!ObjectUtil.isEmpty(duplicateKeyErrorCodesStr)) {
            duplicateKeyErrorCodes = List.of(duplicateKeyErrorCodesStr.trim().split(",")).stream().map(Integer::valueOf)
                    .toList();
        }
    }

    private SmqUtil(String name, boolean auto, boolean sync) {
        this();
        this.name = name;
        this.auto = auto;
        this.sync = sync;

        String ikey = String.format(SMQ_INFO, this.name);
        boolean exists = redisService.inSet(db, SMQ_NAMES, name);
        if (!exists) {
            redisService.putMap(db, ikey, "name", this.name);
            redisService.putMap(db, ikey, "auto", this.auto);
            redisService.putMap(db, ikey, "sync", this.sync);
            redisService.putMap(db, ikey, "created", DateUtil.current());
            redisService.putMap(db, ikey, "timestamp", DateUtil.current());
            redisService.putMap(db, ikey, "size", 0L);
            redisService.putMap(db, ikey, "total", 0L);
            redisService.putMap(db, ikey, "success", 0L);
            redisService.putMap(db, ikey, "error", 0L);
            redisService.putSet(db, SMQ_NAMES, name);
            if(this.sync){
                redisService.putListValue(db,String.format(SMQ_LOCK, this.name), DateUtil.current());
            }
        }
        if (this.auto) {
            autoConsume();
        }
    }

    private SmqUtil(String name, DataBaseDao dataBaseDao, boolean auto, boolean sync) {
        this(name, auto, sync);
        this.dataBaseDao = dataBaseDao;
    }

    /**
     * 获取默认消息队列实例,默认队列名称：default，类型：DB，自动消费，异步消费
     * 
     * @return
     */
    public static SmqUtil ins() {
        SmqUtil util = smqMap.get("default");
        if (util == null) {
            util = build("default");
        }
        return util;
    }

    /**
     * 获取默认消息队列实例,默认队列名称：default-sync，类型：DB，自动消费，同步消费
     * @return
     */
    public static SmqUtil sync(){
        return build("default-sync", true, true);
    }

    /**
     * 获取消息队列实例：指定队列名称
     * 
     * @param name
     * @return
     */
    public static SmqUtil ins(String name) {
        SmqUtil util = smqMap.get(name);
        AssertUtil.service().notNull(util, String.format("队列%s未初始化", name));
        return util;
    }

    /**
     * 构建消息队列实例：指定队列名称,自动消费，异步消费
     * 
     * @param name
     * @return
     */
    public static SmqUtil build(String name) {
        return build(name, true, false);
    }

    /**
     * 构建消息队列实例：指定队列名称、是否自动消费、是否同步消费s
     * 
     * @param name
     * @param auto
     * @param sync
     * @return
     */
    public static SmqUtil build(String name, boolean auto, boolean sync) {
        SmqUtil smqUtil = smqMap.get(name);
        if (smqUtil == null) {
            smqUtil = new SmqUtil(name, auto, sync);
            smqMap.putIfAbsent(name, smqUtil);
        }
        return smqUtil;
    }

    /**
     * 构建消息队列实例：指定队列名称和数据库访问对象，自动消费，异步消费
     * 
     * @param name
     * @param dao
     * @return
     */
    public static SmqUtil build(String name, DataBaseDao dao) {
        return build(name, dao, true, false);
    }

    /**
     * 构建消息队列实例：指定队列名称、数据库访问对象、是否自动消费、是否同步消费
     * 
     * @param name
     * @param dao
     * @param auto
     * @param sync
     * @return
     */
    public static SmqUtil build(String name, DataBaseDao dao, boolean auto, boolean sync) {
        SmqUtil smqUtil = smqMap.get(name);
        if (smqUtil == null) {
            smqUtil = new SmqUtil(name, dao, auto, sync);
            smqMap.putIfAbsent(name, smqUtil);
        }
        return smqUtil;
    }



    /**
     * 发送消息到队列：指定队列名称和数据内容
     * @param name
     * @param data
     */
    public void send(String name, Object data) {
        send(new SmqMessage(name, data));
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型和数据内容
     * @param name
     * @param action
     * @param pojo
     */
    public void send(String name, String action, Pojo pojo) {
        send(name, action, pojo, null);
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象和字段列表
     * 
     * @param name
     * @param action
     * @param pojo
     * @param fields 字段列表，逗号分隔
     */
    public void send(String name, String action, Pojo pojo, String fields) {
        send(name, action, pojo,fields, "u");
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象、字段列表和冲突处理策略
     * 
     * @param name
     * @param action
     * @param pojo
     * @param fields
     * @param conflict
     */
    public void send(String name, String action, Pojo pojo, String fields, String conflict) {
        send(name, action, pojo,pojo, fields, conflict);
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型和POJO对象列表
     * 
     * @param name
     * @param action
     * @param pojos
     */
    public void send(String name, String action, List<? extends Pojo> pojos) {
        send(name, action, pojos, null);
    }


    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象列表和字段列表
     * 
     * @param name
     * @param action
     * @param pojos
     * @param fields
     */
    public void send(String name, String action, List<? extends Pojo> pojos, String fields) {
        send(name, action, pojos, fields,"u");
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象列表和冲突处理策略
     * 
     * @param name
     * @param action
     * @param pojos
     * @param fields 字段列表，逗号分隔
     * @param conflict
     */
    public void send(String name, String action, List<? extends Pojo> pojos, String fields, String conflict) {
        pojos.stream().forEach(data -> {
            send(name, action, data, data,fields, conflict);
        });
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象和数据内容
     * 
     * @param name
     * @param action 操作类型，i:插入,u:更新,d:删除
     * @param pojo
     * @param data
     */
    public void send(String name, String action, Pojo pojo, Object data) {
        send(name, action, pojo, data, "u");
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象、数据内容和冲突处理策略
     * 
     * @param name
     * @param action
     * @param pojo
     * @param data
     * @param conflict
     */
    public void send(String name, String action, Pojo pojo, Object data, String conflict) {
        send(name, action, pojo, data, null, conflict);
    }

    /**
     * 发送消息到队列：指定队列名称、操作类型、POJO对象和数据内容
     * 
     * @param name
     * @param action   操作类型，i:插入,u:更新,d:删除
     * @param pojo
     * @param data
     * @param fields   字段列表，逗号分隔
     * @param conflict 冲突处理策略，i:忽略,u:更新
     */
    public void send(String name, String action, Pojo pojo, Object data, String fields, String conflict) {
        AssertUtil.service().notIn(action, Arrays.asList("i", "u", "d"), "操作类型必须为i/u/d")
                .notIn(conflict, Arrays.asList("i", "u"), "冲突处理策略必须为i/u")
                .notNull(data, "数据内容不能为空")
                .notNull(pojo, "POJO对象不能为空");
        SmqMessage message = new SmqMessage();
        message.setName(name);
        SmqDbsd dbsData = new SmqDbsd();
        dbsData.setAction(action);
        dbsData.setConflict(conflict);
        dbsData.setData(data);
        dbsData.setFields(fields);
        String tableName = dataBaseDao.getSqlManager().getNc().getTableName(pojo.getClass());
        String schema = dataBaseDao.getSqlManager().getMetaDataManager().getTable(tableName).getSchema();
        dbsData.setSchema(schema);
        dbsData.setTable(tableName);
        message.setData(dbsData);
        send(message);
    }

    /**
     * 发送消息到队列
     * 
     * @param data
     */
    @SuppressWarnings("unchecked")
    public void send(SmqMessage message) {
        if (this.auto) {
            AssertUtil.service().isTrue(message.getData() instanceof SmqDbsd, "自动消费模式下，必须为数据库同步消息");
        }
        redisService.putListValue(db, String.format(SMQ_QUEUE, this.name), message);
        String ikey = String.format(SMQ_INFO, this.name);
        redisService.putMap(db, ikey, "timestamp", DateUtil.current());
        redisService.template(db).opsForHash().increment(ikey, "size", 1L);
        redisService.template(db).opsForHash().increment(ikey, "total", 1L);
    }

    /**
     * 消费数据
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public SmqConsumer consume() {
        String dkey = String.format(SMQ_QUEUE, this.name);
        String ikey = String.format(SMQ_INFO, this.name);
        String lkey = String.format(SMQ_LOCK, this.name);
        if (this.sync) {
            // 获取同步锁
            Object lock = redisService.template(db).opsForList().leftPop(lkey, this.consume_timeout,
                    TimeUnit.MILLISECONDS);
            if (lock == null) {
                Object preLock=redisService.getObj(db,String.format("%s:time", lkey));
                if(preLock!=null && (DateUtil.current()-Long.parseLong(preLock.toString()))<this.process_timeout*5){
                    return null;
                }
                if(preLock==null){
                    redisService.put(db,String.format("%s:time", lkey), DateUtil.current());
                }
            }else{
                redisService.put(db,String.format("%s:time", lkey), lock);
            }
        }
        // 异步消费
        SmqMessage message = (SmqMessage) redisService.template(db).opsForList().leftPop(dkey, this.consume_timeout,
                TimeUnit.MILLISECONDS);
        if (message == null) {
            if (this.sync) {
                // 释放同步锁
                redisService.putListValue(db,lkey, DateUtil.current());
            }
            return null;
        }
        redisService.template(db).opsForHash().increment(ikey, "size", -1L);
        redisService.template(db).opsForHash().put(ikey, "timestamp", DateUtil.current());
        return new SmqConsumer(this, message, this.process_timeout, lkey);
    }

    /**
     * 重新消费消息,从异常队列中消费数据
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public SmqConsumer resume() {
        String dkey = String.format(SMQ_ERROR, this.name);
        String lkey = String.format(SMQ_LOCK, String.format("%s-resume", this.name));
        if (this.sync) {
            // 获取同步锁
            Object lock = redisService.template(db).opsForList().leftPop(lkey, this.consume_timeout,
                    TimeUnit.MILLISECONDS);
            if (lock == null) {
                return null;
            }
        }
        // 异步消费
        SmqMessage message = (SmqMessage) redisService.template(db).opsForList().leftPop(dkey, this.consume_timeout,
                TimeUnit.MILLISECONDS);
        if (message == null) {
            if (this.sync) {
                // 释放同步锁
                redisService.putListValue(db,lkey, DateUtil.current());
            }
            return null;
        }
        return new SmqConsumer(this, message, this.process_timeout, lkey);
    }

    /**
     * 获取队列信息
     * 
     * @return
     */
    public SmqInfo info() {
        String ikey = String.format(SMQ_INFO, this.name);
        SmqInfo info = new SmqInfo();
        Map<String, Object> map = redisService.getMap(db, ikey);
        BeanUtil.fillBeanWithMap(map, info, true);
        return info;
    }

    /**
     * 确认消费成功或失败
     * 
     * @param message
     * @param success
     */
    @SuppressWarnings("unchecked")
    private void ack(SmqMessage message, boolean success, String... lock) {
        String ikey = String.format(SMQ_INFO, this.name);
        if (success) {
            redisService.template(db).opsForHash().increment(ikey, "success", 1L);
        } else {
            // 失败，添加异常数据加入失败队列
            String ekey = String.format(SMQ_ERROR, this.name);
            redisService.putListValue(db, ekey, message);
            redisService.template(db).opsForHash().put(ikey, "error", redisService.countList(db, ekey));
            redisService.template(db).opsForHash().put(ikey, "errorstamp", DateUtil.current());
        }
        if (this.sync) {
            // 同步消费:释放锁
            redisService.putListValue(db, lock[0], DateUtil.current());
        }
    }

    /**
     * 自动消费数据
     */
    private void autoConsume() {
        if (!this.auto) {
            return;
        }
        ThreadUtil.newThread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SmqMessage message = null;
                    try {
                        SmqConsumer consumer = consume();
                        if (consumer == null) {
                            continue;
                        }
                        message = consumer.getMessage();
                        // 如果是数据库同步消息
                        if (message.getData() instanceof SmqDbsd) {
                            boolean flag = process(message, (SmqDbsd) message.getData());
                            consumer.ack(flag);
                        } else {
                            // 其他消息，默认确认成功
                            consumer.ack(true);
                        }
                    } catch (Exception e) {
                        if (message != null) {
                            log.error("队列[{}]数据消费异常,name:{},id:{}", getName(), message.getName(), message.getId(), e);
                        } else {
                            log.error("队列[{}]数据消费异常", getName(), e);
                        }
                    }
                }
            }

            /**
             * 处理数据库同步消息
             * 
             * @param data
             * @return
             */
            private boolean process(SmqMessage message, SmqDbsd data) {
                try {
                    // 处理数据库同步消息
                    if ("i".equals(data.getAction())) {
                        try {
                            int len = dataBaseDao
                                    .insert(SqlBuilder.build(data.getTable(), data.getData()).schema(data.getSchema()));
                            return len > 0;
                        } catch (Exception e) {
                            // 如果是主键重复异常
                            if (isDuplicateKeyException(e)) {
                                if ("u".equals(data.getConflict())) {
                                    // 存在，更新
                                    int len = dataBaseDao.deleteById(
                                            SqlBuilder.build(data.getTable(), data.getData()).schema(data.getSchema()));
                                    return len > 0;
                                }
                            }
                            log.error("队列[{}]数据消费异常,name:{},id:{}", getName(), message.getName(), message.getId(), e);
                            return false;
                        }
                    } else if ("u".equals(data.getAction())) {
                        // 更新
                        int len = dataBaseDao.updateById(SqlBuilder.build(data.getTable(), data.getData())
                                .schema(data.getSchema()).id(data.getId()).field(data.getFields()));
                        return len > 0;
                    } else if ("d".equals(data.getAction())) {
                        // 删除
                        int len = dataBaseDao.deleteById(SqlBuilder.build(data.getTable(), data.getData())
                                .schema(data.getSchema()).id(data.getId()));
                        return len > 0;
                    }
                } catch (Exception e) {
                    log.error("队列[{}]数据自动消费异常,name:{},id:{},schema:{},table:{}",
                            getName(), message.getName(), message.getId(), data.getSchema(), data.getTable(), e);
                    return false;
                }
                return true;
            }

            private boolean isDuplicateKeyException(Throwable e) {
                if (e == null) {
                    return false;
                }
                if (e instanceof DuplicateKeyException) {
                    return true;
                }
                if (e instanceof SQLException) {
                    SQLException sqle = (SQLException) e;
                    if ("23000".equals(sqle.getSQLState()) || duplicateKeyErrorCodes.contains(sqle.getErrorCode())) {
                        return true;
                    }
                }
                // 处理 Spring 封装的 DataAccessException
                if (e instanceof DataAccessException) {
                    return isDuplicateKeyException(((DataAccessException) e).getRootCause());
                }
                // 递归查找异常链
                return isDuplicateKeyException(e.getCause());
            }

        }, "smq-consume-" + this.name).start();
    }

    public static class SmqException extends ServiceException {
        public SmqException(String message) {
            super(message);
        }
    }

    public static class SmqConsumer {
        private final SmqUtil smqUtil;
        private final SmqMessage message;
        private Timer timer;
        private String lock[];

        private SmqConsumer(SmqUtil smqUtil, SmqMessage message, long process_timeout, String... lock) {
            this.smqUtil = smqUtil;
            this.message = message;
            this.lock = lock;
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    smqUtil.ack(message, false);
                    throw new SmqException(String.format("队列[%s]消息处理超时,name:%s,id:%s", smqUtil.getName(),
                            message.getName(),message.getId()));
                }
            }, process_timeout);
        }

        public SmqMessage getMessage() {
            return BeanUtil.copyProperties(this.message, SmqMessage.class);
        }

        /**
         * 确认消费成功或失败
         * 
         * @param success
         */
        public void ack(boolean success) {
            smqUtil.ack(message, success, lock);
            timer.cancel();
        }
    }

    @Data
    public static class SmqMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(title = "数据id")
        private Long id;
        @Schema(title = "时间戳")
        private Long timestamp;
        @Schema(title = "数据名称")
        private String name;
        @Schema(title = "数据内容")
        private Object data;

        public SmqMessage() {
            this.id = IdGenHolder.generate();
            this.timestamp = DateUtil.current();
        }

        public SmqMessage(String name, Object data) {
            this();
            this.name = name;
            this.data = data;
        }
    }

    @Data
    @Schema(title = "数据库数据同步数据")
    public static class SmqDbsd implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(title = "数据库模式")
        private String schema;
        @Schema(title = "数据库表名")
        private String table;
        @Schema(title = "数据id")
        private Long id;
        @Schema(title = "操作类型", description = "i:插入,u:更新,d:删除")
        private String action;
        @Schema(title = "数据内容")
        private Object data;
        @Schema(title = "更新字段", description = "更新操作时指定的字段,逗号分隔")
        private String fields;
        @Schema(title = "冲突处理", description = "新增操作冲突处理逻辑：i:忽略，u:更新")
        private String conflict;
    }

    @Data
    public static class SmqInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(title = "队列名称")
        private String name;
        @Schema(title = "是否同步消费", description = "是否同步消费队列中的数据,如果为true,则多端消费时会阻塞,直到某个节点消费完成")
        private boolean sync;
        @Schema(title = "是否自动消费", description = "是否自动消费队列中的数据并写入数据库，仅DB类型支持")
        private boolean auto;
        @Schema(title = "队列大小", description = "当前队列中的数据量")
        private Long size;
        @Schema(title = "历史消费量", description = "")
        private Long total;
        @Schema(title = "成功消费量", description = "当前成功队列中的数据量")
        private Long success;
        @Schema(title = "异常消息量", description = "当前异常队列中的数据量")
        private Long error;
        @Schema(title = "创建时间", description = "队列首次创建时间")
        private Long created;
        @Schema(title = "最近活动时间", description = "队列最近一次活动时间")
        private Long timestamp;
        @Schema(title = "最近异常时间", description = "队列最近一次异常时间")
        private Long errorstamp;
        @Schema(title = "最近监测时间", description = "队列最近一次队列监测时间")
        private Long lifestamp;
    }

}
