package com.oops.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

// 인벤토리의 태그
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(InventoryTag.class)
public class InventoryTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id")
    private Inventory inventory;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id")
    private Tag tag;

    public static InventoryTag create(Inventory inventory, Tag tag) {
        return InventoryTag.builder()
                .inventory(inventory)
                .tag(tag)
                .build();
    }
}
