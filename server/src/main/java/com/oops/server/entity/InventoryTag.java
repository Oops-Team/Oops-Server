package com.oops.server.entity;

import com.oops.server.compositekey.InventoryTagID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인벤토리의 태그
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(InventoryTagID.class)
@Table(name = "inventory_tag")
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
