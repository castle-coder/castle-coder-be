package com.castle.redis.service;

import com.castle.redis.config.RedisConfig;
import com.castle.redis.handler.RedisHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 단일 데이터를 처리하는 비즈니스 로직 구현체입니다.
 */
@Service
@RequiredArgsConstructor
public class RedisDataServiceImpl implements RedisDataService {

    private final RedisHandler redisHandler;
    private final RedisConfig redisConfig;

    /**
     * Redis 단일 데이터 값을 등록/수정합니다.
     *
     * @param key   : redis key
     * @param value : redis value
     * @return {int} 성공(1), 실패(0)
     */
    @Override
    public int setSingleData(String key, Object value) {
        return redisHandler.executeOperation(() -> redisHandler.getValueOperations().set(key, value));
    }

    /**
     * Redis 단일 데이터 값을 등록/수정합니다.(duration 값이 존재하면 메모리 상 유효시간을 지정합니다.)
     *
     * @param key      : redis key
     * @param value:   : redis value
     * @param duration : redis 값 메모리 상의 유효시간.
     * @return {int} 성공(1), 실패(0)
     */
    @Override
    public int setSingleData(String key, Object value, Duration duration) {
        return redisHandler.executeOperation(() -> redisHandler.getValueOperations().set(key, value, duration));
    }

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 조회합니다.
     *
     * @param key : redis key
     * @return {String} redis value 값 반환 or 미 존재시 null 반환
     */
    @Override
    public String getSingleData(String key) {
        if (redisHandler.getValueOperations().get(key) == null) return "";
        return String.valueOf(redisHandler.getValueOperations().get(key));
    }

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 삭제합니다.
     *
     * @param key : redis key
     * @return {int} 성공(1), 실패(0)
     */
    @Override
    public int deleteSingleData(String key) {
        return redisHandler.executeOperation(() -> redisConfig.redisTemplate().delete(key));
    }

    /**
     * Redis 키의 TTL(남은 유효 시간)을 조회합니다.
     *
     * @param key : redis key
     * @return {Long} TTL 값 (초 단위) or -1 (만료 시간 없음) or -2 (키가 존재하지 않음)
     */
    @Override
    public Long getTTL(String key) {
        return redisConfig.redisTemplate().getExpire(key, TimeUnit.SECONDS);
    }
}