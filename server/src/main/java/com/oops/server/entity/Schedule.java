package com.oops.server.entity;

import com.oops.server.compositekey.ScheduleID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(ScheduleID.class)
@Table(name = "schedule")
public class Schedule {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Id
    private LocalDate date;

    @Column(length = 10, nullable = false)
    private String tagList;

    @Column(nullable = false, columnDefinition = "TIME(0)")
    private LocalTime outTime;

    @Column(length = 10)
    private String notification;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE)
    private List<DateTodo> dateTodo;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE)
    private List<DateStuff> dateStuffs;

    @ManyToOne
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id")
    private Inventory inventory;

    public static Schedule create(User user, LocalDate date, String tagList, LocalTime outTime, String notification) {
        return Schedule.builder()
                .user(user)
                .date(date)
                .tagList(tagList)
                .outTime(outTime)
                .notification(notification)
                .build();
    }

    public void modifyInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void deleteInventory() { this.inventory = null; }

    public void modifyTagList(String tagList) {
        this.tagList = tagList;
    }

    public void modifyOutTime(LocalTime outTime) {
        this.outTime = outTime;
    }

    public void modifyNotification(String notification) {
        this.notification = notification;
    }
}
