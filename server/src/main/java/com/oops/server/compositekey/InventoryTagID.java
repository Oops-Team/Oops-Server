package com.oops.server.compositekey;

import com.oops.server.entity.Inventory;
import com.oops.server.entity.Tag;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTagID implements Serializable {

    private Inventory inventory;
    private Tag tag;
}
