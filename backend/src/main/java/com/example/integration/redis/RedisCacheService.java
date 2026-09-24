package com.example.integration.redis;

import com.example.core.cache.ICacheService;
import com.example.core.exception.CacheException;
import com.example.core.logging.LogExecution;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class RedisCacheService implements ICacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  @LogExecution(level = Level.DEBUG, logArguments = true)
  @Override
  public <T> T get(String key, Class<T> type) {
    try {
      Object value = redisTemplate.opsForValue().get(key);

      return type.cast(value);
    } catch (RuntimeException e) {
      throw new CacheException("Failed to get value from cache for key: " + key, e);
    }
  }

  @LogExecution(
      level = Level.DEBUG,
      logArguments = true,
      argumentIndexes = {0})
  @Override
  public <T> void put(String key, T value) {
    try {
      redisTemplate.opsForValue().set(key, value);
    } catch (RuntimeException e) {
      throw new CacheException("Failed to put value into cache for key: " + key, e);
    }
  }

  @LogExecution(
      level = Level.DEBUG,
      logArguments = true,
      argumentIndexes = {0, 2})
  @Override
  public <T> void put(String key, T value, Duration ttl) {
    try {
      redisTemplate.opsForValue().set(key, value, ttl);
    } catch (RuntimeException e) {
      throw new CacheException("Failed to put value into cache for key: " + key, e);
    }
  }

  @LogExecution(level = Level.DEBUG, logArguments = true)
  @Override
  public void delete(String key) {
    try {
      redisTemplate.delete(key);
    } catch (RuntimeException e) {
      throw new CacheException("Failed to delete cache entry for key: " + key, e);
    }
  }
}
