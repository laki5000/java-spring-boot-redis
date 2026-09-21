package com.example.proj.handler;

import com.example.core.cache.ICacheExpirationHandler;
import com.example.core.logging.LogExecution;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheDemoExpirationHandler implements ICacheExpirationHandler {

  private static final String KEY = "key";

  @LogExecution(level = Level.INFO, logArguments = true)
  @Override
  public void onExpired(String key) {
    if (key.equals(KEY)) {
      log.info("Cache entry expired for key: {}", key);
    }
  }
}
