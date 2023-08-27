package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class UniOneJedisConnectionConfiguration extends JedisConnectionConfiguration {

	public UniOneJedisConnectionConfiguration(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
			ObjectProvider<RedisClusterConfiguration> clusterConfiguration) {
		super(properties, standaloneConfigurationProvider, sentinelConfiguration, clusterConfiguration);
	}

	public JedisConnectionFactory redisConnectionFactory(
			ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisConnectionFactory connectionFactory=super.redisConnectionFactory(builderCustomizers);
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}


}
