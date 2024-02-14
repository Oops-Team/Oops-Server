package com.oops.server.composite_key;

import com.oops.server.entity.Inventory;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryObjectID implements Serializable {

    private Inventory inventory;
    private String objectName;
}