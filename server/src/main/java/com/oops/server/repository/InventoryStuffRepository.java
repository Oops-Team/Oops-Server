package com.oops.server.repository;

import com.oops.server.compositekey.InventoryStuffID;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryStuff;
import com.oops.server.entity.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStuffRepository extends JpaRepository<InventoryStuff, InventoryStuffID> {

    // 특정 인벤토리의 모든 소지품 삭제
    @Transactional
    void deleteAllByInventory(Inventory inventory);

    // 특정 인벤토리의 모든 소지품 가져오기 (중요도 순으로 정렬)
    List<InventoryStuff> findAllByInventoryOrderByStuffStuffRank(Inventory inventory);

    // 해당 유저의 모든 인벤토리-소지품까지 불러오기 (중요도 순으로 정렬)
    List<InventoryStuff> findAllByInventoryUserOrderByStuffStuffRank(User user);
}
