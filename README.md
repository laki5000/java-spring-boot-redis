# Java Spring Boot Redis

A Spring Boot project demonstrating Redis integration and caching.

This project is derived from the [Java Spring Boot Core](https://github.com/laki5000/java-spring-boot-core) project.

## Redis Integration

The project provides a Redis integration through `RedisCacheService`.

It supports storing, retrieving, and removing cached values, including cache expiration using TTL.

## Cache Annotations

The project provides two annotations for easier caching.

### `@Cacheable`

The `@Cacheable` annotation checks the cache before executing the annotated method.

If a cached value exists, it is returned and the method is not executed.

If no cached value exists, the method is executed and its result is stored in the cache.

### `@PutCache`

The `@PutCache` annotation executes the annotated method and stores its result in the cache.

It does not try to retrieve an existing value before executing the method.

## Demo Endpoints

The project contains demo endpoints for testing and demonstrating the caching functionality.

Caching can be used in two ways:

* Directly through `RedisCacheService`
* Through the `@Cacheable` and `@PutCache` annotations

The annotation-based approach uses `RedisCacheService` internally.
