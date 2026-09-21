package com.example.proj.handler;

import com.example.core.cache.ICacheExpirationHandler;
import org.springframework.stereotype.Component;

@Component
public class CacheExpirationHandler implements ICacheExpirationHandler {
  @Override
  public void onExpired(String key) {}
}
