package com.oops.server.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Getter
@Builder
@Table(name = "inventory")
public class Inventory {

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(length = 6, nullable = false)
    private String name;

    @Column(nullable = false)
    private int icon;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
    private List<InventoryTag> inventoryTags;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
    private List<InventoryStuff> inventoryStuffs;

    @OneToMany(mappedBy = "inventory")
    private List<Schedule> schedules;

    public static Inventory create(User user, String inventoryName) {
        return Inventory.builder()
                .user(user)
                .name(inventoryName)
                .icon(1)
                .build();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(int icon) { this.icon = icon; }
}
