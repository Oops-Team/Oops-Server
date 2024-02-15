package com.oops.server.repository;

import com.oops.server.compositekey.InventoryStuffID;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryStuff;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStuffRepository extends JpaRepository<InventoryStuff, InventoryStuffID> {

    // 특정 인벤토리의 모든 소지품 삭제
    @Transactional
    void deleteAllByInventory(Inventory inventory);
}
