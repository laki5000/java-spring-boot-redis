package com.example.integration.redis;

import com.example.core.cache.ICacheExpirationHandler;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisExpirationListener implements MessageListener {

  private final ICacheExpirationHandler handler;

  public RedisExpirationListener(ICacheExpirationHandler handler) {
    this.handler = handler;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String key = new String(message.getBody(), StandardCharsets.UTF_8);

    log.debug("Redis key expired: {}", key);

    handler.onExpired(key);
  }
}
