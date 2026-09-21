package com.example.proj.service;

import com.example.core.cache.ICacheService;
import com.example.core.exception.NotFoundException;
import com.example.generated.dto.ApiResponseString;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheDemoService {

    private static final String CACHE_DEMO_ENTRY_NOT_FOUND = "cache.demo.entry.not-found";

    private final ICacheService cacheService;

    public String getCacheDemo(String key) {
        String value = cacheService.get(key, String.class);

        if (value == null) {
            throw new NotFoundException(CACHE_DEMO_ENTRY_NOT_FOUND);
        }

        return value;
    }

    public void putCacheDemo(String key, String value) {
        cacheService.put(key, value);
    }

    public void deleteCacheDemo(String key) {
        cacheService.delete(key);
    }
}