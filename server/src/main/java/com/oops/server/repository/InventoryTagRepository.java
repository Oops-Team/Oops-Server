package com.oops.server.repository;

import com.oops.server.compositekey.InventoryTagID;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryTag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTagRepository extends JpaRepository<InventoryTag, InventoryTagID> {

    // 특정 인벤토리의 레코드(저장된 태그) 전부 삭제
    @Transactional
    void deleteAllByInventory(Inventory inventory);
}
