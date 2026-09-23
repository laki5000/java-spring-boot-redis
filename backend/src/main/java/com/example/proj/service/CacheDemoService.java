package com.example.proj.service;

import com.example.core.cache.CachePut;
import com.example.core.cache.Cacheable;
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

    private static final String CACHEABLE_DEMO_KEY = "cacheable-demo";
    private static final String CACHE_PUT_DEMO_KEY = "cache-put-demo";
    private static final String CACHEABLE_DEMO_VALUE = "value-for-cacheable-demo";
    private static final String CACHE_PUT_DEMO_VALUE = "value-for-cache-put-demo";
    private static final long CACHEABLE_DEMO_DELAY_MILLIS = 3_000;
    private static final long CACHEABLE_DEMO_TTL_SECONDS = 60;

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

    @Cacheable(key = CACHEABLE_DEMO_KEY, ttlSeconds = CACHEABLE_DEMO_TTL_SECONDS)
    @LogExecution(level = Level.DEBUG, logArguments = true)
    public String getCacheableDemo() {
        sleep();

        return CACHEABLE_DEMO_VALUE;
    }

    @CachePut(key = CACHE_PUT_DEMO_KEY, ttlSeconds = CACHEABLE_DEMO_TTL_SECONDS)
    @LogExecution(level = Level.DEBUG, logArguments = true, argumentIndexes = {0})
    public String putCachePutDemo() {
        return CACHE_PUT_DEMO_VALUE;
    }

    private void sleep() {
        try {
            Thread.sleep(CacheDemoService.CACHEABLE_DEMO_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted", e);
        }
    }
}