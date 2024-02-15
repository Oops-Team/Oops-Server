package com.oops.server.repository;

import com.oops.server.entity.Stuff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StuffRepository extends JpaRepository<Stuff, String> {

    // 소지품 이름으로 찾기
    Stuff findByName(String name);
}
