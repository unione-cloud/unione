package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class UniOneLettuceConnectionConfiguration extends LettuceConnectionConfiguration {

	public UniOneLettuceConnectionConfiguration(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
		super(properties, standaloneConfigurationProvider, sentinelConfigurationProvider, clusterConfigurationProvider);
	}
	
	public LettuceConnectionFactory redisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		LettuceConnectionFactory connectionFactory=super.redisConnectionFactory(builderCustomizers, super.lettuceClientResources(customizers));
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}


}
