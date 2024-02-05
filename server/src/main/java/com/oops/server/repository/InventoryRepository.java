package com.oops.server.repository;

import com.oops.server.entity.Inventory;
import com.oops.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 해당 유저의 특정 인벤토리 찾기 (인벤토리 이름 이용)
    Inventory findByUserAndName(User user, String name);
}
