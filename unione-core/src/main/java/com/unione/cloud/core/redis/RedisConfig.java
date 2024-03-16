package com.unione.cloud.core.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unione.cloud.core.exception.ServiceException;

/**
 * @Redis 服务配置
 * @author Jeking Yang
 * @version 1.0.0
 */
@Configuration
public class RedisConfig {

	private Logger log = LoggerFactory.getLogger(RedisConfig.class);

	/**
	 * redis tmpl map
	 */
	@SuppressWarnings("rawtypes")
	private static Map<Integer, RedisTemplate> redisTmplMap = new ConcurrentHashMap<>();

	private static Map<Integer, RedisMessageListenerContainer> redisListenerMap = new ConcurrentHashMap<>();

	@Autowired
	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;

	
	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked", "null" })
	@PostConstruct
	public void postConstruct() {
		// 默认redis库处理
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        
		if(redisTemplate.getConnectionFactory() instanceof LettuceConnectionFactory) {
			LettuceConnectionFactory connectionFactory=(LettuceConnectionFactory)redisTemplate.getConnectionFactory();
			redisTmplMap.put(connectionFactory.getDatabase(), redisTemplate);
		}else {
			JedisConnectionFactory connectionFactory=(JedisConnectionFactory)redisTemplate.getConnectionFactory();
			redisTmplMap.put(connectionFactory.getDatabase(), redisTemplate);
		}
		
	}

	@SuppressWarnings("null")
	@Bean(destroyMethod = "destroy")
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		if(redisTemplate.getConnectionFactory() instanceof LettuceConnectionFactory) {
			LettuceConnectionFactory connectionFactory=(LettuceConnectionFactory)redisConnectionFactory;
			redisListenerMap.put(connectionFactory.getDatabase(), container);		
		}else {
			JedisConnectionFactory connectionFactory=(JedisConnectionFactory)redisTemplate.getConnectionFactory();
			redisListenerMap.put(connectionFactory.getDatabase(), container);	
		}
		return container;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public RedisTemplate<String, Object> stringSerializerRedisTemplate() {
		RedisSerializer<String> stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setHashKeySerializer(stringSerializer);
		return redisTemplate;
	}
	
	
	/**
	 * 	获取指定redis db template
	 * @param db
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation", "null" })
	public RedisTemplate<String, Object> getRedisTmpls(int db) {
		try {
			if (redisTmplMap.get(db) != null) {
				return redisTmplMap.get(db);
			}

			synchronized (redisTmplMap) {
				RedisTemplate<String, Object> tmpls = new RedisTemplate<>();
				RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
				if(connectionFactory instanceof LettuceConnectionFactory){
					LettuceConnectionFactory lettuceConnectionFactory=(LettuceConnectionFactory)connectionFactory;
					LettuceConnectionFactory newConnectionFactory=null;
					if(lettuceConnectionFactory.getClusterConfiguration()!=null){
						newConnectionFactory=new LettuceConnectionFactory(lettuceConnectionFactory.getClusterConfiguration());
					}else{
						newConnectionFactory=new LettuceConnectionFactory(lettuceConnectionFactory.getStandaloneConfiguration());
					}
					newConnectionFactory.setDatabase(db);
					newConnectionFactory.afterPropertiesSet();
					tmpls.setConnectionFactory(newConnectionFactory);
				}else{
					throw new ServiceException("当前版本不支持Jedis");
				}
				
				RedisSerializer<String> stringSerializer = new StringRedisSerializer();
				tmpls.setKeySerializer(stringSerializer);
				tmpls.setHashKeySerializer(stringSerializer);
				
				Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
				objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
				jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
				
				tmpls.setValueSerializer(jackson2JsonRedisSerializer);
				tmpls.setHashValueSerializer(jackson2JsonRedisSerializer);
				tmpls.afterPropertiesSet();
				
				redisTmplMap.put(db, tmpls);
				return tmpls;
			}
		} catch (Exception e) {
			log.error("创建redisTemplate失败,db:{}", db, e);
			throw new ServiceException("创建redisTemplate失败,db:"+db,e);
		}
	}

	
	/**
	 * 	获取指定 redis db message 监听管理器
	 * @param db
	 * @return
	 */
	@SuppressWarnings("null")
	public RedisMessageListenerContainer getRedisMessageListener(int db) {
		if(redisListenerMap.get(db)!=null) {
			return redisListenerMap.get(db);
		}
		synchronized(redisListenerMap) {
			if(getRedisTmpls(db)==null){
				throw new ServiceException(String.format("RedistTemplate获取失败,db:%s", db));
			}
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory(getRedisTmpls(db).getConnectionFactory());
			redisListenerMap.put(db, container);
			return container;
		}
	}

}
