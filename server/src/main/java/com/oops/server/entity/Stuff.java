package com.oops.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;

@Entity
@Getter
@Table(name = "stuff")
public class Stuff {

    @Id
    @Column(length = 20)
    private String name;

    @Column(length = 500)
    private String img_url;

    private int rank;

    @JsonIgnore
    @OneToMany(mappedBy = "stuff")
    private List<InventoryStuff> inventoryStuffs;

    @OneToMany(mappedBy = "stuff")
    private List<DateStuff> dateStuffs;
}
