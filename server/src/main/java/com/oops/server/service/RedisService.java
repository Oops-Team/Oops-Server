package com.oops.server.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    // 인증번호 만료 시간 (2분)
    private final int EXPIRATION_TIME_MINUTES = 2;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 인증번호 저장
    public void saveVerificationCode(String userEmail, String code) {
        redisTemplate.opsForValue().set(userEmail, code, EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
    }

    // Redis에서 인증번호 가져오기
    public String getVerificationCode(String userEmail) {
        return redisTemplate.opsForValue().get(userEmail);
    }

    // 저장된 인증번호 삭제
    public void deleteVerificationCode(String userEmail) {
        redisTemplate.delete(userEmail);
    }
}
