package com.example.proj.controller.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.core.cache.ICacheService;
import com.example.generated.dto.ApiResponseString;
import com.example.generated.dto.PutCacheDemoRequest;
import com.example.proj.handler.CacheDemoExpirationHandler;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CacheDemoControllerIntegrationTests {

  private static final String CACHE_DEMO_ENDPOINT = "/api/v1/cache/demo";
  private static final String CACHEABLE_DEMO_ENDPOINT = "/api/v1/cache/demo/cacheable";
  private static final String CACHE_PUT_DEMO_ENDPOINT = "/api/v1/cache/demo/cache-put";

  private static final String PARAM_KEY = "key";

  private static final String CACHE_KEY = "key";
  private static final String CACHE_VALUE = "value";
  private static final long CACHE_TTL_SECONDS = 3;

  private static final String CACHEABLE_DEMO_VALUE = "value-for-cacheable-demo";

  private static final String CACHE_ENTRY_EXPIRED_LOG = "Cache entry expired for key: " + CACHE_KEY;

  @Autowired private MockMvc mockMvc;

  @Autowired private ICacheService cacheService;

  @Autowired private ObjectMapper objectMapper;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    cacheService.delete(CACHE_KEY);

    logger = (Logger) LoggerFactory.getLogger(CacheDemoExpirationHandler.class);

    listAppender = new ListAppender<>();
    listAppender.start();

    logger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);

    cacheService.delete(CACHE_KEY);
  }

  @Test
  void testGetCacheDemo_shouldReturnCachedValue_whenCacheEntryExists() throws Exception {
    // Given
    cacheService.put(CACHE_KEY, CACHE_VALUE);

    // When / Then
    mockMvc
        .perform(get(CACHE_DEMO_ENDPOINT).param(PARAM_KEY, CACHE_KEY))
        .andExpect(status().isOk());
  }

  @Test
  void testGetCacheDemo_shouldReturnNotFound_whenCacheEntryDoesNotExist() throws Exception {
    // When / Then
    mockMvc
        .perform(get(CACHE_DEMO_ENDPOINT).param(PARAM_KEY, CACHE_KEY))
        .andExpect(status().isNotFound());
  }

  @Test
  void testPutCacheDemo_shouldStoreValue() throws Exception {
    // Given
    PutCacheDemoRequest request = createRequest(null);

    // When
    mockMvc
        .perform(
            put(CACHE_DEMO_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // Then
    assertEquals(CACHE_VALUE, cacheService.get(CACHE_KEY, String.class));
  }

  @Test
  void testDeleteCacheDemo_shouldDeleteCacheEntry() throws Exception {
    // Given
    cacheService.put(CACHE_KEY, CACHE_VALUE);

    // When
    mockMvc
        .perform(delete(CACHE_DEMO_ENDPOINT).param(PARAM_KEY, CACHE_KEY))
        .andExpect(status().isNoContent());

    // Then
    assertNull(cacheService.get(CACHE_KEY, String.class));
  }

  @Test
  void testPutCacheDemo_shouldLogExpiration_whenCacheEntryExpires() throws Exception {
    // Given
    PutCacheDemoRequest request = createRequest(CACHE_TTL_SECONDS);

    // When
    mockMvc
        .perform(
            put(CACHE_DEMO_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // Then
    assertEquals(CACHE_VALUE, cacheService.get(CACHE_KEY, String.class));

    waitForExpiration();

    assertTrue(
        listAppender.list.stream()
            .anyMatch(
                event ->
                    event.getLevel() == Level.INFO
                        && CACHE_ENTRY_EXPIRED_LOG.equals(event.getFormattedMessage())));
  }

  @Test
  void testGetCacheableDemo_shouldReturnExpectedValue() throws Exception {
    // When
    String response =
        mockMvc
            .perform(get(CACHEABLE_DEMO_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Then
    ApiResponseString apiResponse = objectMapper.readValue(response, ApiResponseString.class);

    assertEquals(CACHEABLE_DEMO_VALUE, apiResponse.getData());
  }

  @Test
  void testPutCachePutDemo_shouldExecuteSuccessfully() throws Exception {
    // When / Then
    mockMvc.perform(put(CACHE_PUT_DEMO_ENDPOINT)).andExpect(status().isNoContent());
  }

  private PutCacheDemoRequest createRequest(Long ttlSeconds) {
    PutCacheDemoRequest request = new PutCacheDemoRequest();
    request.setKey(CACHE_KEY);
    request.setValue(CACHE_VALUE);
    request.setTtlSeconds(ttlSeconds);

    return request;
  }

  private void waitForExpiration() throws InterruptedException {
    long expirationTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(CACHE_TTL_SECONDS);

    while (System.nanoTime() < expirationTime) {
      Thread.sleep(100);
    }

    long timeout = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

    while (System.nanoTime() < timeout) {
      if (!listAppender.list.isEmpty()) {
        return;
      }

      Thread.sleep(100);
    }
  }
}
