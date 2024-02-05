package com.oops.server.repository;

import com.oops.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 특정 sns의 회원 찾기
    User findByEmailAndSnsType(String email, String snsType);

    // 닉네임으로 회원 찾기
    User findByName(String name);

    // 유저 고유 아이디값으로 해당 회원 찾기
    User findByUserId(Long userId);
}
