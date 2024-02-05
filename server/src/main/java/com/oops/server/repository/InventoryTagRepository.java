package com.oops.server.repository;

import com.oops.server.composite_key.InventoryTagID;
import com.oops.server.entity.Inventory;
import com.oops.server.entity.InventoryTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTagRepository extends JpaRepository<InventoryTag, InventoryTagID> {

}
