package com.oops.server.entity;

import com.oops.server.compositekey.DateStuffID;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@IdClass(DateStuffID.class)
@Table(name = "date_stuff")
public class DateStuff {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "date", referencedColumnName = "date"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private Schedule schedule;


    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stuff_name", referencedColumnName = "name")
    private Stuff stuff;

    public static DateStuff create(Schedule schedule, Stuff stuff) {
        return DateStuff.builder()
                .schedule(schedule)
                .stuff(stuff)
                .build();
    }
}
