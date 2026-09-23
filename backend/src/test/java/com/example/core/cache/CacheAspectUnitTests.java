package com.example.core.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.core.exception.CacheException;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheAspectUnitTests {

  private static final String CACHE_KEY = "user:123";
  private static final String CACHE_VALUE = "cached value";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);
  private static final long CACHE_TTL_SECONDS = 600;
  private static final String EXCEPTION_MESSAGE = "test";

  @Mock private ICacheService cacheService;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private MethodSignature methodSignature;

  @Mock private Cacheable cacheable;

  @Mock private CachePut cachePut;

  private CacheAspect cacheAspect;

  @BeforeEach
  void setUp() {
    cacheAspect = new CacheAspect(cacheService);
  }

  @Test
  void testCacheable_shouldReturnCachedValue_whenValueExistsInCache() throws Throwable {
    // Given
    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
  }

  @Test
  void testCacheable_shouldProceedAndCacheResult_whenValueDoesNotExistInCache() throws Throwable {
    // Given
    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testCacheable_shouldProceedAndCacheResultWithTtl_whenTtlIsProvided() throws Throwable {
    // Given
    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(cacheable.ttlSeconds()).thenReturn(CACHE_TTL_SECONDS);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE, CACHE_TTL);
  }

  @Test
  void testCacheable_shouldReturnNullAndNotCache_whenProceedReturnsNull() throws Throwable {
    // Given
    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);
    when(joinPoint.proceed()).thenReturn(null);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertNull(result);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
  }

  @Test
  void testCacheable_shouldProceedAndCacheResult_whenCacheGetFails() throws Throwable {
    // Given
    CacheException cacheException = new CacheException(EXCEPTION_MESSAGE, new RuntimeException());

    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenThrow(cacheException);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testCacheable_shouldReturnResult_whenCachePutFails() throws Throwable {
    // Given
    CacheException cacheException = new CacheException(EXCEPTION_MESSAGE, new RuntimeException());

    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);
    doThrow(cacheException).when(cacheService).put(CACHE_KEY, CACHE_VALUE);

    // When
    Object result = cacheAspect.cacheable(joinPoint, cacheable);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testCacheable_shouldPropagateException_whenProceedFails() throws Throwable {
    // Given
    RuntimeException expectedException = new RuntimeException(EXCEPTION_MESSAGE);

    when(cacheable.key()).thenReturn(CACHE_KEY);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getReturnType()).thenReturn(String.class);
    when(cacheService.get(CACHE_KEY, String.class)).thenReturn(null);
    when(joinPoint.proceed()).thenThrow(expectedException);

    // When
    RuntimeException actualException =
        assertThrows(RuntimeException.class, () -> cacheAspect.cacheable(joinPoint, cacheable));

    // Then
    assertSame(expectedException, actualException);
    verify(cacheService).get(CACHE_KEY, String.class);
    verify(joinPoint).proceed();
  }

  @Test
  void testCachePut_shouldProceedAndCacheResult_whenTtlIsNotProvided() throws Throwable {
    // Given
    when(cachePut.key()).thenReturn(CACHE_KEY);
    when(cachePut.ttlSeconds()).thenReturn(0L);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cachePut(joinPoint, cachePut);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testCachePut_shouldProceedAndCacheResultWithTtl_whenTtlIsProvided() throws Throwable {
    // Given
    when(cachePut.key()).thenReturn(CACHE_KEY);
    when(cachePut.ttlSeconds()).thenReturn(CACHE_TTL_SECONDS);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);

    // When
    Object result = cacheAspect.cachePut(joinPoint, cachePut);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE, CACHE_TTL);
  }

  @Test
  void testCachePut_shouldReturnNullAndNotCache_whenProceedReturnsNull() throws Throwable {
    // Given
    when(joinPoint.proceed()).thenReturn(null);

    // When
    Object result = cacheAspect.cachePut(joinPoint, cachePut);

    // Then
    assertNull(result);
    verify(joinPoint).proceed();
  }

  @Test
  void testCachePut_shouldReturnResult_whenCachePutFails() throws Throwable {
    // Given
    CacheException cacheException = new CacheException(EXCEPTION_MESSAGE, new RuntimeException());

    when(cachePut.key()).thenReturn(CACHE_KEY);
    when(cachePut.ttlSeconds()).thenReturn(0L);
    when(joinPoint.proceed()).thenReturn(CACHE_VALUE);
    doThrow(cacheException).when(cacheService).put(CACHE_KEY, CACHE_VALUE);

    // When
    Object result = cacheAspect.cachePut(joinPoint, cachePut);

    // Then
    assertEquals(CACHE_VALUE, result);
    verify(joinPoint).proceed();
    verify(cacheService).put(CACHE_KEY, CACHE_VALUE);
  }

  @Test
  void testCachePut_shouldPropagateException_whenProceedFails() throws Throwable {
    // Given
    RuntimeException expectedException = new RuntimeException(EXCEPTION_MESSAGE);

    when(joinPoint.proceed()).thenThrow(expectedException);

    // When
    RuntimeException actualException =
        assertThrows(RuntimeException.class, () -> cacheAspect.cachePut(joinPoint, cachePut));

    // Then
    assertSame(expectedException, actualException);
    verify(joinPoint).proceed();
  }
}
