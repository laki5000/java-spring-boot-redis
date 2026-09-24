package com.example.integration.redis;

import com.example.core.cache.ICacheExpirationHandler;
import com.example.core.cache.ICacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "cache.provider", havingValue = "redis")
public class RedisConfig {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new JacksonJsonRedisSerializer<>(Object.class));
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new JacksonJsonRedisSerializer<>(Object.class));

    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory, RedisExpirationListener expirationListener) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();

    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(expirationListener, new PatternTopic("__keyevent@*__:expired"));

    return container;
  }

  @Bean
  public ICacheService cacheService(RedisTemplate<String, Object> redisTemplate) {

    return new RedisCacheService(redisTemplate);
  }

  @Bean
  public RedisExpirationListener redisExpirationListener(ICacheExpirationHandler handler) {

    return new RedisExpirationListener(handler);
  }
}
