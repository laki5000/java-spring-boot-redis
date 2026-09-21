package com.example.integration.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.core.exception.CacheException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceUnitTests {

  private static final String CACHE_KEY = "user:123";
  private static final String CACHE_VALUE = "cached value";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);
  private static final String ERROR_MESSAGE = "Redis error";

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private ValueOperations<String, Object> valueOperations;

  private RedisCacheService redisCacheService;

  @BeforeEach
  void setUp() {
    redisCacheService = new RedisCacheService(redisTemplate);
  }

  @Test
  void testGet_shouldReturnCachedValue_whenKeyExists() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenReturn(CACHE_VALUE);

    // When
    String result = redisCacheService.get(CACHE_KEY, String.class);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(redisTemplate).opsForValue();
    verify(valueOperations).get(CACHE_KEY);
  }

  @Test
  void testGet_shouldReturnNull_whenKeyDoesNotExist() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenReturn(null);

    // When
    String result = redisCacheService.get(CACHE_KEY, String.class);

    // Then
    assertNull(result);
    verify(redisTemplate).opsForValue();
    verify(valueOperations).get(CACHE_KEY);
  }

  @Test
  void testGet_shouldReturnValueOfRequestedType_whenCachedValueHasExpectedType() {
    // Given
    Integer cachedValue = 123;

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenReturn(cachedValue);

    // When
    Integer result = redisCacheService.get(CACHE_KEY, Integer.class);

    // Then
    assertEquals(cachedValue, result);
    verify(redisTemplate).opsForValue();
    verify(valueOperations).get(CACHE_KEY);
  }

  @Test
  void testGet_shouldThrowCacheException_whenRedisOperationFails() {
    // Given
    RuntimeException cause = new RuntimeException(ERROR_MESSAGE);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenThrow(cause);

    // When
    CacheException exception =
        assertThrows(CacheException.class, () -> redisCacheService.get(CACHE_KEY, String.class));

    // Then
    assertEquals("Failed to get value from cache for key: " + CACHE_KEY, exception.getMessage());
    assertSame(cause, exception.getCause());
    verify(redisTemplate).opsForValue();
    verify(valueOperations).get(CACHE_KEY);
  }

  @Test
  void testPut_shouldStoreValue_whenTtlIsNotProvided() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // When
    redisCacheService.put(CACHE_KEY, CACHE_VALUE);

    // Then
    verify(redisTemplate).opsForValue();
    verify(valueOperations).set(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testPut_shouldThrowCacheException_whenRedisOperationFails() {
    // Given
    RuntimeException cause = new RuntimeException(ERROR_MESSAGE);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(cause).when(valueOperations).set(CACHE_KEY, CACHE_VALUE);

    // When
    CacheException exception =
        assertThrows(CacheException.class, () -> redisCacheService.put(CACHE_KEY, CACHE_VALUE));

    // Then
    assertEquals("Failed to put value into cache for key: " + CACHE_KEY, exception.getMessage());
    assertSame(cause, exception.getCause());
    verify(redisTemplate).opsForValue();
    verify(valueOperations).set(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testPut_shouldStoreValueWithTtl_whenTtlIsProvided() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // When
    redisCacheService.put(CACHE_KEY, CACHE_VALUE, CACHE_TTL);

    // Then
    verify(redisTemplate).opsForValue();
    verify(valueOperations).set(CACHE_KEY, CACHE_VALUE, CACHE_TTL);
  }

  @Test
  void testPut_shouldThrowCacheException_whenRedisOperationWithTtlFails() {
    // Given
    RuntimeException cause = new RuntimeException(ERROR_MESSAGE);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(cause).when(valueOperations).set(CACHE_KEY, CACHE_VALUE, CACHE_TTL);

    // When
    CacheException exception =
        assertThrows(
            CacheException.class, () -> redisCacheService.put(CACHE_KEY, CACHE_VALUE, CACHE_TTL));

    // Then
    assertEquals("Failed to put value into cache for key: " + CACHE_KEY, exception.getMessage());
    assertSame(cause, exception.getCause());
    verify(redisTemplate).opsForValue();
    verify(valueOperations).set(CACHE_KEY, CACHE_VALUE, CACHE_TTL);
  }

  @Test
  void testDelete_shouldDeleteKey() {
    // When
    redisCacheService.delete(CACHE_KEY);

    // Then
    verify(redisTemplate).delete(CACHE_KEY);
  }

  @Test
  void testDelete_shouldThrowCacheException_whenRedisOperationFails() {
    // Given
    RuntimeException cause = new RuntimeException(ERROR_MESSAGE);

    when(redisTemplate.delete(CACHE_KEY)).thenThrow(cause);

    // When
    CacheException exception =
        assertThrows(CacheException.class, () -> redisCacheService.delete(CACHE_KEY));

    // Then
    assertEquals("Failed to delete cache entry for key: " + CACHE_KEY, exception.getMessage());
    assertSame(cause, exception.getCause());
    verify(redisTemplate).delete(CACHE_KEY);
  }
}
