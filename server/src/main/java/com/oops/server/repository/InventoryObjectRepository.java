package com.oops.server.repository;

import com.oops.server.composite_key.InventoryObjectID;
import com.oops.server.entity.InventoryObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryObjectRepository extends JpaRepository<InventoryObject, InventoryObjectID> {


}
