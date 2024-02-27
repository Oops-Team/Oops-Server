package com.oops.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "date_todo")
public class DateTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "date", referencedColumnName = "date"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private Schedule schedule;

    @Column(length = 20)
    private String content;

    @ColumnDefault("false")
    @Column(name = "is_complete")
    private boolean isComplete;

    public static DateTodo create(Schedule schedule, String content) {
        return DateTodo.builder()
                .schedule(schedule)
                .content(content)
                .build();
    }
}
