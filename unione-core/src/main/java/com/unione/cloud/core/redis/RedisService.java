package com.unione.cloud.core.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.exception.ServiceException;


/**
 * @Redis 服务对象
 * 
 * @author Jeking Yang
 * @version 5.0.0
 * @since 2019.04.22
 */
@Service
@RefreshScope
public class RedisService {
	private Logger logger=LoggerFactory.getLogger(RedisService.class);
	
	@Autowired
	private RedisConfig redisConfig;
	
	@Autowired
	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;
	
	@Autowired
	private RedisMessageListenerContainer redisMessageListenerContainer;
	
	
	/**
	 * @发布消息过期时间，默认30秒，单位秒
	 */
	@Value("${redis.service.msg.timeout:30}")
	private Long PUBLISH_TIMEOUT;
	
	
	
	/**
	 * @设置Object数据
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void put(String key,Object value) {
		logger.debug("设置Object数据，Key:{}",key);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		valueOps.set(key, value);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void put(int db, String key,Object value) {
		logger.debug("设置Object数据，db:{},Key:{},value:{}",db,key,value);
		
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		valueOps.set(key, value);
	}
	
	/**
	 * @设置Object数据
	 * @param key
	 * @param value
	 * @param timeout
	 */
	@SuppressWarnings("unchecked")
	public void put(String key,Object value,Duration timeout) {
		logger.debug("设置Object数据，Key:{}",key);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		valueOps.set(key, value,timeout);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void put(int db, String key,Object value,Duration timeout) {
		logger.debug("设置Object数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		valueOps.set(key, value,timeout);
	}
	
	
	/**
	 * 	设置Object数据
	 * @param key
	 * @param value
	 * @param timeout
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean putIfAbsent(String key,Object value,Duration timeout) {
		logger.debug("设置Object数据，Key:{}",key);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		return valueOps.setIfAbsent(key, value,timeout);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean putIfAbsent(int db, String key,Object value,Duration timeout) {
		logger.debug("设置Object数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		return valueOps.setIfAbsent(key, value,timeout);
	}
	
	
	/**
	 * @设置Map数据
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putMap(String key,Map<String, ?> value) {
		logger.debug("设置Map数据，Key:{}",key);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.putAll(key, value);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putMap(int db, String key,Map<String, ?> value) {
		logger.debug("设置Map数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.putAll(key, value);
	}
	
	/**
	 * 	@设置Map数据
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putMap(String key,String hashKey,Object value) {
		logger.debug("设置Map数据，Key:{}",key);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.put(key, hashKey, value);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putMap(int db, String key,String hashKey,Object value) {
		logger.debug("设置Map数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.put(key, hashKey, value);
	}
	
	/**
	 * @设置set数据
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putSet(String key,Object... values) {
		logger.debug("设置set数据，Key:{}",key);
		SetOperations<String, Object> setOps=redisTemplate.opsForSet();
		setOps.add(key, values);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putSet(int db, String key,Object... values) {
		logger.debug("设置set数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		SetOperations<String, Object> setOps=redisTemplate.opsForSet();
		setOps.add(key, values);
	}
	
	
	/**
	 * @设置List数据
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putList(String key,List<?> value) {
		logger.debug("设置List数据，Key:{}",key);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.rightPushAll(key, value);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putList(int db, String key,List<?> value) {
		logger.debug("设置List数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.rightPushAll(key, value);
	}
	
	
	/**
	 * 	@设置List数据
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putListValue(String key,Object value) {
		logger.debug("设置List数据，Key:{}",key);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.rightPush(key, value);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putListValue(int db, String key,Object value) {
		logger.debug("设置List数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.rightPush(key, value);
	}
	
	/**
	 * @设置List数据
	 * @param key
	 * @param index
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putListValue(String key,int index,Object value) {
		logger.debug("设置List数据，Key:{}",key);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.set(key, index, value);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putListValue(int db, String key,int index,Object value) {
		logger.debug("设置List数据，db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		listOps.set(key, index, value);
	}
	
	
	/**
	 * @设置Object数据并发布消息
	 * @param key
	 * @param value
	 */
	public void putAndPublish(String key,Object value) {
		this.put(key, value,Duration.ofSeconds(PUBLISH_TIMEOUT));
		this.publish(key, "updated");
	}
	
	/**
	 * @设置Map数据并发布消息
	 * @param key
	 * @param value
	 */
	@Deprecated
	public void putAndPublish(String key,Map<String, Object> value) {
		this.put(key, value,Duration.ofSeconds(PUBLISH_TIMEOUT));
		this.publish(key, "updated");
	}
	
	
	/**
	 * @获得obj数据
	 * @param key
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObj(String key) {
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		return (T)valueOps.get(key);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getObj(int db,String key) {
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		return (T)valueOps.get(key);
	}
	
	/**
	 * 	获得obj操作对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ValueOperations<String, Object> getObjOps(){
		return redisTemplate.opsForValue();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ValueOperations<String, Object> getObjOps(int db){
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		return redisTemplate.opsForValue();
	}
	
	/**
	 * @获得list数据集合
	 * @param key
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String key) {
		logger.debug("获得数据，Key:{}",key);
		if(redisTemplate!=null) {
			ListOperations<String, Object> listOps = redisTemplate.opsForList();
			long size=listOps.size(key);
			return(List<T>)listOps.range(key, 0, size);
		}
		logger.warn("未开启redis服务,缓存获取失败,key:{}",key);
		return null;
	}
	
	
	
	/**
	 * @获得list指定元素
	 * @param key
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getListValue(String key,int index) {
		logger.debug("获得数据，Key:{}",key);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		return (T)listOps.index(key, index);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getListValue(int db ,String key,int index) {
		logger.debug("获得数据，Key:{}",key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		ListOperations<String, Object> listOps = redisTemplate.opsForList();
		return (T)listOps.index(key, index);
	}
	
	/**
	 * 	获得list操作对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ListOperations<String, Object> getListOps(){
		return redisTemplate.opsForList();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ListOperations<String, Object> getListOps(int db){
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		return redisTemplate.opsForList();
	}
	
	/**
	 * @获得map数据集合
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getMap(String key) {
		logger.debug("获得数据，Key:{}",key);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		return (Map<String, T>)hashOps.entries(key);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Map<String, T> getMap(int db,String key) {
		logger.debug("获得数据，db:{},Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		return (Map<String, T>)hashOps.entries(key);
	}
	
	/**
	 * 	获得map hash value
	 * @param key
	 * @param hashKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMap(String key,String hashKey) {
		logger.debug("获得数据，Key:{}",key);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		return (T)hashOps.get(key, hashKey);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getMap(int db,String key,String hashKey) {
		logger.debug("获得数据,db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		return (T)hashOps.get(key, hashKey);
	}
	
	/**
	 * 	批量获得数据 对象
	 * @param key
	 * @param hashKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getMap(String key,Set<String> hashKeys) {
		logger.debug("批量获得数据，Key:{}",key);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		List<T> list = (List<T>)hashOps.multiGet(key, hashKeys);
		return list.stream().filter(item->item!=null).collect(Collectors.toList());
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> getMap(int db,String key,Set<String> hashKeys) {
		logger.debug("批量获得数据,db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		List<T> list = (List<T>)hashOps.multiGet(key, hashKeys);
		return list.stream().filter(item->item!=null).collect(Collectors.toList());
	}
	
	
	/**
	 * 	获得map操作对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HashOperations<String, String, Object> getMapOps(){
		return redisTemplate.opsForHash();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashOperations<String, String, Object> getMapOps(int db){
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		return redisTemplate.opsForHash();
	}
	
	/**
	 * 	获得set集合
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> getSet(String key) {
		SetOperations<String, T> setOps=redisTemplate.opsForSet();
		return setOps.members(key);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Set<T> getSet(int db,String key) {
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		SetOperations<String, T> setOps=redisTemplate.opsForSet();
		return setOps.members(key);
	}
	
	
	/**
	 * @删除数据
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public void delete(String key) {
		logger.debug("删除数据，Key:{}",key);
		redisTemplate.delete(key);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(int db,String key) {
		logger.debug("删除数据,db:{}，Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		redisTemplate.delete(key);
	}
	
	/**
	 * 	删除map hashKey
	 * @param key
	 * @param hashKey
	 */
	@SuppressWarnings("unchecked")
	public void deleteMapHash(String key,String hashKey) {
		logger.debug("删除map hashKey，Key:{},hashKey:{}",key,hashKey);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.delete(key, hashKey);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteMapHash(int db,String key,String hashKey) {
		logger.debug("删除map hashKey,db:{}，Key:{},hashKey:{}",db,key,hashKey);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
		hashOps.delete(key, hashKey);
	}
	
	/**
	 * 	删除Set value 元素
	 * @param key
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public void deleteSetValue(String key,Object ...values) {
		SetOperations<String, Object> setOps=redisTemplate.opsForSet();
		setOps.remove(key, values);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteSetValue(int db,String key,Object ...values) {
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		SetOperations<String, Object> setOps=redisTemplate.opsForSet();
		setOps.remove(key, values);
	}
	
	/**
	 * @订阅key消息
	 * @param key
	 * @param listener
	 */
	public void subscribe(String key,MessageListener listener) {
		logger.debug("订阅消息，Key:{}",key);
		redisMessageListenerContainer.addMessageListener(listener, new ChannelTopic(key));
	}
	public void subscribe(int db,String key,MessageListener listener) {
		logger.debug("订阅消息，db:{},Key:{}",db,key);
		redisConfig.getRedisMessageListener(db).addMessageListener(listener, new ChannelTopic(key));
	}
	
	/**
	 * @发布消息
	 * @param key
	 * @param message
	 */
	public void publish(String key,Object message) {
		logger.debug("发布消息，Key:{}",key);
		redisTemplate.convertAndSend(key, message);
	}
	@SuppressWarnings("rawtypes")
	public void publish(int db,String key,Object message) {
		logger.debug("发布消息，db:{},Key:{}",db,key);
		RedisTemplate redisTemplate=redisConfig.getRedisTmpls(db);
		redisTemplate.convertAndSend(key, message);
	}
	
	/**
	 * 	高可用分布式锁 :do Hight Process Distributed locks。基于redis分布式锁实现
	 * 	1、验证尝试次数是否到达限制,tryCount>0，如果tryCount<=0则抛出异常
	 * 	2、尝试获取redis分布式锁，成功获取则执行处理方法
	 * 	3、获取失败，则sleep指定时间，然后重试
	 * 
	 * @param process		处理逻辑方法
	 * @param ptime			处理时间（redis分布式锁有效时间），单位毫秒
	 * @param tcount		尝试次数
	 */
	public <T> T doHpdl(HpdlProcess<T> process,long ptime,long stime,int tcount) {
		logger.info("进入:高可用分布式锁处理方法,process id:{},hpdl name:{},process time:{},sleep time:{},try count:{}",process,process.getHpdlName(),ptime,stime,tcount);
		T result=null;
		if(tcount<=0) {
			return null;
		}
		
		boolean lock=putIfAbsent(String.format("HPDL:%s", process.getHpdlName()), true, Duration.ofMillis(ptime));
		if(lock) {
			try {
				result=process.process();
			} catch (Exception e) {
				logger.error("高可用分布式锁处理异常",e);
				throw new ServiceException("高可用分布式锁处理异常",e);
			} finally {
				// 删除全局事务锁
				delete(String.format("HPDL:%s", process.getHpdlName()));
			}
			logger.info("退出:高可用分布式锁处理方法,process id:{},hpdl name:{},process time:{},sleep time:{},try count:{}",process,process.getHpdlName(),ptime,stime,tcount);
		}else {
			try {
				Thread.sleep(stime);
				result=doHpdl(process, ptime, stime, tcount-1);
			} catch (InterruptedException e) {
				logger.error("退出:高可用分布式锁处理方法,process id:{},hpdl name:{},process time:{},sleep time:{},try count:{}",process,process.getHpdlName(),ptime,stime,tcount,e);
				return null;
			}
		}
		return result;
	}
	
	public <T> T doHpdl(HpdlProcess<T> process,long ptime,long stime) {
		return doHpdl(process, ptime, stime, 1);
	}
	

}
