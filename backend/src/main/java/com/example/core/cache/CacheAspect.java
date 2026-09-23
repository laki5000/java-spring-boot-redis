package com.example.core.cache;

import com.example.core.exception.CacheException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CacheAspect {

  private final ICacheService cacheService;

  public CacheAspect(ICacheService cacheService) {
    this.cacheService = cacheService;
  }

  @Around("@annotation(cacheable)")
  public Object cacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

    Object cachedValue;

    try {
      Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
      cachedValue = cacheService.get(cacheable.key(), returnType);
    } catch (CacheException e) {
      log.warn("Cache get failed", e);
      cachedValue = null;
    }

    if (cachedValue != null) {
      return cachedValue;
    }

    Object result = joinPoint.proceed();

    if (result == null) {
      return null;
    }

    putToCache(cacheable.key(), result, cacheable.ttlSeconds());

    return result;
  }

  @Around("@annotation(cachePut)")
  public Object cachePut(ProceedingJoinPoint joinPoint, CachePut cachePut) throws Throwable {

    Object result = joinPoint.proceed();

    if (result == null) {
      return null;
    }

    putToCache(cachePut.key(), result, cachePut.ttlSeconds());

    return result;
  }

  private void putToCache(String key, Object value, long ttlSeconds) {
    try {
      if (ttlSeconds > 0) {
        cacheService.put(key, value, Duration.ofSeconds(ttlSeconds));
      } else {
        cacheService.put(key, value);
      }
    } catch (CacheException e) {
      log.warn("Cache put failed", e);
    }
  }
}
