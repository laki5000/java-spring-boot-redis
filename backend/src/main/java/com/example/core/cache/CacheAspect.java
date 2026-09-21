package com.example.core.cache;

import com.example.core.exception.CacheException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
      cachedValue = cacheService.get(cacheable.key(), cacheable.type());
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

    try {
      cacheService.put(cacheable.key(), result);
    } catch (CacheException e) {
      log.warn("Cache put failed", e);
    }

    return result;
  }

  @Around("@annotation(cachePut)")
  public Object cachePut(ProceedingJoinPoint joinPoint, CachePut cachePut) throws Throwable {

    Object result = joinPoint.proceed();

    if (result == null) {
      return null;
    }

    try {
      if (cachePut.ttlSeconds() > 0) {
        cacheService.put(cachePut.key(), result, Duration.ofSeconds(cachePut.ttlSeconds()));
      } else {
        cacheService.put(cachePut.key(), result);
      }
    } catch (CacheException e) {
      log.warn("Cache put failed", e);
    }

    return result;
  }
}
