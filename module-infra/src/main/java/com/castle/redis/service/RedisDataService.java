package com.castle.redis.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 데이터를 처리하는 비즈니스 로직 인터페이스입니다.
 */
@Service
public interface RedisDataService {

    int setSingleData(String key, Object value);

    int setSingleData(String key, Object value, Duration duration);

    String getSingleData(String key);

    int deleteSingleData(String key);

    Long getTTL(String key);
}