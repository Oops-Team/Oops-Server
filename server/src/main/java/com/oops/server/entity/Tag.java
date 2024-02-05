package com.oops.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
@Table(name = "tag")
public class Tag {

    @Id
    private Integer id;

    @Column(length = 3)
    private String name;

    @OneToMany(mappedBy = "tag")
    private List<InventoryTag> inventoryTags;

    public static Tag create(Integer id, String name) {
        return Tag.builder()
                .id(id)
                .name(name)
                .build();
    }
}
