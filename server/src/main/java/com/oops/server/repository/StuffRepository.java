package com.oops.server.repository;

import com.oops.server.entity.Stuff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StuffRepository extends JpaRepository<Stuff, String> {

    // 소지품 이름으로 찾기
    Stuff findByName(String name);

    // 정렬해서 모두 가져오기
    List<Stuff> findAllByOrderByStuffRank();
}
