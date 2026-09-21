package com.example.core.cache;

import java.time.Duration;

public interface ICacheService {

  <T> T get(String key, Class<T> type);

  <T> void put(String key, T value);

  <T> void put(String key, T value, Duration ttl);

  void delete(String key);
}
