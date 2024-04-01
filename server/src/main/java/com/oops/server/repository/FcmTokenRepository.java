package com.oops.server.repository;

import com.oops.server.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    // 특정 유저의 정보 가져오기
    FcmToken findByUserId(Long userId);
}
