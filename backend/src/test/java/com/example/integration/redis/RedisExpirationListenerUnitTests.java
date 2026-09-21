package com.example.integration.redis;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.core.cache.ICacheExpirationHandler;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

@ExtendWith(MockitoExtension.class)
class RedisExpirationListenerUnitTests {

  private static final String CACHE_KEY = "user:123";

  @Mock private ICacheExpirationHandler handler;

  @Mock private Message message;

  private RedisExpirationListener redisExpirationListener;

  @BeforeEach
  void setUp() {
    redisExpirationListener = new RedisExpirationListener(handler);
  }

  @Test
  void testOnMessage_shouldNotifyHandlerWithExpiredKey() {
    // Given
    when(message.getBody()).thenReturn(CACHE_KEY.getBytes(StandardCharsets.UTF_8));

    // When
    redisExpirationListener.onMessage(message, null);

    // Then
    verify(handler).onExpired(CACHE_KEY);
  }
}
