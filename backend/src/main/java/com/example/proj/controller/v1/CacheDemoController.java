package com.example.proj.controller.v1;

import com.example.generated.api.CacheDemoApi;
import com.example.generated.dto.ApiResponseString;
import com.example.generated.dto.CacheDemoRequest;
import com.example.proj.service.CacheDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CacheDemoController implements CacheDemoApi {

  private final CacheDemoService cacheDemoService;

  @Override
  public ResponseEntity<ApiResponseString> getCacheDemo(String key) {
    ApiResponseString response = new ApiResponseString();
    response.setData(cacheDemoService.getCacheDemo(key));

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ApiResponseString> getCacheableDemo() {
    ApiResponseString response = new ApiResponseString();
    response.setData(cacheDemoService.getCacheableDemo());

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> putCacheDemo(CacheDemoRequest body) {
    cacheDemoService.putCacheDemo(body.getKey(), body.getValue(), body.getTtlSeconds());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> putCachePutDemo() {
    cacheDemoService.putCachePutDemo();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteCacheDemo(String key) {
    cacheDemoService.deleteCacheDemo(key);
    return ResponseEntity.noContent().build();
  }
}
