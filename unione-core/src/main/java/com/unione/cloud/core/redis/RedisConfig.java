package com.unione.cloud.core.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.UniOneJedisConnectionConfiguration;
import org.springframework.boot.autoconfigure.data.redis.UniOneLettuceConnectionConfiguration;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
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
@EnableConfigurationProperties(RedisProperties.class)
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

	@Autowired
	private RedisProperties redisProperties;
	
	@Value("${spring.redis.client-type:lettuce}")
	private String redisClientType;
	
	@Autowired
	private ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider;
	@Autowired
	private ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration;
	@Autowired
	private ObjectProvider<RedisClusterConfiguration> clusterConfiguration;
	@Autowired
	private ObjectProvider<ClientResourcesBuilderCustomizer> customizers;
	@Autowired
	private ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers;
	@Autowired
	private ObjectProvider<JedisClientConfigurationBuilderCustomizer> jedisBuilderCustomizers;
	
	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
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
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public RedisTemplate<String, Object> getRedisTmpls(int db) {
		try {
			if (redisTmplMap.get(db) != null) {
				return redisTmplMap.get(db);
			}

			synchronized (redisTmplMap) {
				RedisProperties props = new RedisProperties();
				BeanUtils.copyProperties(props,redisProperties);
				props.setDatabase(db);
				
				RedisTemplate<String, Object> tmpls = new RedisTemplate<>();
				
				if("lettuce".equalsIgnoreCase(redisClientType)) {
					UniOneLettuceConnectionConfiguration config = new UniOneLettuceConnectionConfiguration(props,
							standaloneConfigurationProvider, sentinelConfiguration, clusterConfiguration);
					RedisConnectionFactory redisConnectionFactory = config.redisConnectionFactory(builderCustomizers,customizers);
					tmpls.setConnectionFactory(redisConnectionFactory);
				}else {
					UniOneJedisConnectionConfiguration config = new UniOneJedisConnectionConfiguration(props,
							standaloneConfigurationProvider, sentinelConfiguration, clusterConfiguration);
					RedisConnectionFactory redisConnectionFactory = config.redisConnectionFactory(jedisBuilderCustomizers);
					tmpls.setConnectionFactory(redisConnectionFactory);
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
	public RedisMessageListenerContainer getRedisMessageListener(int db) {
		if(redisListenerMap.get(db)!=null) {
			return redisListenerMap.get(db);
		}
		synchronized(redisListenerMap) {
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory((RedisConnectionFactory)getRedisTmpls(db));
			redisListenerMap.put(db, container);
			return container;
		}
	}

}
