package com.oops.server.repository;

import com.oops.server.compositekey.InventoryTagID;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryTag;
import com.oops.server.entity.Tag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTagRepository extends JpaRepository<InventoryTag, InventoryTagID> {

    // 특정 태그를 갖고 있는 특정 인벤토리 데이터 가져오기
    InventoryTag findByInventoryAndTag(Inventory inventory, Tag tag);

    // 특정 인벤토리의 레코드(저장된 태그) 전부 삭제
    @Transactional
    void deleteAllByInventory(Inventory inventory);
}
