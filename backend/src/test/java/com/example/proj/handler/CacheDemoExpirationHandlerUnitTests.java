package com.example.proj.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class CacheDemoExpirationHandlerUnitTests {

  private static final String KEY = "key";
  private static final String OTHER_KEY = "other-key";
  private static final String LOG_MESSAGE = "Cache entry expired for key: " + KEY;

  private CacheDemoExpirationHandler handler;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    handler = new CacheDemoExpirationHandler();

    logger = (Logger) LoggerFactory.getLogger(CacheDemoExpirationHandler.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
  }

  @Test
  void testOnExpired_shouldLogExpiration_whenKeyMatches() {
    // When
    handler.onExpired(KEY);

    // Then
    assertEquals(1, listAppender.list.size());

    ILoggingEvent logEvent = listAppender.list.getFirst();

    assertEquals(Level.INFO, logEvent.getLevel());
    assertEquals(LOG_MESSAGE, logEvent.getFormattedMessage());
  }

  @Test
  void testOnExpired_shouldNotLogExpiration_whenKeyDoesNotMatch() {
    // When
    handler.onExpired(OTHER_KEY);

    // Then
    assertTrue(listAppender.list.isEmpty());
  }
}
