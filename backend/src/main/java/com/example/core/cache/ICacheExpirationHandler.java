package com.example.core.cache;

public interface ICacheExpirationHandler {

  void onExpired(String key);
}
