package com.example.proj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.core.cache.ICacheService;
import com.example.core.exception.NotFoundException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheDemoServiceUnitTests {

  private static final String CACHE_KEY = "key";
  private static final String CACHE_VALUE = "value";
  private static final long TTL_SECONDS = 60;
  private static final Duration CACHE_TTL = Duration.ofSeconds(TTL_SECONDS);
  private static final String NOT_FOUND_MESSAGE = "cache.demo.entry.not-found";

  @Mock private ICacheService cacheService;

  private CacheDemoService cacheDemoService;

  @BeforeEach
  void setUp() {
    cacheDemoService = new CacheDemoService(cacheService);
  }

  @Test
  void testGetCacheDemo_shouldReturnValue_whenCacheEntryExists() {
    // Given
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(CACHE_VALUE);

    // When
    String result = cacheDemoService.getCacheDemo(CACHE_KEY);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
  }

  @Test
  void testGetCacheDemo_shouldThrowNotFoundException_whenCacheEntryDoesNotExist() {
    // Given
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);

    // When
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> cacheDemoService.getCacheDemo(CACHE_KEY));

    // Then
    assertEquals(NOT_FOUND_MESSAGE, exception.getMessage());
    verify(cacheService).get(CACHE_KEY, String.class);
  }

  @Test
  void testPutCacheDemo_shouldPutValueWithoutTtl_whenTtlIsNotProvided() {
    // When
    cacheDemoService.putCacheDemo(CACHE_KEY, CACHE_VALUE, null);

    // Then
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testPutCacheDemo_shouldPutValueWithTtl_whenTtlIsProvided() {
    // When
    cacheDemoService.putCacheDemo(CACHE_KEY, CACHE_VALUE, TTL_SECONDS);

    // Then
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE, CACHE_TTL);
  }

  @Test
  void testDeleteCacheDemo_shouldDeleteCacheEntry() {
    // When
    cacheDemoService.deleteCacheDemo(CACHE_KEY);

    // Then
    verify(cacheService).delete(CACHE_KEY);
  }
}
