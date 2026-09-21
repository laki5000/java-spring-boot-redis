package com.example.proj.service;

import com.example.core.cache.ICacheService;
import com.example.core.exception.NotFoundException;
import com.example.core.logging.LogExecution;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheDemoService {

  private static final String CACHE_DEMO_ENTRY_NOT_FOUND = "cache.demo.entry.not-found";

  private final ICacheService cacheService;

  @LogExecution(level = Level.DEBUG, logArguments = true)
  public String getCacheDemo(String key) {
    String value = cacheService.get(key, String.class);

    if (value == null) {
      throw new NotFoundException(CACHE_DEMO_ENTRY_NOT_FOUND);
    }

    return value;
  }

  @LogExecution(
      level = Level.DEBUG,
      logArguments = true,
      argumentIndexes = {0, 2})
  public void putCacheDemo(String key, String value, Long ttlSeconds) {
    if (ttlSeconds != null) {
      cacheService.put(key, value, Duration.ofSeconds(ttlSeconds));
      return;
    }

    cacheService.put(key, value);
  }

  @LogExecution(level = Level.DEBUG, logArguments = true)
  public void deleteCacheDemo(String key) {
    cacheService.delete(key);
  }
}
