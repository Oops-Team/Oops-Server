package com.oops.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oops.server.compositekey.InventoryStuffID;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(InventoryStuffID.class)
@Table(name = "inventory_stuff")
public class InventoryStuff {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id")
    private Inventory inventory;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "stuff_name", referencedColumnName = "name")
    private Stuff stuff;

    public static InventoryStuff create(Inventory inventory, Stuff stuff) {
        return InventoryStuff.builder()
                .inventory(inventory)
                .stuff(stuff)
                .build();
    }
}
